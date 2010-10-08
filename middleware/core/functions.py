from datetime import datetime, time
from middleware.core.models import Activity

def get_week(course):
    td = datetime.now()
    start_date = datetime.strptime('%d %d 1' % (course.start_year, course.start_week), '%Y %W %w')
    diff = td - start_date
    return diff.days/7 + 1

def get_current_activity(assistant, course):
    now = datetime.now().time()
    
    for group in assistant.groups.filter(course = course):
        for act in group.activity_set.all():
            #see if the group activity is taking place now
            start = time(act.time_hour_start, act.time_minute_start)
            end = time(act.time_hour_end, act.time_minute_end)
            today = datetime.today().weekday()
            if today == act.day and start <= now and now <= end:
                return act
                
    return None

def get_timetable(course):
    """ Return a special timetable object """
    class Day:
        def __init__(self, i, a):
            self.day, self.activities = i,a
    class Hour:
        def __init__(self, i):
            self.id = i
        @property
        def days(self):
            return [Day(i,Activity.objects.filter(course=course).filter(day=i).filter(time_hour_start=self.id)) for i in range(0,7)]
        def __str__(self):
            return "%d" % self.id            
    class Timetable:
        hours = [Hour(i) for i in range(8,20)]        
        def __getattr__(self,name):
            return "%s"%name
    return Timetable()
