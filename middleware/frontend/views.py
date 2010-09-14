# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext
from django.views.generic.list_detail import object_list
import views
from django.core.urlresolvers import reverse

from middleware.core.models import Student, Group, Course, Activity

students_list_info = {
    'queryset' :   Student.objects.all(),
    'allow_empty': True, 
    'template_name': 'students_list.html', 
    'extra_context': {
    }
}

@login_required
def dashboard(request, getcourse=''):
    """ Show client dashboard or redirect to select client page"""
    course = request.session.get('course', None)
    if not course:
        return redirect(reverse(views.course_select))
    
    return render_to_response('dashboard.html', 
        context_instance=RequestContext(request),
        )      

@login_required
def course_select(request, course):
    """ List clients and select one """
    if not course:
        courses = Course.objects.all()
        return render_to_response('course_select.html',
            {'courses': courses},
            context_instance=RequestContext(request))
   
    course = get_object_or_404(Course, pk=course)
    if not request.user.is_staff:
        profile = request.user.get_profile()
        if not profile.assistant or (course not in profile.assistant.courses):
            return redirect('/dev-null') # TODO error
    
    request.session['course'] = course
    return redirect(reverse("course_selected", args=[course.name]))
    
@login_required
def students_list(request, getcourse):
    course = request.session.get('course', None)
    
    if not course or getcourse != course.name:
        return redirect('/course_select/')
        
    students_list_info['queryset'] = Student.objects.filter(course=course)
    return object_list(request, **students_list_info)

@login_required
def groups_index(request, getcourse):
    course = request.session.get('course', None)
    
    if not course or getcourse != course.name:
        return redirect('/course_select/')
        
    moodle_groups = course.get_groups()
    groups = list(Group.objects.filter(course=course))
    
    return render_to_response('groups.html',
        {'moodle_groups': moodle_groups,
        'groups': groups},
        context_instance=RequestContext(request),
        )    

@login_required
def timetable(request, getcourse):
    course = request.session.get('course', None)
    
    if not course or getcourse != course.name:
        return redirect('/course_select/')
    days = [
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
            'Sunday'
        ]
    
    
    activities = Activity.objects.filter(course=course).order_by('time_hour_end', 'time_hour_end', 'time_minute_start', 'time_minute_end')
    activities_per_day = [(day,[]) for day in days]
    for act in activities:
        activities_per_day[act.day][1].append(act)
    
    return render_to_response('timetable.html',
        {'activities_per_day': activities_per_day,
         'days': days,
        },
        context_instance=RequestContext(request),
        )     


@login_required
def form_success(request, object, operation, id):
    possible_objects={}
    possible_objects['Activity'] = Activity
    possible_objects['Group'] = Group

    possible_operations = ['create', 'update', 'delete']

    #implemented list of object that can be crud'd
    if object not in possible_objects or operation not in possible_operations:
        return redirect('/')
    
    obj = None
    if operation != 'delete':
        obj = get_object_or_404(possible_objects[object], pk=id)

    return render_to_response('form_success.html',
        {'object': obj,
         'operation' : operation,
         'object_type': object,
        },
        context_instance=RequestContext(request),
        )    
