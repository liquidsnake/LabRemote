# -*- coding: utf-8 -*-
from django.shortcuts import render_to_response, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required, permission_required
from django.template import RequestContext
from django.views.generic.list_detail import object_list
from django.core.paginator import Paginator, InvalidPage, EmptyPage
import views # wtf?
from django.core.urlresolvers import reverse
from django.template.defaultfilters import slugify
from django.db.models import Max, Sum, Q
from django.http import HttpResponse, HttpResponseRedirect
from django import forms

from middleware.core.models import *
from middleware.core.functions import *
from middleware.frontend.forms import RegisterForm, UpdateGradeForm, SearchForm

import csv
import datetime
import unicodedata
import json

def json_response(dct):
    """ A shorthand for dumping json data """
    data = json.dumps(dct)
    return HttpResponse(data, mimetype='application/json')


def course_required(function):
    def _decorated(request, getcourse=0, **kwargs):
        course = request.session.get('course', None)
        if not course:
            return redirect(reverse(views.course_select))
        return function(request, getcourse, **kwargs)
    return _decorated
        
@login_required
@course_required
def dashboard(request, getcourse=0):
    """ Show client dashboard or redirect to select client page"""
    info = {}
    course = request.session.get('course', None)
    course = Course.objects.get(id=course.id)
    
    info['week'] = get_week(course)
    info['current_day'] = datetime.datetime.now().weekday()
    info['actual_week'] = info['week'] + course.start_week
    info['inactive_week'] = info['week'] in course.inactive_as_list
    try:
        info['activity'] = get_current_activity(request.user.get_profile().assistant, course)
    except Exception as e:
        info['activity'] = str(e)
        
    t = get_timetable(course)
    
    return render_to_response('dashboard.html', 
        {'info': info, 'course': course, 'timetable': t},
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
            # Done but if kept you don't get to he import page anymore.
            # maybe move the link somewhere else
            #if len(courses) == 1:
            #    return redirect(reverse('course_selected', args=[courses[0].id]))
        else:
            courses = []
        return render_to_response('course_select.html',
            {'courses': courses, 'hide_menu': True},
            context_instance=RequestContext(request))
   
    course = get_object_or_404(Course, pk=course)
    if not request.user.is_staff:
        profile = request.user.get_profile()
        if not profile.assistant or (course not in profile.assistant.courses.all()):
            return redirect('/dev-null') # TODO error
    
    request.session['course'] = course
    return redirect(reverse("course_selected", args=[course.id]))
    
@login_required
def import_course(request):
    """ Wizard for importing a course """
    step = request.GET.get('step', 1)
    step = int(step)
    
    if step == 1:
        assistants = Assistant.objects.all()
        return render_to_response('import_course.html',
            {'step': step, 'assistants': assistants},
            context_instance=RequestContext(request))            
    if step == 2:
        id = request.GET.get('a', 0)
        a = Assistant.objects.get(pk=id)
        # Start import
        # Run middleware.worker for now and capture output
        # import required
        import os, subprocess
        class FakeCourse(object):
            def __init__(self):
                self.id, self.name = 0,''
        error, courses, courses2 = None, [], {}
        worker_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'worker.py'))
        
        output = subprocess.Popen(["/usr/bin/python", worker_path, a.moodle_user, a.moodle_password, a.moodle_url], 
                    stdout=subprocess.PIPE).communicate()[0]
        
        lines = output.split("\n")
        if lines[0] != "Ok":
            error = lines[0]
        else:
            for i in lines[1:]:
                if not i: break
                c = FakeCourse()
                parts = i.split(' ')
                c.id = int(parts[0])
                c.name = ' '.join(parts[1:])
                courses.append(c)
                courses2[c.id] = c.name
                
        request.session['courses'] = courses2
                
        return render_to_response('import_course.html',
            {'step': step, 'error': error, 'courses': courses, 'assistant': a},
            context_instance=RequestContext(request))
    if step == 3:
        id = request.GET.get('a', 0)
        a = Assistant.objects.get(pk=id)
        course_id = int(request.GET.get('c', -1))
        
        try:
            a.moodle_course_id = course_id
            a.save()
        except: pass
        
        # Now import students and groups
        import os, subprocess
        error, groups = None, None
        worker_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'update.py'))
        
        output = subprocess.Popen(["/usr/bin/python", worker_path, str(a.id)], 
                    stdout=subprocess.PIPE).communicate()[0]
        
    return render_to_response('import_course.html',
            {'step': 3},
            context_instance=RequestContext(request))

@login_required
@course_required
def students_list(request, getcourse):
    """Shows a paginated list of imported students"""
    course = request.session.get('course', None)
    request.csrf_processing_done = True
    query = request.GET.get('query', '')
    
    if query: 
        form = SearchForm(request.GET) 
        if form.is_valid():
            students_list = Student.objects.filter(course=course).filter(
                    Q(first_name__icontains=form.cleaned_data['query']) | 
                    Q(last_name__icontains=form.cleaned_data['query']) |
                    Q(group__icontains=form.cleaned_data['query'])
            )
    else:
        form = SearchForm() # An unbound form
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
            {"students": students,
             "form": form,
             "query": query,
            },
            context_instance=RequestContext(request),)
            
@login_required
@course_required
def student_profile(request, getcourse, stud_id):
    """Shows the profile of a student"""
    course = request.session.get('course', None)

    student = Student.objects.get(id=stud_id)

    s_groups = [elem["id"] for elem in student.virtual_group.all().values("id")]
    activities = Activity.objects.filter(course = course).filter(group__pk__in = s_groups)
    
    weeks = []
    for week in range(1,course.max_weeks+1):
        current_week = {
            "week" : week,
            "grades" : [0 for i in range(activities.count())],
        }
        weeks.append(current_week)        

    for i,activity in enumerate(activities):
        for attendance in Attendance.objects.filter(student = student, course = course, activity = activity).order_by("week"):
            weeks[attendance.week - 1]["grades"][i] = int(attendance.grade)
 
    return render_to_response('student_profile.html', 
            {"activities" : activities,
             "weeks" : weeks,
             "student" : student,
             "course" : course,
             "inactive" : course.inactive_as_list,
            },
            context_instance=RequestContext(request),)

def _assistants(request):
    """Shows a list of assistants and details about them"""
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
def assistants(request, getcourse):
    return _assistants(request)
    
@login_required
@course_required
def courses(request, getcourse):
    """Shows a list of all the courses"""
    courses = Course.objects.all()
    
    return render_to_response('courses.html', 
            {"courses": courses},
            context_instance=RequestContext(request),)

@login_required
@course_required
def assistant_approve(request, getcourse, ass_id):
    """Approves or disapproves an assistant"""
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
    """Shows all the students from a group"""
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
def group_view(request, getcourse, group_id):
    """Shows a single group"""
    group = Group.objects.get(id=group_id)
            
    return render_to_response('group_view.html',
        {'group': group},
        context_instance=RequestContext(request),
        )
        
@login_required
@course_required
def group_students_add(request, getcourse, group_id, stud_id):
    """Adds a student to a certain group"""
    group = Group.objects.get(id=group_id)
    student = Student.objects.get(id=stud_id)
    group.students.add(student)
    return redirect(reverse(group_students, args=[getcourse, group_id]))

@login_required
@course_required    
def group_students_rem(request, getcourse, group_id, stud_id):
    """Removes a student from the group"""
    group = Group.objects.get(id=group_id)
    student = Student.objects.get(id=stud_id)
    group.students.remove(student)
    return redirect(reverse(group_students, args=[getcourse, group_id]))

def group_edit(request, getcourse, group_id):
    """Edit the attendances of a group. Uses YUI for asyncronous and seamless saving of the grades. """
    course = request.session.get('course', None)
    #Get the course as saving it in the session has some problems
    course = Course.objects.get(id=course.id)
    group = get_object_or_404(Group, id=group_id)

    students = group.students.all() 
    activities = group.activity_set.order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start').all()
    saved_activities = []
    for activity in activities:
        saved_activity = {}
        saved_activity['interval'] = activity.interval
        saved_activity['day'] = activity.day_of_the_week
        saved_activity['activity'] = activity
        #get the maximum attendance week
        saved_activities.append(saved_activity)

    #show the weeks in human readable form
    week_legend = []
    c_week = datetime.datetime.strptime('%d %d 1' % (course.start_year, course.start_week), '%Y %W %w')
    for i in range(course.max_weeks):
        next_date = c_week + timedelta(weeks = 1)
        end_date = next_date.strftime("%d-%m-%Y")
        start_date = c_week.strftime("%d-%m-%Y")
        week_legend.append({'week': i+1, 'week_m1': i, 'start_date': start_date, 'end_date': end_date})
        c_week = next_date
                    
    return render_to_response('group_edit.html',
        {'saved_activities': saved_activities,
         'group_name' : group.name,
         'group' : group,
         'course' : course,
         'students': students,
         'weeks' : range(1,course.max_weeks+1),
         'week_legend' : week_legend,
         'inactive' : course.inactive_as_list,
        },
        context_instance=RequestContext(request),
        )   

@login_required
@course_required
def get_activity(request, getcourse, activity_id):
    """Gets the student names and grades for an activity. Used by the group edit view """
    course = request.session.get('course', None)
    #TODO error checking
    student_list = []
    try:
        activity = Activity.objects.get(id = activity_id) 
        student_list = activity.group.students.all()
    except Activity.DoesNotExist:
        pass
    students = []        
    for student in student_list:
        max_w = course.max_weeks
        attendances = Attendance.objects.filter(student = student, course = course, activity = activity)
        student_data = {"name": student.name}
        student_data["student_id"] = student.id
        student_data["image"] = student.avatar
        sum = 0
        for i in range(1, max_w+1):
            try:
                grade = attendances.get(week = i).grade
                student_data["week_%d" % i] = grade
                sum += grade
            except Attendance.DoesNotExist:
                student_data["week_%d" % i] = 0
        student_data["sum"] = sum
        students.append(student_data)

    return json_response({"Results": students})

@login_required
@course_required
def update_grade(request, getcourse, activity_id, student_id, week):
    """Call that saves the grade of a student. Used for asyncronous saving by the group edit view """
    form = UpdateGradeForm(request.POST)
    new_grade = 0
    if form.is_valid():
        new_grade = form.cleaned_data['new_grade']
    else:
        pass
    try:
        student = Student.objects.get(id = student_id)
    except Student.DoesNotExist:
        return HttpResponse("No such student")
    try:    
        activity = Activity.objects.get(id = activity_id)
    except Activity.DoesNotExist:
        return HttpResponse("No such activity")
    try:
        course = Course.objects.get(id = getcourse)
    except Course.DoesNotExist:
        return HttpResponse("No such course")
    week = int(week)
    if week > course.max_weeks or week < 1:
        return HttpResponse("Invalid week")
    if new_grade < 0:
        return HttpResponse("Invalid grade")
    
    attendance, created = Attendance.objects.get_or_create(course = course, activity = activity, student = student, week = week, defaults={'grade': new_grade})
    attendance.grade = new_grade
    attendance.save()
    return HttpResponse("")

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
    possible_objects['Course'] = Course

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
    """Exports data about a course in CSV format"""
    course = request.session.get('course', None)
    group = get_object_or_404(Group, id=group_id)
    response = HttpResponse(mimetype='text/csv')
    response['Content-Disposition'] = 'attachment; filename=%s-group.csv' % slugify(group.name)

    writer = csv.writer(response, delimiter=',')

    writer.writerow(['Group %s' % group.name])

    students = group.students.all() 
    activities = group.activity_set.order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start').all()
    for activity in activities:
        writer.writerow(["Activity during %s on %s" % (activity.interval, activity.day_of_the_week)])
        #get the maximum attendance week
        max = course.max_weeks
        l = range(max+1)
        l[0] = ''
        l.extend(['', 'Sum'])
        writer.writerow(l)
        for student in students:
            attendances = Attendance.objects.filter(student = student, course = course, activity = activity)
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
        grade = Attendance.objects.filter(student = student, course = course).aggregate(Sum('grade'))['grade__sum']
        if grade == None:
            grade = 0
        atts = [unicodedata.normalize('NFKD', student.name).encode('ascii','ignore'), grade]
        writer.writerow(atts)
    writer.writerow([])
                    
    return response

def public_group_link(request, getcourse, group_id):
    """Public page to be shown to students containing the grades."""
    course = get_object_or_404(Course, id=getcourse)
    group = get_object_or_404(Group, id=group_id)

    students = group.students.all() 
    activities = group.activity_set.order_by('day', 'time_hour_start', 'time_hour_end', 'time_minute_start').all()
    saved_activities = []
    
    max = course.max_weeks

    for activity in activities:
        saved_activity = {}
        saved_activity['interval'] = activity.interval
        saved_activity['day'] = activity.day_of_the_week
        #get the maximum attendance week
        saved_activity['students'] = []
        for student in students:
            attendances = Attendance.objects.filter(student = student, course = course, activity = activity)
            #grades for this student for this activity
            atts = []
            sum = 0
            for i in range(1, max+1):
                try:
                    grade = int(attendances.get(week = i).grade)
                    atts.append(grade)
                    sum += grade
                except Attendance.DoesNotExist:
                    atts.append(0)
            saved_activity['students'].append({
                'name' : unicodedata.normalize('NFKD', student.name).encode('ascii','ignore'), 
                'attendances' : atts,
                'sum' : sum,
            })
        saved_activities.append(saved_activity)
    
    total_grades = []
    for student in students:
        grade = Attendance.objects.filter(student = student, course = course).aggregate(Sum('grade'))['grade__sum']
        if grade == None:
            grade = 0
        total_grades.append({'name':unicodedata.normalize('NFKD', student.name).encode('ascii','ignore'), 'grade':grade})
                    
    return render_to_response('public_group.html',
        {'saved_activities': saved_activities,
         'total_grades' : total_grades,
         'group_name' : group.name,
         'weeks' : range(1,course.max_weeks+1),
         'inactive' : course.inactive_as_list,
        },
        context_instance=RequestContext(request),
        )    

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
            #print "profile", profile, assistant, profile.assistant
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

@login_required
@course_required
def timetable(request, getcourse):
    """ Show all the current activities for a course."""
    course = request.session.get('course', None)
    #defines for the way the timetable will look like:
    #hour height in pixels
    hour_height = 40
    #minimum hour (24 hour format)
    min_hour = 8
    #max hour
    max_hour = 20
    #how many hours between lines
    interval = 2 
   
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
        pos_start = (act.time_hour_start + act.time_minute_start/60.0 - min_hour) * hour_height
        height = ((act.time_hour_end + act.time_minute_end /60) - (act.time_hour_start + act.time_minute_start/60)) * hour_height
        activities_per_day[act.day][1].append({'activity':act, 'position_start': pos_start, 'height':height})
    
    return render_to_response('timetable.html',
        {'activities_per_day': activities_per_day,
         'days': days,
         'min_hour': min_hour,
         'max_hour': max_hour,
         'range': range((min_hour + interval), max_hour, interval), #the first row is put manually
         'rows': (max_hour-min_hour)/interval,
         'interval_height': hour_height * interval,
        },
        context_instance=RequestContext(request),
        )     
