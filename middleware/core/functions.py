from datetime import datetime, time

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
