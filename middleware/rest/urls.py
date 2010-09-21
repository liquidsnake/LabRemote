from django.conf.urls.defaults import *

import middleware.rest.views as views

urlpatterns = patterns('',
    (r'^get/(?P<object>[^/]+)/(?P<id>\d+)/$', views.get_object),
    
    # Specific requests
    (r'login/(?P<qr_code>[^/]+)/$', views.login),
    (r'timetable/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/$', views.timetable),
    (r'group/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<name>[^/]+)/', views.group),
    (r'current_group/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/', views.current_group),
    (r'student/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<id>\d+)/', views.student),
    (r'search/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/(?P<query>\w+)/', views.search),
    (r'post/(?P<course>\w+)/(?P<user>\d+)/(?P<session_key>\w+)/$', views.post_data),
)
