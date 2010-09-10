from django.conf.urls.defaults import *

import middleware.frontend.views as views
from django.views.generic.create_update import create_object
from django.views.generic.simple import direct_to_template
from middleware.core.models import Activity

urlpatterns = patterns('',
    (r'^$', views.dashboard),
    (r'^form_success/(?P<object>[^/]+)/(?P<id>\d+)/$', views.form_success),
    (r'^(?P<getcourse>[^/]+)/$', views.dashboard),
    (r'^(?P<getcourse>[^/]+)/students/$', views.students_list),
    (r'^(?P<getcourse>[^/]+)/timetable/$', views.timetable),
    (r'^course_select/$', views.course_select),
    (r'^crud/add/activity/$', create_object, {
            'model': Activity, 
            'login_required': True, 
            'template_name': 'activity_form.html', 
            'post_save_redirect': '/form_success/Activity/%(id)s/', 
            'extra_context': {'test': 'hey'},
        }
    ),
    (r'^(?P<getcourse>[^/]+)/groups/$', views.groups_index), # TODO
)
