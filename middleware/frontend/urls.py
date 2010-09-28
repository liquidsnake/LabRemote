from django.conf.urls.defaults import *
import middleware.frontend.views as views
from django.views.generic.create_update import create_object, update_object, delete_object
from django.views.generic.simple import direct_to_template
from middleware.core.models import Activity, Group, Assistant
from middleware.frontend.forms import GroupForm, AssistantForm

urlpatterns = patterns('',
    url(r'^course_select(/(?P<course>\d+))?/$', views.course_select, name="course_select"),
    (r'^$', views.dashboard),
    (r'^form_success/(?P<object>[^/]+)/(?P<operation>[^/]+)(/(?P<id>\d+))?/$', views.form_success),
    url(r'^course/(?P<getcourse>[^/]+)/$', views.dashboard, name="course_selected"),
    (r'^course/(?P<getcourse>[^/]+)/students/$', views.students_list),
    (r'^course/(?P<getcourse>[^/]+)/student/(?P<stud_id>\d+)/$', views.student_profile),
    (r'^course/(?P<getcourse>[^/]+)/timetable/$', views.timetable),
    (r'^course/(?P<getcourse>[^/]+)/groups/$', views.groups_index), 
    (r'^course/(?P<getcourse>[^/]+)/group_students/(?P<group_id>\d+)/$', views.group_students), 
    (r'^course/(?P<getcourse>[^/]+)/export_group_csv/(?P<group_id>\d+)/$', views.export_group_csv), 
    (r'^public/(?P<getcourse>[^/]+)/group/(?P<group_id>\d+)/$', views.public_group_link), 
    (r'^course/(?P<getcourse>[^/]+)/group_student_add/(?P<group_id>\d+)/(?P<stud_id>\d+)/$', views.group_students_add), 
    (r'^course/(?P<getcourse>[^/]+)/group_student_rem/(?P<group_id>\d+)/(?P<stud_id>\d+)/$', views.group_students_rem), 
    
    (r'^course/(?P<getcourse>[^/]+)/assistants/$', views.assistants),
    (r'^course/(?P<getcourse>[^/]+)/assistant/approve/(?P<ass_id>\d+)/$', views.assistant_approve),
    
    #generic view magic. Using generic views to add, update and delete the objects
    (r'^crud/add/activity/$', create_object, {
            'model': Activity, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Activity/create/%(id)s/', 
        }
    ),
    (r'^crud/update/activity/(?P<object_id>\d+)/$', update_object, {
            'model': Activity, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Activity/update/%(id)s/', 
        }
    ),
    (r'^crud/delete/activity/(?P<object_id>\d+)/$', delete_object, {
            'model': Activity, 
            'login_required': True, 
            'template_name': 'object_delete_form.html', 
            'post_delete_redirect': '/form_success/Activity/delete/', 
        }
    ),
    (r'^crud/add/group/$', create_object, {
            'form_class': GroupForm, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Group/create/%(id)s/', 
        }
    ),
    (r'^crud/update/group/(?P<object_id>\d+)/$', update_object, {
            'form_class': GroupForm, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Group/update/%(id)s/', 
        }
    ),
    (r'^crud/delete/group/(?P<object_id>\d+)/$', delete_object, {
            'model': Group, 
            'login_required': True, 
            'template_name': 'object_delete_form.html', 
            'post_delete_redirect': '/form_success/Group/delete/', 
        }
    ),
    # Assistant
    (r'^crud/update/assistant/(?P<object_id>\d+)/$', update_object, {
            'form_class': AssistantForm, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Assistant/update/%(id)s/', 
            'extra_context': {'object_name': 'Assistant'},
        }
    ),
)
