#!/usr/bin/env python

"""
An updater script for keeping moodle and lr-middleware in sync

"""

import os, sys

from scrapper.moodle import MoodleSession, LoginError

from django.core.management import setup_environ
import settings
setup_environ(settings)

from core.models import Assistant, Student, Course

def main(args):
    """ Iterate through assistants and scrap their moodle """
    if len(args) < 1:
        todos = Assistant.objects.all()
    else:
        todos = []
        for a in args[1:]:
            try:
                assistant = Assistant.objects.get(pk=int(a))
                todos.append(assistant)
            except: 
                sys.stderr.write('Could not load assistant: %s' % a)
    
    # Execute todos
    for a in todos:
        if not a.is_updater:
            continue
        
        session = MoodleSession(str(a.moodle_url))
        session.set_verbosity(0)
        try:
            session.login(a.moodle_user, a.moodle_password)
        except LoginError:
            sys.stderr.write("Failed to connect.\n")
            sys.exit(1)
        
        if not session.answered("Overview of my courses"):
            sys.stderr.write("Unexpected page (%d bytes)\n" % len(session.body()))
            sys.exit(-2)
        else:
            course_id = int(a.moodle_course_id)
            try:
                course = Course.objects.get(external_id=course_id)
            except:
                courses = session.list_courses()
                course = None
                for c in courses:
                    if c[0] == course_id:
                        course = Course(external_id=course_id, title=c[1], name=c[1])
                        course.save()
                        break
                if course is None:
                    sys.stderr.write("Could not get course: %d " % course_id)
                    continue
                
            groups = session.list_groups(int(a.moodle_course_id))
            
            for key,g in groups.items():
                for user in g['users']:
                    try:
                        student = Student.objects.get(external_id=user[0])
                    except:
                        student = Student(external_id=user[0])
                        
                    if student.first_name != user[1] or \
                        student.avatar != user[2] or \
                        student.group != g['name']:
                            student.first_name = user[1]
                            student.avatar = user[2]
                            student.group = g['name']
                            student.save()
                            
                    if course.students.filter(id=student.id).count() == 0:
                        # Add student to course
                        course.students.add(student)
                            
            print "Updated course: ", a.moodle_course_id

if __name__ == "__main__":
    sys.exit(main(sys.argv))
    
