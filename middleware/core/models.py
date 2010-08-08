from django.db import models

class Student(models.Model):
    first_name = models.CharField(max_length=64)
    last_name = models.CharField(default='', max_length=64, blank=True)
    
    group = models.CharField(default='', blank=True, max_length=20)
    
    @property
    def name(self):
        return u'%s %s' % (self.first_name, self.last_name)
        
class Course(models.Model):
    name = models.CharField(max_length=64)
    
    students = models.ManyToManyField(Student)
    
class Activity(models.Model):
    course = models.ForeignKey(Course)
    day = models.IntegerField(default=0)
    time_start = models.IntegerField(default=8)
    time_end = models.IntegerField(default=10)
    
class Attendance(models.Model):
    student = models.ForeignKey(Student)
    activity = models.ForeignKey(Activity)
    grade = models.FloatField(default=0)
    week = models.IntegerField(default=0)
    
