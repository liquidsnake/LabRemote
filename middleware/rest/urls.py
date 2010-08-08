from django.conf.urls.defaults import *

import views

urlpatterns = patterns('',
    (r'^get/(?P<object>[^/]+)/(?P<id>\d+)/$', views.get_object),
)
