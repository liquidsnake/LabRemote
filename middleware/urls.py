import os
from django.conf.urls.defaults import *

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    (r'^', include('middleware.frontend.urls')),
    (r'^api/', include('middleware.rest.urls')),

    # Contrib:
    (r'^accounts/login/$', 'django.contrib.auth.views.login',
                {'template_name': 'accounts/login.html'}),
    (r'^accounts/logout/$', 'django.contrib.auth.views.logout_then_login'),

    (r'^media/(?P<path>.*)$', 'django.views.static.serve',
        {'document_root': os.path.abspath(os.path.join(os.path.dirname(__file__), 'media'))}),

    # Uncomment the next line to enable the admin:
    (r'^admin/', include(admin.site.urls)),
)
