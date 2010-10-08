from django.conf.urls.defaults import *

import middleware.rest.views as views

urlpatterns = patterns('',
    (r'^get/(?P<object>[^/]+)/(?P<id>\d+)/$', views.get_object),
    
    # Specific requests
    (r'login/(?P<qr_code>[^/]+)/$', views.login),
    (r'timetable/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/$', views.timetable),
    url(r'group/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<name>[^/]+)/(?P<activity_id>\d+)/(?P<week>\d+)/', views.group, name = 'views.group_week'),
    url(r'group/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<name>[^/]+)/(?P<activity_id>\d+)/', views.group, name = 'views.group'),
    url(r'current_group/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<week>\d+)/', views.current_group, name = "views.current_group_week"),
    url(r'current_group/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/', views.current_group, name = "views.current_group"),
    (r'student/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<id>\d+)/', views.student),
    (r'search/(?P<course>\d+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<query>\w+)/', views.search),
    (r'post/$', views.post_data),
)
