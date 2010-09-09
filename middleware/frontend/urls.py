from django.conf.urls.defaults import *

import middleware.frontend.views as views

urlpatterns = patterns('',
    (r'^$', views.dashboard),
    
)
