"""
This file demonstrates two different styles of tests (one doctest and one
unittest). These will both pass when you run "manage.py test".

Replace these with more appropriate tests for your application.
"""

from django.test import TestCase, Client
from models import *
import middleware.rest.views as views
from datetime import datetime
from django.contrib.auth.models import User
from django.core.urlresolvers import reverse
import middleware.settings
import json

class AttrDict(dict):
    __getattr__= dict.__getitem__
    __setattr__= dict.__setitem__
    __delattr__= dict.__delitem__

def compute_date(course, week, activity):
    comp_date = datetime.strptime('%d %d 1' % (course.start_year, course.start_week), '%Y %W %w')
    comp_date = comp_date + timedelta(weeks = week - 1, days = activity.day)
    return comp_date.strftime("%a, %d %B")

class ApiTestCase(TestCase):
    #urls = "middleware.rest.urls"
    def setUp(self):
        #Some defined objects for the test
        self.current_week = 6
        fixtures = AttrDict({
            "course" : AttrDict({
                "external_id" : 0,
                "name" : "Test course",
                "title" : "Test course",
                "start_year" : 2010,
                "start_week" : int(datetime.now().strftime("%W")) - self.current_week + 1,
                "max_weeks" : 10,
                "inactive_weeks" : "1,4,7",
            }),
            "group" : AttrDict({
                "parent_group" : "group",
                "name" : "group",
            }),
            "student" : AttrDict({
                "external_id" : 0,
                "first_name" : "Test",
                "last_name" : "Student",
            }),
            "assistant" : AttrDict({
                "code" : "code",
                "first_name" : "Tester", 
                "last_name" : "Assistant",
            }),
            "user" : AttrDict({
                "username" : "admin", 
                "email" : "adm@lr.ro", 
                "password" : "test",
            }),
            "activity" : AttrDict({
                "day" : 2,
                "time_hour_start" : 8,
                "time_hour_end" : 10,
                "time_minute_start" : 0,
                "time_minute_end" : 0,
            }),
            "attendance" : AttrDict({
                "grade" : 5,
            }),
        })
        self.course = Course(
            external_id = fixtures.course.external_id,
            name = fixtures.course.name, 
            title = fixtures.course.title,
            start_year = fixtures.course.start_year,
            start_week = fixtures.course.start_week,
            max_weeks = fixtures.course.max_weeks,
            inactive_weeks = fixtures.course.inactive_weeks
        )
        self.course.save()

        self.group = Group(
            parent_group = fixtures.group.parent_group,
            name = fixtures.group.name,
            course = self.course
        )
        self.group.save()

        self.student = Student(
            external_id = fixtures.student.external_id,
            first_name = fixtures.student.first_name,
            last_name = fixtures.student.last_name,
        )
        self.student.save()
        self.student.virtual_group.add(self.group)

        self.assistant = Assistant(
            code = fixtures.assistant.code,
            first_name = fixtures.assistant.first_name, 
            last_name = fixtures.assistant.last_name
        )
        self.assistant.save()
        self.assistant.courses.add(self.course)
        self.assistant.groups.add(self.group)
        self.assistant.save()
        
        self.user = User.objects.create_user(
            fixtures.user.username,
            fixtures.user.email,
            fixtures.user.password
        )
        self.user.save()
        self.userprofile = self.user.get_profile()
        self.userprofile.user = self.user
        self.userprofile.assistant = self.assistant
        self.userprofile.approved = True
        self.userprofile.save()
       
        self.activity = Activity(
            course = self.course, 
            group = self.group,
            day = fixtures.activity.day,
            time_hour_start = fixtures.activity.time_hour_start,
            time_hour_end = fixtures.activity.time_hour_end,
            time_minute_start = fixtures.activity.time_minute_start,
            time_minute_end = fixtures.activity.time_minute_end
        )
        self.activity.save()

        self.attendances = []
        for i in range (1,self.course.max_weeks + 1):
            attendance = Attendance(
                student = self.student,
                course = self.course,
                activity = self.activity,
                grade = fixtures.attendance.grade,
                week = i
            )
            attendance.save()
            self.attendances.append(attendance)


        self.c = Client()
        self.c.login(username="admin", password="test")
       
    @staticmethod 
    def error_json(error):
        return '{"status": "failed", "error": "%s"}' % error

class LoginTestCase(ApiTestCase):
    def test_good_code(self):
        tested = self.c.get(reverse(views.login, args=[ self.assistant.code ]))
        expected = '{"status": "success", "courses": [{"name": "Test course", "abbr": "Test course", "id": 1}], "user": 2, "name": "Tester Assistant"}'
        self.assertEqual( tested.content, expected,
                         'Incorrect login response for correct login attempt')

    def test_bad_code(self):
        tested = self.c.get(reverse(views.login, args=[ "bad_code" ]))
        expected = self.error_json("Invalid code")
        self.assertEqual( tested.content, expected,
                         'Incorrect login response for incorrect login attempt')


class TimetableTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id
        }
        
    def test_good_parameters(self):
        tested = self.c.get(reverse(views.timetable, kwargs = self.params))
        expected = '{"timetable": {"monday": {}, "tuesday": {}, "friday": {}, "wednesday": {"08:00-10:00": [{"name": "group", "id": 1}]}, "thursday": {}, "sunday": {}, "saturday": {}}, "status": "success"}'

        self.assertEqual( tested.content, expected,
                         'Incorrect timetable response for correct timetable attempt')

    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.get(reverse(views.timetable, kwargs=self.params))
        expected = self.error_json("No such course")

        self.assertEqual( tested.content, expected,
                         'Incorrect timetable response for bad course(should have errored)')


class GroupTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id, 
            "name": "group", 
            "activity_id" : self.activity.id 
        }
    
    def test_good_parameters_no_week_normal(self):
        tested = self.c.get(reverse('views.group', kwargs = self.params))
        expected = '{"week": %d, "status": "success", "activity_id": "1", "name": "group", "students": [{"grade": 5, "name": "Test Student", "avatar": "", "id": 1}], "inactive_weeks": [1, 4, 7], "date": "%s", "max_weeks": 10}' % (self.current_week, compute_date(self.course, self.current_week, self.activity))

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, no week, good parameters')

    def test_good_parameters_week_normal(self):
        #change grade for week 3
        selected_week = 3
        self.attendances[selected_week - 1].grade = 7
        self.attendances[selected_week - 1].save()
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.group_week', kwargs = self.params))
        expected = '{"week": %d, "status": "success", "activity_id": "1", "name": "group", "students": [{"grade": 7, "name": "Test Student", "avatar": "", "id": 1}], "inactive_weeks": [1, 4, 7], "date": "%s", "max_weeks": 10}' % (selected_week, compute_date(self.course, selected_week, self.activity))

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, normal week, good parameters')

    def test_good_parameters_week_holiday(self):
        #use week 4 which is in holiday
        selected_week = 4
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.group_week', kwargs = self.params))
        expected = '{"week": %d, "status": "success", "activity_id": "1", "name": "group", "students": [], "inactive_weeks": [1, 4, 7], "date": "%s", "max_weeks": 10}' % (selected_week, compute_date(self.course, selected_week, self.activity))

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, no week, good parameters')

    def test_bad_week(self):
        selected_week = 0
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.group_week', kwargs = self.params))
        expected = self.error_json("The selected week is invalid") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, week 0 (should have errored)')
        selected_week = self.course.max_weeks + 1
        tested = self.c.get(reverse('views.group_week', kwargs = self.params))
        expected = self.error_json("The selected week is invalid") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, week = max_week+1 (should have errored)')

    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.get(reverse('views.group', kwargs = self.params))
        expected = self.error_json("No such course") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, bad course (should have errored)')
        
    def test_bad_activity(self):
        self.params["activity_id"] = 999
        tested = self.c.get(reverse('views.group', kwargs = self.params))
        expected = self.error_json("No such activity") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, bad activity (should have errored)')


class CurrentGroupTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        #create a new "current activity" to test
        current_hour = datetime.now().time().hour

        self.current_activity = Activity(
            course = self.course, 
            group = self.group,
            day = datetime.today().weekday(),
            time_hour_start = current_hour - 1,
            time_hour_end = current_hour + 1,
            time_minute_start = 0,
            time_minute_end = 0
        )
        self.current_activity.save()
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id, 
        }
    
    def test_good_parameters_no_week_normal(self):
        tested = self.c.get(reverse('views.current_group', kwargs = self.params))
        expected = '{"week": 6, "status": "success", "activity_id": 2, "name": "group", "students": [{"grade": 0, "name": "Test Student", "avatar": "", "id": 1}], "inactive_weeks": [1, 4, 7], "date": "Fri, 08 October", "max_weeks": 10}'

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, no week, good parameters')

    def test_good_parameters_week_normal(self):
        #change grade for week 3
        selected_week = 3
        self.attendances[selected_week - 1].grade = 7
        self.attendances[selected_week - 1].save()
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.current_group_week', kwargs = self.params))
        expected = '{"week": 3, "status": "success", "activity_id": 2, "name": "group", "students": [{"grade": 0, "name": "Test Student", "avatar": "", "id": 1}], "inactive_weeks": [1, 4, 7], "date": "Fri, 17 September", "max_weeks": 10}'

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, normal week, good parameters')

    def test_good_parameters_week_holiday(self):
        #use week 4 which is in holiday
        selected_week = 4
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.current_group_week', kwargs = self.params))
        expected = '{"week": 4, "status": "success", "activity_id": 2, "name": "group", "students": [], "inactive_weeks": [1, 4, 7], "date": "Fri, 24 September", "max_weeks": 10}'

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, no week, good parameters')

    def test_bad_week(self):
        selected_week = 0
        self.params["week"] = selected_week
        tested = self.c.get(reverse('views.current_group_week', kwargs = self.params))
        expected = self.error_json("The selected week is invalid") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, week 0 (should have errored)')
        selected_week = self.course.max_weeks + 1
        tested = self.c.get(reverse('views.current_group_week', kwargs = self.params))
        expected = self.error_json("The selected week is invalid") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, week = max_week+1 (should have errored)')

    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.get(reverse('views.current_group', kwargs = self.params))
        expected = self.error_json("No such course") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, bad course (should have errored)')
    
    def test_no_current_group(self):
        self.current_activity.delete()
        tested = self.c.get(reverse('views.current_group', kwargs = self.params))
        expected = self.error_json("No current group") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for current group view, no current group (should have errored)')


class StudentTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id, 
            "id" : self.student.id,
        }
    
    def test_good_parameters(self):
        tested = self.c.get(reverse(views.student, kwargs = self.params))
        expected = '{"status": "success", "attendances": {"1": {"grade": 5, "grades": [5]}, "2": {"grade": 5, "grades": [5]}, "3": {"grade": 5, "grades": [5]}, "4": {"grade": 5, "grades": [5]}, "5": {"grade": 5, "grades": [5]}, "6": {"grade": 5, "grades": [5]}, "7": {"grade": 5, "grades": [5]}, "8": {"grade": 5, "grades": [5]}, "9": {"grade": 5, "grades": [5]}, "10": {"grade": 5, "grades": [5]}}, "group": "", "name": "Test Student", "avatar": "", "virtual_group": "group", "id": 1}'

        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, no week, good parameters')

    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.get(reverse(views.student, kwargs = self.params))
        expected = self.error_json("No such course") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, bad course (should have errored)')
    
    def test_bad_student(self):
        self.params["id"] = 999
        tested = self.c.get(reverse(views.student, kwargs = self.params))
        expected = self.error_json("No such student") 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for group view, bad student (should have errored)')
    
    def test_no_attendances(self):
        for att in self.attendances:
            att.delete()
        tested = self.c.get(reverse(views.student, kwargs = self.params))
        expected = '{"status": "success", "attendances": {"1": {"grade": 0, "grades": []}, "2": {"grade": 0, "grades": []}, "3": {"grade": 0, "grades": []}, "4": {"grade": 0, "grades": []}, "5": {"grade": 0, "grades": []}, "6": {"grade": 0, "grades": []}, "7": {"grade": 0, "grades": []}, "8": {"grade": 0, "grades": []}, "9": {"grade": 0, "grades": []}, "10": {"grade": 0, "grades": []}}, "group": "", "name": "Test Student", "avatar": "", "virtual_group": "group", "id": 1}' 
        self.assertEqual( tested.content, expected,
                         'Incorrect group response for student view, no attendances')


class SearchTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id,
            "query":"test", 
        }
    
    def test_existing_student(self):
        tested = self.c.get(reverse(views.search, kwargs = self.params))
        expected = '{"students": [{"id": 1, "avatar": "", "name": "Test Student"}], "status": "success"}'

        self.assertEqual( tested.content, expected,
                         'Incorrect student response for valid search')

    def test_non_existing_student(self):
        self.params["query"] = 'zxzxz'
        tested = self.c.get(reverse(views.search, kwargs = self.params))
        expected = '{"students": [], "status": "success"}'

        self.assertEqual( tested.content, expected,
                         'Incorrect student response for empty result search')
    
    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.get(reverse(views.search, kwargs = self.params))
        expected = self.error_json("No such course") 
        self.assertEqual( tested.content, expected,
                         'Incorrect student response for bad course (should have errored)')


class PostTestCase(ApiTestCase):
    def setUp(self):
        ApiTestCase.setUp(self)
        self.params = {
            "user":self.assistant.id, 
            "session_key":self.assistant.code, 
            "course":self.course.id,
            "type": "group",
        }
        self.new_grade = 9
        self.data = { 
            "week" : self.current_week,
            "activity_id" : self.activity.id,
            "students" : [{ "id" : self.student.id, "grade" : self.new_grade }],
        }
    
    def test_empty_post(self):
        tested = self.c.post(reverse(views.post_data), {})
        expected = '{"status": "failed", "error": "Malformed post request"}'
        
        self.assertEqual( tested.content, expected,
                         'Incorrect post for empty data (should have errored)')
    
    def test_bad_user(self):
        self.params["user"] = 999
        tested = self.c.post(reverse(views.post_data), self.params)
        expected = '{"status": "failed", "error": "No such user"}'
        
        self.assertEqual( tested.content, expected,
                         'Incorrect post for bad user (should have errored)')
    
    def test_bad_key(self):
        self.params["session_key"] = "qwerty"
        tested = self.c.post(reverse(views.post_data), self.params)
        expected = '{"status": "failed", "error": "Invalid session key"}'
        
        self.assertEqual( tested.content, expected,
                         'Incorrect post for bad code (should have errored)')

    def test_bad_course(self):
        self.params["course"] = 999
        tested = self.c.post(reverse(views.post_data), self.params)
        expected = self.error_json("No such course") 
        self.assertEqual( tested.content, expected,
                         'Incorrect post for bad course (should have errored)')

    def test_good_post(self):
        self.params["contents"] = json.dumps(self.data)
        tested = self.c.post(reverse(views.post_data), self.params)
        expected = '{"status": "success"}'
        self.assertEqual( tested.content, expected,
                         'Incorrect post for good data')
        self.assertEqual( Attendance.objects.get(student = self.student, course = self.course, activity = self.activity, week = self.current_week).grade, self.new_grade, "Incorrect post for new grade")
