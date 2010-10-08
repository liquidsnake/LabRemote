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

class AttrDict(dict):
    __getattr__= dict.__getitem__
    __setattr__= dict.__setitem__
    __delattr__= dict.__delitem__

class ApiTestCase(TestCase):
    #urls = "middleware.rest.urls"
    def setUp(self):
        #Some defined objects for the test
        fixtures = AttrDict({
            "course" : AttrDict({
                "external_id" : 0,
                "name" : "Test course",
                "title" : "Test course",
                "start_year" : 2010,
                "start_week" : int(datetime.now().strftime("%w")) - 5,
                "max_weeks" : 10,
                "inactive_weeks" : "1,4,6",
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
        for i in range (1,self.course.max_weeks):
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
    def test_good_parameters(self):
        tested = self.c.get(reverse(views.timetable, kwargs={ "user":self.assistant.id, "session_key":self.assistant.code, "course":self.course.id }))
        expected = '{"timetable": {"monday": {}, "tuesday": {}, "friday": {}, "wednesday": {"08:00-10:00": [{"name": "group", "id": 1}]}, "thursday": {}, "sunday": {}, "saturday": {}}, "status": "success"}'

        self.assertEqual( tested.content, expected,
                         'Incorrect timetable response for correct timetable attempt')

    def test_bad_course(self):
        tested = self.c.get(reverse(views.timetable, kwargs={ "user":self.assistant.id, "session_key":self.assistant.code, "course": 999}))
        expected = self.error_json("No such course")

        self.assertEqual( tested.content, expected,
                         'Incorrect timetable response for bad course(should have errored)')

