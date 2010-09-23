from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse, Http404
import json
import datetime
from django.views.decorators.csrf import csrf_exempt


from models import *

DAYS = ["monday", "tuesday", "wednesday", "thursday", "friday"]

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
    def _decorated(request, user, session_key, *args, **kwargs):
        try:
            assistant = Assistant.objects.get(pk=user)
        except Assistant.DoesNotExist:
            return json_response({"error":"no such user"}, failed = True)
            
        if assistant.get_session_key() != session_key:
            return json_response({"error":"invalid session key"}, failed = True)
            
        request.assistant = assistant
        return view_func(request, user, session_key, *args, **kwargs)
    return _decorated

def get_object(request, object, id):
    """ Generic dump json method. Debug only.

    Arguments:
    object -- type of object to be requested
    id -- id of the object
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

def login(request, qr_code):
    """ Validates a login request.
    Arguments:
    qr_code -- authentication code
    """
    try:
        assistant = Assistant.objects.get(code=qr_code)
    except Assistant.DoesNotExist:
        return json_response({"error": "Invalid code"}, failed = True)

    courses = [c.name for c in assistant.courses.all()]
    response = {"user": assistant.id, "name": assistant.name, "courses": courses}
    
    return json_response(response)
    
@valid_key
def timetable(request, user, session_key, course):
    """ Returns the timetable for the current course. """
    try:
        course = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)

    assistant = request.assistant
    timetable = dict()
    
    for (i, day) in enumerate(DAYS):
        activities = {}
        acts = Activity.objects.filter(course__name=course).filter(day=i) 
        for a in acts:
            try:
                activities[a.interval].append({"name":a.group.name, "id":a.id})
            except Exception:
                activities[a.interval] = []
            activities[a.interval].append({"name":a.group.name, "id":a.id})
        timetable[day] = activities
    
    return json_response({"timetable" : timetable})

@valid_key
def group(request, user, session_key, name, course, activity_id):
    """ Returns a certain group from a certain course """
    assistant = request.assistant
    
    try:
        course = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    try:
        act = Activity.objects.get(id = activity_id)
    except Activity.DoesNotExist:
        return json_response({"error":"No such activity"}, failed = True)
    
    try:
        group = Group.objects.get(name=name, course__name=course)
        students = []
        for s in group.students.all():
            attendance, created = Attendance.objects.get_or_create(course = course, student = s, activity = act, week = get_week(act.day_start), defaults={'grade': 0})
            students.append({"name": s.name, 
                "grade": int(attendance.grade),
                "id": s.id,
                "avatar": s.avatar})
    except Group.DoesNotExist:
        return json_response({"error": "No such group"}, failed = True)
                
    return json_response({"name": name, "students": students, "activity_id":activity_id})

@valid_key
def current_group(request, user, session_key, course):
    """ Returns the current group for this course and assistant """
    assistant = request.assistant
    now = datetime.datetime.now().time()

    try:
        course = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    #get all the where the user is an assistant for this course
    for group in assistant.groups.filter(course__name=course):
        for act in group.activity_set.all():
            #see if the group activity is taking place now
            start = datetime.time(act.time_hour_start, act.time_minute_start)
            end = datetime.time(act.time_hour_end, act.time_minute_end)
            today = datetime.date.today().weekday()
            if today == act.day and start <= now and now <= end:
                students = []
                for s in group.students.all(): 
                    attendance, created = Attendance.objects.get_or_create(course = course, student = s, activity = act, week = get_week(act.day_start), defaults={'grade': 0})
                    students.append({"name": s.name, 
                        "grade": int(attendance.grade),
                        "id": s.id,
                        "avatar": s.avatar})
                return json_response({"name": group.name, "students": students, 'activity_id' : act.id})
    return json_response({"error":"no current group"}, failed = True)
    
@valid_key
def student(request, user, session_key, course, id):
    """ Return a student and the attendances for this course"""
    try:
        student = Student.objects.get(pk=id)
    except Student.DoesNotExist:
        return json_response({"error":"No such student"}, failed = True)

    try:
        course = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)

    try:
        my_group = student.virtual_group.get(course=course)
    except Group.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
        
    
    attendances = {}
    activities = my_group.activity_set.all().order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start')    
    for i,act in enumerate(activities):
        current_act = {"activity_id":act.id, "activity_day": act.day, "activity_interval":act.interval}
        atts = act.attendance_set.filter(student=student, course=course)
        for a in atts:
            grd = int(a.grade)
            if a.week in attendances:
                attendances[a.week]['grade'] += grd
                attendances[a.week]['grades'].append(grd)
            else:
                attendances[a.week] = {"grade":grd, "grades": [grd]}
    
    return json_response({"name": student.name,
        "id": student.id,
        "avatar": student.avatar,
        "virtual_group": my_group.name,
        "attendances": attendances})
        
@valid_key
def search(request, user, session_key, course, query):
    """ Search for users having query in name.
    Returns a list of maximum 20 results """
    
    try:
        c = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)
    
    limit = 20
    # TODO: query db, not this crap
    query = query.lower()
    results = []
    for u in Student.objects.all():
        if query in u.name.lower():
            #check if the student is actually in one of the groups in our course
            for group in u.virtual_group.all():
                print u, group
                if group.course == c:
                    results.append(u.info_dict())
                    if len(results) >= limit:
                        break
    
    return json_response({"students": results})

def get_week(start_day):
    delta = datetime.date.today() - start_day
    return max(delta.days / 7, 0)

@csrf_exempt
def post_data(request):
    """ Handles the POST requests """
    try:
        user = request.POST['user']
        session_key = request.POST['session_key']
        course = request.POST['course']
    except:
        return json_response({"error":"Malformed post request"}, failed = True)

    try:
        assistant = Assistant.objects.get(pk=user)
    except Assistant.DoesNotExist:
        return json_response({"error":"no such user"}, failed = True)
        
    if assistant.get_session_key() != session_key:
        return json_response({"error":"invalid session key"}, failed = True)
        
    try:
        course = Course.objects.get(name=course)
    except Course.DoesNotExist:
        return json_response({"error":"No such course"}, failed = True)

    try:
        data = json.loads(request.POST['contents'])
        if request.POST['type'] == 'group':
            try:
                act = Activity.objects.get(id = int(data['activity_id']))
            except Activity.DoesNotExist:
                return json_response({"error":"Activity not found"}, failed = True)
            for student in data['students']:
                try: 
                    current_student = Student.objects.get(id = int(student['id']))
                    student['grade'] = float(student['grade'])
                    attendance, created = Attendance.objects.get_or_create(course = course, student = current_student, activity = act, week = get_week(act.day_start), defaults={'grade': student['grade']})
                    attendance.grade = student['grade']
                    attendance.save()
                except Student.DoesNotExist:
                    return json_response({"error":"Student not found"}, failed = True)
            return json_response({})
        else:
            return json_response({"error":"Wrong query type"}, failed = True)
    except Exception:
        return json_response({"error":"Malformed query"}, failed = True)
