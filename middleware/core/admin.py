from django.contrib import admin

from models import *

class StudentAdmin(admin.ModelAdmin): pass
class ActivityAdmin(admin.ModelAdmin): pass
class AttendanceAdmin(admin.ModelAdmin): pass
class CourseAdmin(admin.ModelAdmin): pass

admin.site.register(Student, StudentAdmin)
admin.site.register(Activity, ActivityAdmin)
admin.site.register(Attendance, AttendanceAdmin)
admin.site.register(Course, CourseAdmin)

