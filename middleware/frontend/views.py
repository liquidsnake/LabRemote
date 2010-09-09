# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext
from django.views.generic.list_detail import object_list

from middleware.core.models import Student, Group, Course

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
        return redirect('/course_select/')
    
    return render_to_response('dashboard.html', 
        context_instance=RequestContext(request),
        )      

@login_required
def course_select(request):
    """ List clients and select one """
    course = request.GET.get('course', None)
    
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
    return redirect('/%s/' % course.name)
    
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
