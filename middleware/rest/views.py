from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse, Http404
from django.views.decorators.csrf import csrf_exempt
import json
from datetime import datetime
from datetime import time
from datetime import date

from models import *
from middleware.core.functions import get_week

DAYS = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]
DAYS_SHORT = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]

def json_response(dct, failed = False):
    """ A shorthand for dumping json data """
    if failed:
        dct['status'] = 'failed'
    else:
        dct['status'] = 'success'

    data = json.dumps(dct)
    return HttpResponse(data, mimetype='application/json')
    
def valid_key(view_func):
    """ A decorator checking the key for validity """
    def _decorated(request, user, check_hash, *args, **kwargs):
        try:
            assistant = Assistant.objects.get(pk=user)
        except Assistant.DoesNotExist:
            return json_response({"error":"no such user"}, failed = True)
            
        if assistant.get_check_hash(request) != check_hash:
            return json_response({"error":"invalid check hash, expected: %s got %s" % (assistant.get_check_hash(request), check_hash)}, failed = True)
            
        request.assistant = assistant
        return view_func(request, user, *args, **kwargs)
    return _decorated

def get_object(request, object, id):
    """ Generic dump json method. Debug only.

    Arguments:
    object -- type of object to be requested
    id     -- id of the object

    """
    if object not in ('student', 'course', 'activity', 'attendance'):
        raise Http404

    if object == 'student':
        klass = Student
    elif object == 'course':
        klass = Course
    elif object == 'activity':
        klass = Activity
    elif object == 'attendance':
        klass = Attendance
        
    obj = get_object_or_404(klass, pk=id)
    data = serializers.serialize('json', [obj])
    return HttpResponse(data, mimetype='application/json')

def login(request, qr_code, check_hash):
    """ Validates a login request.
    Arguments:
    qr_code -- authentication code

    """
    try:
        assistant = Assistant.objects.get(code=qr_code)
    except Assistant.DoesNotExist:
        return json_response({"error": "Invalid code"}, failed = True)
    
    if assistant.get_check_hash(request) != check_hash:
         return json_response({"error":"invalid check hash, expected: %s got %s" % (assistant.get_check_hash(request), check_hash)}, failed = True)
    
    courses = [{"name" : c.title, "id": c.id, "abbr" : c.name} for c in assistant.courses.all()]
    response = {
        "user": assistant.id, 
        "name": assistant.name, 
        "courses": courses
    }
    
    return json_response(response)
    
@valid_key
def timetable(request, user, course):
    """ Returns the timetable for the current course. 

    Arguments:
    user        -- user making the request
    course      -- id of the course for which the timetable is extracted
    
    """
    try:
        course = Course.objects.get(id=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)

    assistant = request.assistant
    timetable = dict()
   
    # get the activities for each day 
    for (i, day) in enumerate(DAYS):
        activities = {}
        acts = Activity.objects.filter(course=course).filter(day=i) 
        for a in acts:
            try:
                activities[a.interval].append({"name":a.group.name, "id":a.id})
            except Exception:
                activities[a.interval] = []
            activities[a.interval].append({"name":a.group.name, "id":a.id})
        timetable[day] = activities
    
    return json_response({"timetable" : timetable})


def _group(request, course, group, activity, week):
    """ Dump given group and activity, without checking.
    Used by current_group and group API calls
    """
    # compute day when the respective activity took place so we can show it on the smartphone
    comp_date = datetime.strptime('%d %d 1' % (course.start_year, course.start_week), '%Y %W %w')
    comp_date = comp_date + timedelta(weeks = week - 1, days = activity.day)
    text_date = comp_date.strftime("%a, %d %B")    
    
    if week in course.inactive_as_list:
        return json_response({
            "name": group.name, 
            "students": [], 
            "activity_id":activity.id, 
            "max_weeks": course.max_weeks,
            "week": week, 
            "inactive_weeks" : course.inactive_as_list, 
            "date": text_date
        })
    
    students = []
    for s in group.students.all():
        attendance, created = Attendance.objects.get_or_create(course = course, student = s, activity = activity, week = week, defaults={'grade': 0})
        students.append({"name": s.name, 
            "grade": int(attendance.grade),
            "id": s.id,
            "avatar": s.avatar})

    return json_response({
        "name": group.name, 
        "students": students, 
        "activity_id": activity.id, 
        "max_weeks": course.max_weeks,
        "week": week, 
        "inactive_weeks" : course.inactive_as_list, 
        "date": text_date
    })
    
@valid_key
def group(request, user, name, course, activity_id, week = None):
    """ Returns a certain group from a certain course 

    Arguments:
    user        -- user making the request
    name        -- name of the group requested
    course      -- id of the course used
    activity_id -- id of the activity for which the group data is requested
    week        -- (optional) week for which the data is requested. If ommited 
                   the current week is used
    """

    assistant = request.assistant
    
    try:
        course = Course.objects.get(id=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    # week checks
    if week is None:
        week = get_week(course)
    else:
        week = int(week)
   
    if week < 1 or week > course.max_weeks:
        return json_response({"error":"The selected week is invalid"}, failed = True)
    
    try:
        act = Activity.objects.get(id = activity_id)
    except Activity.DoesNotExist:
        return json_response({"error":"No such activity"}, failed = True)
    
    try:
        group = Group.objects.get(name=name, course = course)
    except Group.DoesNotExist:
        return json_response({"error": "No such group"}, failed = True)

    return _group(request, course, group, act, week)

@valid_key
def current_group(request, user, course, week = None):
    """ Returns the current group for this course and assistant 
    
    Arguments:
    user        -- user making the request
    course      -- id of the course used
    week        -- (optional) week for which the data is requested. If ommited 
                   the current week is used

    """
    assistant = request.assistant
    now = datetime.now().time() # Atentie: nu da valori corecte (timezone)
    
    try:
        course = Course.objects.get(id=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    if week is None:
        week = get_week(course)
    else:
        week = int(week)
    
    if week < 1 or week > course.max_weeks:
        return json_response({"error":"The selected week is invalid"}, failed = True)
    
    
    today = datetime.today().weekday()
    # get all the where the user is an assistant for this course
    for group in assistant.groups.filter(course = course):
        for act in group.activity_set.all():
            # see if the group activity is taking place now
            start = time(act.time_hour_start, act.time_minute_start)
            end = time(act.time_hour_end, act.time_minute_end)
    
            if today == act.day and start <= now and now <= end:
                # This is the activity I have to return
                return _group(request, course, group, act, week)
        
    return json_response({"error":"no current group"}, failed = True)

@valid_key
def groups(request, user, course):
    assistant = request.assistant
    
    # Fetch the list of groups assigned to this teaching assistant
    activities = [{"name": "%s %s %02d:%02d" % (a.group.name, DAYS_SHORT[a.day % 7], a.time_hour_start, a.time_minute_start), 
            "group": a.group.name,
            "activity_id": a.id} 
                for a in assistant.activities]
                
    return json_response({"activities":activities})
    
@valid_key
def student(request, user, course, id):
    """ Return a student and the attendances for this course

    Arguments:
    user        -- user making the request
    course      -- id of the course used
    id          -- id of the student for which the data is retrieved
    """
    try:
        student = Student.objects.get(pk=id)
    except Student.DoesNotExist:
        return json_response({"error":"No such student"}, failed = True)

    try:
        course = Course.objects.get(pk=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)

    try:
        my_group = student.virtual_group.get(course=course)
        #send all the attendances even though they don't exist yet
        attendances = dict([(i, {"grade":0, "grades": []}) for i in range(1, course.max_weeks+1)])
        activities = my_group.activity_set.all().order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start')    
        for i,act in enumerate(activities):
            current_act = {"activity_id":act.id, "activity_day": act.day, "activity_interval":act.interval}
            atts = act.attendance_set.filter(student=student, course=course)
            for a in atts:
                if a.week < 1 or a.week > course.max_weeks:
                    continue
                grd = int(a.grade)
                attendances[a.week]['grade'] += grd
                attendances[a.week]['grades'].append(grd)
        my_group = my_group.name
    except Group.DoesNotExist:
        my_group = ''
        attendances = {}
        
    return json_response({
        "name": student.name,
        "id": student.id,
        "avatar": student.avatar,
        "group": student.group,
        "virtual_group": my_group,
        "attendances": attendances
    })
        
@valid_key
def search(request, user, course, query):
    """ Search for users having query in name.
    Returns a list of maximum 20 results

    Arguments:
    user        -- user making the request
    course      -- id of the course used
    query       -- string used to query database
    
    """
    try:
        c = Course.objects.get(id=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    limit = 20
    query = query.lower()
    results = []
    for u in Student.objects.all():
        if query in u.name.lower():
            #check if the student is actually in one of the groups in our course
            for group in u.virtual_group.all():
                if group.course == c:
                    results.append(u.info_dict())
                    if len(results) >= limit:
                        break
    
    return json_response({"students": results})


@csrf_exempt
def post_data(request):
    """ Handles the POST requests 

    Expects the folowing in POST data:
    user        -- user making the post
    course      -- current course id
    type        -- type of post request (currently only 'group' is supported)
    contents    -- JSON encoded dictionary containing:
        week        -- optional week number, otherwise current week is used
        name        -- group name
        activity_id -- current activity id for which the grades are saved
        students    -- list of dictionary elements containing
            id          -- id of the student
            grade       -- new grade of the student
    """
    try:
        user = request.POST['user']
        session_key = request.POST['session_key']
        course = request.POST['course']
    except:
        return json_response({"error":"Malformed post request"}, failed = True)

    try:
        assistant = Assistant.objects.get(pk=user)
    except Assistant.DoesNotExist:
        return json_response({"error":"No such user"}, failed = True)
        
    if assistant.get_session_key() != session_key:
        return json_response({"error":"Invalid session key"}, failed = True)
        
    try:
        course = Course.objects.get(id=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    try:
        data = json.loads(request.POST['contents'])
        if request.POST['type'] == 'group':
            try:
                week = int(data['week'])
            except:
                week = get_week(course)

            if week > course.max_weeks:
                return json_response({"error":"The selected week is larger than the number of weeks for this course"}, failed = True)
            
            if week in course.inactive_as_list:
                return json_response({"error":"This week is during the holiday"}, failed = True)
            
            try:
                act = Activity.objects.get(id = int(data['activity_id']))
            except Activity.DoesNotExist:
                return json_response({"error":"Activity not found"}, failed = True)
            #now we try to save the data for each student
            for student in data['students']:
                try: 
                    current_student = Student.objects.get(id = int(student['id']))
                    student['grade'] = float(student['grade'])
                    attendance, created = Attendance.objects.get_or_create(course = course, student = current_student, activity = act, week = week, defaults={'grade': student['grade']})
                    attendance.grade = student['grade']
                    attendance.save()
                except Student.DoesNotExist:
                    return json_response({"error":"Student not found"}, failed = True)
            #if we succeded then we return status ok
            return json_response({})
        else:
            return json_response({"error":"Wrong query type"}, failed = True)
    except Exception as e:
        return json_response({"error":"Malformed query %s" % e}, failed = True)
