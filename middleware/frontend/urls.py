from django.conf.urls.defaults import *
import middleware.frontend.views as views
from django.views.generic.create_update import create_object, update_object, delete_object
from django.views.generic.simple import direct_to_template
from middleware.core.models import Activity, Group

urlpatterns = patterns('',
    url(r'^course_select(/(?P<course>\d+))?/$', views.course_select, name="course_select"),
    (r'^$', views.dashboard),
    (r'^form_success/(?P<object>[^/]+)/(?P<operation>[^/]+)(/(?P<id>\d+))?/$', views.form_success),
    url(r'^course/(?P<getcourse>[^/]+)/$', views.dashboard, name="course_selected"),
    (r'^course/(?P<getcourse>[^/]+)/students/$', views.students_list),
    (r'^course/(?P<getcourse>[^/]+)/timetable/$', views.timetable),
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
            'model': Group, 
            'login_required': True, 
            'template_name': 'object_form.html', 
            'post_save_redirect': '/form_success/Group/create/%(id)s/', 
        }
    ),
    (r'^crud/update/group/(?P<object_id>\d+)/$', update_object, {
            'model': Group, 
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
    (r'^(?P<getcourse>[^/]+)/groups/$', views.groups_index), # TODO
)
