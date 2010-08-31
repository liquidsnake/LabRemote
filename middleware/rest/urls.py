from django.conf.urls.defaults import *

import middleware.rest.views as views

urlpatterns = patterns('',
    (r'^get/(?P<object>[^/]+)/(?P<id>\d+)/$', views.get_object),
    
    # Specific requests
    (r'login/(?P<qr_code>[^/]+)/$', views.login),
    (r'timetable/(?P<user>\d+)/(?P<session_key>\w+)/$', views.timetable),
    (r'group/(?P<user>\d+)/(?P<session_key>\w+)/(?P<name>[^/]+)/', views.group),
    (r'student/(?P<user>\d+)/(?P<session_key>\w+)/(?P<course>\w+)/(?P<id>\d+)/', views.student),
    (r'search/(?P<user>\d+)/(?P<session_key>\w+)/(?P<query>\w+)/', views.search),
)
