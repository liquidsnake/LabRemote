# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext
from django.views.generic.list_detail import object_list

from middleware.core.models import Student

students_list_info = {
    'queryset' :   Student.objects.all(),
    'allow_empty': True, 
    'template_name': 'students_list.html', 
    'extra_context': {
    }
}

@login_required
def dashboard(request):
    """ Show client dashboard or redirect to select client page"""
    return render_to_response('dashboard.html', 
        context_instance=RequestContext(request),
        )      

@login_required
def students_list(request):
    return object_list(request, **students_list_info)
