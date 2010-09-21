from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse, Http404
import json
import datetime



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
        group = Group.objects.get(name=name, course__name=course)
        
        students = [ {"name": s.name, 
                "grade": 0, # TODO
                "id": s.id,
                "avatar": s.avatar} for s in group.students.all() ]
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
                students = [ {"name": s.name, 
                    "grade": 0, # TODO
                    "id": s.id,
                    "avatar": s.avatar} for s in group.students.all() ]    
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

    atts = Attendance.objects.filter(student=student, course=course)
    attendance = {}
    for a in atts:
        attendance[a.week] = {"grade": a.grade}
    
    return json_response({"name": student.name,
        "grade": 0, #TODO
        "id": student.id,
        "avatar": student.avatar,
        "attendance": attendance})
        
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
    delta = start_day - datetime.date.today()
    return delta.days / 7

@valid_key
def post_data(request):
    """ Returns the timetable for the current course. """
    print "lalalal"
    user = request.POST['user']
    session_key = request.POST['session_key']
    course = request.POST['course']
    print user
    print session_key
    print course
    print request.POST['data']
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
                act = Activity.objects.get(id = data['activity_id'])
            except Activity.DoesNotExist:
                return json_response({"error":"Activity not found"}, failed = True)
            for student in data['students']:
                try: 
                    current_student = Student.objects.get(id = student['id'])
                    attendance = Attendace.objects.get_or_create(course = course, student = current_student, activity = act, week = get_week(act.day_start))
                    attendance.grade = student['grade']
                    attendance.save()
                except Student.DoesNotExist:
                    return json_response({"error":"Student not found"}, failed = True)
            
    except TypeError:
        return json_response({"error":"Malformed post request"}, failed = True)
    
    
    
    return json_response({"timetable" : timetable})
