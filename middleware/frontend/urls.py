from django.conf.urls.defaults import *

import middleware.frontend.views as views
from django.views.generic.create_update import create_object, update_object, delete_object
from django.views.generic.simple import direct_to_template
from middleware.core.models import Activity

urlpatterns = patterns('',
    (r'^$', views.dashboard),
    (r'^form_success/(?P<object>[^/]+)/(?P<operation>[^/]+)(/(?P<id>\d+))?/$', views.form_success),
    (r'^(?P<getcourse>[^/]+)/$', views.dashboard),
    (r'^(?P<getcourse>[^/]+)/students/$', views.students_list),
    (r'^(?P<getcourse>[^/]+)/timetable/$', views.timetable),
    (r'^course_select/$', views.course_select),
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
    (r'^(?P<getcourse>[^/]+)/groups/$', views.groups_index), # TODO
)
