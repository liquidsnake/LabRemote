from django.conf.urls.defaults import *

import middleware.frontend.views as views

urlpatterns = patterns('',
    (r'^$', views.dashboard),
    (r'^students/$', views.students_list),
    (r'^groups/$', views.dashboard), # TODO
    
)
