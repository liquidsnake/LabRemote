from django.contrib import admin
from django.contrib.auth.admin import UserAdmin

from models import *

class StudentAdmin(admin.ModelAdmin): pass
class ActivityAdmin(admin.ModelAdmin): pass
class AttendanceAdmin(admin.ModelAdmin): pass
class CourseAdmin(admin.ModelAdmin): pass

admin.site.register(Assistant, StudentAdmin)
admin.site.register(Student, StudentAdmin)
admin.site.register(Activity, ActivityAdmin)
admin.site.register(Attendance, AttendanceAdmin)
admin.site.register(Course, CourseAdmin)
admin.site.register(Group)

class UserProfileInline(admin.StackedInline):
    model = UserProfile

class UserProfileAdmin(UserAdmin):
    inlines = (UserProfileInline,)
    list_display = ('username', 'email', 'first_name', 'last_name', 'is_staff', 'is_active')

admin.site.unregister(User)
admin.site.register(User, UserProfileAdmin)
