from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse, Http404
import json

from models import *

DAYS = ["monday", "tuesday", "wednesday", "thursday", "friday"]

def json_response(obj):
    """ A shorthand for dumping json data """
    data = json.dumps(obj)
    return HttpResponse(data, mimetype='application/json')
    
def valid_key(view_func):
    """ A decorator checking the key for validity """
    def _decorated(request, user, session_key, *args, **kwargs):
        try:
            assistant = Assistant.objects.get(pk=user)
        except Assistant.DoesNotExist:
            return json_response({"error":"no such user"})
            
        if assistant.get_session_key() != session_key:
            return json_response({"error":"invalid session key"})
            
        request.assistant = assistant
        return view_func(request, user, session_key, *args, **kwargs)
    return _decorated

def get_object(request, object, id):
    """ Generic dump json method """
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
    """ Validates a login request """
    try:
        assistant = Assistant.objects.get(code=qr_code)
    except Assistant.DoesNotExist:
        return json_response({"login": "invalid"})

    courses = [c.name for c in assistant.courses.all()]
    response = {"login": "ok", "user": assistant.id, "name": assistant.name, "courses": courses}
    
    return json_response(response)
    
@valid_key
def timetable(request, user, session_key):
    assistant = request.assistant
    timetable = dict()
    
    for (i, day) in enumerate(DAYS):
        activities = []
        for c in assistant.courses.all():
            acts = Activity.objects.filter(course=c, day=i)
            acts = [{a.interval:a.group.name} for a in acts]
            activities.extend(acts)
        timetable[day] = activities

    return json_response({"timetable" : timetable})

@valid_key
def group(request, user, session_key, name):
    assistant = request.assistant
    
    group = get_object_or_404(Group, name=name)
    students = [ {"name": s.name, 
                "grade": 0, # TODO
                "avatar": s.avatar} for s in group.students.all() ]
                
    return json_response({"name": name, "students": students})
                
