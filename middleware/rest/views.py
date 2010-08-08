from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.conf import settings
from django.core import serializers
from django.http import HttpResponse, Http404

from models import Student, Course, Activity, Attendance

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
