from django.conf.urls.defaults import *

import middleware.rest.views as views

urlpatterns = patterns('',
    (r'^get/(?P<object>[^/]+)/(?P<id>\d+)/$', views.get_object),
    
    # Specific requests
    (r'login/(?P<qr_code>[^/]+)/$', views.login),
    (r'timetable/(?P<course>\d+)/(?P<user>\d+)/(?P<check_hash>\w+)/$', views.timetable),
    (r'group/(?P<course>\d+)/(?P<user>\d+)/(?P<name>[^/]+)/(?P<activity_id>\d+)/(?P<week>\d+)/(?P<check_hash>\w+)/', views.group),
    (r'group/(?P<course>\d+)/(?P<user>\d+)/(?P<name>[^/]+)/(?P<activity_id>\d+)/(?P<check_hash>\w+)/', views.group),
    (r'current_group/(?P<course>\d+)/(?P<user>\d+)/(?P<week>\d+)/(?P<check_hash>\w+)/', views.current_group),
    (r'current_group/(?P<course>\d+)/(?P<user>\d+)/(?P<check_hash>\w+)/', views.current_group),
    (r'student/(?P<course>\d+)/(?P<user>\d+)/(?P<id>\d+)/(?P<check_hash>\w+)/', views.student),
    (r'search/(?P<course>\d+)/(?P<user>\d+)/(?P<query>\w+)/(?P<check_hash>\w+)/', views.search),
    (r'post/$', views.post_data),
)
