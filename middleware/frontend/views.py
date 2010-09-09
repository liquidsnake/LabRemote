# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext


@login_required
def dashboard(request):
    """ Show client dashboard or redirect to select client page"""
    return render_to_response('dashboard.html', 
        context_instance=RequestContext(request),
        )
