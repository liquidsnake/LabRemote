from django.conf.urls.defaults import *

import middleware.frontend.views as views

urlpatterns = patterns('',
    (r'^$', views.dashboard),
    (r'^course_select/$', views.course_select),
    (r'^(?P<getcourse>[^/]+)/$', views.dashboard),
    (r'^(?P<getcourse>[^/]+)/students/$', views.students_list),
    (r'^(?P<getcourse>[^/]+)/groups/$', views.groups_index), # TODO
    
)
