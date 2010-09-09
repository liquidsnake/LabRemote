import hashlib
from django.db import models
from django.contrib.auth.models import User

class UserProfile(models.Model):
    user = models.ForeignKey(User, unique=True)    
    
    assistant = models.ForeignKey('Assistant', default=None, blank=True, null=True)

class Student(models.Model):
    first_name = models.CharField(max_length=64)
    last_name = models.CharField(default='', max_length=64, blank=True)
    
    group = models.CharField(default='', blank=True, max_length=20)
    avatar = models.URLField(verify_exists=False, default='', blank=True)
    
    @property
    def name(self):
        return u'%s %s' % (self.first_name, self.last_name)
        
    def info_dict(self):
        return {"id": self.id,
            "name": self.name,
            "avatar": self.avatar}
            
    def __unicode__(self):
        return self.name

class Assistant(Student):
    code = models.CharField(max_length=128, default='')
    courses = models.ManyToManyField('Course', blank=True)
    groups = models.ManyToManyField('Group', blank=True)
    
    # Wheter this assistant can add courses/moodle credentials for scrapping
    is_updater = models.BooleanField(default=False)
    moodle_url = models.CharField(max_length=128, default='http://', blank=True)
    moodle_user = models.CharField(max_length=128, blank=True)
    moodle_password = models.CharField(max_length=128, blank=True)
    moodle_course_id = models.CharField(max_length=32, default='0', blank=True)
    
    def get_session_key(self):
        """ Return an md5 hash built form code + date salt """
        #computed_hash = hashlib.sha256(self.code).hexdigest()
        computed_hash = self.code # TODO change this before release
        return computed_hash

class Course(models.Model):
    name = models.CharField(max_length=64)
    
    students = models.ManyToManyField(Student)
    
    def __unicode__(self):
        return u"%s" % self.name
    
class Group(models.Model):
    """ This is a managed, virtual group. Not a moodle one """
    parent_group = models.CharField(default='', max_length=64, blank=True)
    name = models.CharField(max_length=64)
    
    students = models.ManyToManyField(Student,related_name='virtual_group', blank=True)
    
    def __unicode__(self):
        return u"%s (%s)" % (self.name, self.parent_group)

class Activity(models.Model):
    course = models.ForeignKey(Course)
    group = models.ForeignKey(Group, default=None, blank=True)
    
    day = models.IntegerField(default=0)
    time_start = models.IntegerField(default=8)
    time_end = models.IntegerField(default=10)
    
    @property
    def interval(self):
        return u"%s-%s" % (self.time_start, self.time_end)
        
    def __unicode__(self):
        return u"%s:%s %d %s" % (self.course, self.group, self.day + 1, self.interval)

class Attendance(models.Model):
    student = models.ForeignKey(Student)
    course = models.ForeignKey(Course)
    activity = models.ForeignKey(Activity)
    grade = models.FloatField(default=0, blank=True)
    week = models.IntegerField(default=0, blank=True)
    
    def __unicode__(self):
        return u"%s, %s week: %d" % (self.student, self.course, self.week)
        
def user_post_save(sender, instance, **kwargs):
    profile, new = UserProfile.objects.get_or_create(user=instance)

# Automatic creation of profile
models.signals.post_save.connect(user_post_save, sender=User)

