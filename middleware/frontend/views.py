# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext
from django.views.generic.list_detail import object_list
from django.core.paginator import Paginator, InvalidPage, EmptyPage
import views
from django.core.urlresolvers import reverse
from django.template.defaultfilters import slugify
from django.db.models import Max, Sum
from django.http import HttpResponse, HttpResponseRedirect

from middleware.core.models import *
from middleware.frontend.forms import RegisterForm

import csv
import unicodedata

students_list_info = {
    'queryset' :   Student.objects.all(),
    'allow_empty': True, 
    'template_name': 'students_list.html', 
    'extra_context': {
    }
}

def course_required(function):
    def _decorated(request, getcourse='', **kwargs):
        course = request.session.get('course', None)
        if not course:
            return redirect(reverse(views.course_select))
        return function(request, getcourse, **kwargs)
    return _decorated
        
@login_required
@course_required
def dashboard(request, getcourse=''):
    """ Show client dashboard or redirect to select client page"""
    return render_to_response('dashboard.html', 
        context_instance=RequestContext(request),
        )      

@login_required
def course_select(request, course):
    """ List clients and select one """
    if not course:
        assistant = request.user.get_profile().assistant
        approved = request.user.get_profile().approved
        
        if request.user.is_staff:
            courses = Course.objects.all()
        elif not approved:
            courses = []
        elif assistant:
            courses = assistant.courses.all()
            # TODO: if there's only one course, redirect to it
        else:
            courses = []
        return render_to_response('course_select.html',
            {'courses': courses},
            context_instance=RequestContext(request))
   
    course = get_object_or_404(Course, pk=course)
    if not request.user.is_staff:
        profile = request.user.get_profile()
        if not profile.assistant or (course not in profile.assistant.courses.all()):
            return redirect('/dev-null') # TODO error
    
    request.session['course'] = course
    return redirect(reverse("course_selected", args=[course.name]))
    
@login_required
@course_required
def students_list(request, getcourse):
    course = request.session.get('course', None)
    
    students_list = Student.objects.filter(course=course)
    paginator = Paginator(students_list, 25) # Show 25 contacts per page

    try:
        page = int(request.GET.get('page', '1'))
    except ValueError:
        page = 1

    try:
        students = paginator.page(page)
    except (EmptyPage, InvalidPage):
        students = paginator.page(paginator.num_pages)
    
    return render_to_response('students_list.html', 
            {"students": students},
            context_instance=RequestContext(request),)
            
@login_required
@course_required
def student_profile(request, getcourse, stud_id):
    course = request.session.get('course', None)
    
    student = Student.objects.get(id=stud_id)
    return render_to_response('student_profile.html', 
            {"student": student},
            context_instance=RequestContext(request),)
            
@login_required
@course_required
def assistants(request, getcourse):
    return _assistants(request)
    
def _assistants(request):
    assistants = Assistant.objects.all()
    
    for a in assistants:
        a.profile = UserProfile.objects.filter(assistant=a)
        if a.profile:
            a.profile = a.profile[0]
        else:
            a.profile = None
    
    return render_to_response('assistants.html', 
            {"assistants": assistants},
            context_instance=RequestContext(request),)

@login_required
@course_required
def assistant_approve(request, getcourse, ass_id):
    if not request.user.is_staff:
        return redirect('/')
        
    assistant = Assistant.objects.get(pk=ass_id)
    
    # Get user profile(s) pointing here
    profile = UserProfile.objects.filter(assistant=assistant)
    for p in profile:
        p.approved = not p.approved
        p.save()
        
    assistants = Assistant.objects.all()
    
    return _assistants(request)

@login_required
@course_required
def groups_index(request, getcourse):
    """ Show all grups """
    course = request.session.get('course', None)
        
    moodle_groups = course.get_groups()
    groups = list(Group.objects.filter(course=course))
    
    return render_to_response('groups.html',
        {'moodle_groups': moodle_groups,
        'groups': groups},
        context_instance=RequestContext(request),
        )  
        
@login_required
@course_required
def group_students(request, getcourse, group_id):
    group = Group.objects.get(id=group_id)
    students = Student.objects.filter(group=group.parent_group).all()
    
    # Remove existing from available
    available_students = []
    for a in students:
        if a not in group.students.all():
            available_students.append(a)
            
    return render_to_response('group_students.html',
        {'group': group, 'available_students': available_students},
        context_instance=RequestContext(request),
        )
        
@login_required
@course_required
def group_students_add(request, getcourse, group_id, stud_id):
    group = Group.objects.get(id=group_id)
    student = Student.objects.get(id=stud_id)
    group.students.add(student)
    return redirect(reverse(group_students, args=[getcourse, group_id]))

@login_required
@course_required    
def group_students_rem(request, getcourse, group_id, stud_id):
    group = Group.objects.get(id=group_id)
    student = Student.objects.get(id=stud_id)
    group.students.remove(student)
    return redirect(reverse(group_students, args=[getcourse, group_id]))

@login_required
@course_required
def timetable(request, getcourse):
    course = request.session.get('course', None)
   
    #array for the days of the week
    days = [
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
            'Sunday'
        ]
    
    #get all activities for the current course ordered by the time they take place
    activities = Activity.objects.filter(course=course).order_by('time_hour_start', 'time_hour_end', 'time_minute_start', 'time_minute_end')
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
    """ Generic view to be shown after an opperation 
    Arguments:
    object -- type of object on which the operation was done
    operation -- type of operation (create, update, delete)
    id -- id of object affected (not used for delete)
    """
    possible_objects={}
    possible_objects['Activity'] = Activity
    possible_objects['Group'] = Group
    possible_objects['Assistant'] = Assistant

    possible_operations = ['create', 'update', 'delete']

    #implemented list of object that can be crud'd
    if object not in possible_objects or operation not in possible_operations:
        return redirect('/')
    
    #show information regarding the object if the operation was not deletion
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

@login_required
@course_required
def export_group_csv(request, getcourse, group_id):
    #TODO: check for inexistent group
    group = Group.objects.get(id=group_id)
    response = HttpResponse(mimetype='text/csv')
    response['Content-Disposition'] = 'attachment; filename=%s-group.csv' % slugify(group.name)

    writer = csv.writer(response, delimiter=',')

    writer.writerow(['Group %s' % group.name])

    students = group.students.all() 
    activities = group.activity_set.order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start').all()
    for activity in activities:
        writer.writerow(["Activity during %s on %s" % (activity.interval, activity.day_of_the_week)])
        #get the maximum attendance week
        max = Attendance.objects.filter(course__name = getcourse, activity = activity).aggregate(Max('week'))['week__max']
        l = range(max+1)
        l[0] = ''
        l.extend(['', 'Sum'])
        writer.writerow(l)
        for student in students:
            attendances = Attendance.objects.filter(student = student, course__name = getcourse, activity = activity)
            #grades for this student for this activity
            atts = [unicodedata.normalize('NFKD', student.name).encode('ascii','ignore')]
            sum = 0
            for i in range(1, max+1):
                try:
                    grade = attendances.get(week = i).grade
                    atts.append(grade)
                    sum += grade
                except Attendance.DoesNotExist:
                    atts.append(0)
            atts.extend(['', sum])
            writer.writerow(atts)
        writer.writerow([])
    
    writer.writerow(['Sum during all the activities'])
    for student in students:
        grade = Attendance.objects.filter(student = student, course__name = getcourse).aggregate(Sum('grade'))['grade__sum']
        if grade == None:
            grade = 0
        atts = [unicodedata.normalize('NFKD', student.name).encode('ascii','ignore'), grade]
        writer.writerow(atts)
    writer.writerow([])
                    
    return response

def register(request):
    """ Handle user registration """
    if request.method == 'POST':
        form = RegisterForm(request.POST)
        if form.is_valid():
            data = form.cleaned_data
            newuser = User.objects.create_user(username=data['username'],
                        password=data['password1'],
                        email=data['email'])
            newuser.first_name=data['first_name']
            newuser.last_name=data['last_name']
            newuser.save()
            profile = newuser.get_profile()
            # Attach an assistant
            assistant = Assistant(first_name=data['first_name'],
                        last_name=data['last_name'])
            assistant.save()
            profile.assistant = assistant
            profile.save()
            print "profile", profile, assistant, profile.assistant
            return HttpResponseRedirect("/accounts/created/")
    else:
        form = RegisterForm()

    return render_to_response("accounts/register.html", {
        'form' : form
        },
        context_instance=RequestContext(request)
    )

def created(request):
    return render_to_response("accounts/created.html")
