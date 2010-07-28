An Android application to help teaching assistants.
# Overview #

Using this application the assistant can easily take attendance and grade the students using the smartphone.

The application will allow retrieving the student list (name and avatar) according to the timetable, grading the students and exporting/synchronizing the information back to Moodle and/or Google Docs / other interfaces. It will also allow viewing/editing the timetable, group assignment and attendance from the smartphone information.

![System architecture](http://eftimie.ro/wiki/_media/general-architecture.png?cache=cache&w=624&h=253 "System architecture")

Base functionalities:
*    obtaining the list of students (according to the timetable)
*    marking the attendance / grading
*    synchronizing with the server

# Workflow #

The main workflow is:
![Basic workflow](http://eftimie.ro/wiki/_media/workflow-en.png?w=&h=&cache=cache "Basic workflow")

The basic ui views are:

![Student list view](http://eftimie.ro/wiki/_media/1._home.png?w=&h=&cache=cache "Student list view")

![Timetable](http://eftimie.ro/wiki/_media/2._timetable.png?id=moodle-remote&cache=cache "Timetable")

![Student view](http://eftimie.ro/wiki/_media/3._individual.png?id=moodle-remote&cache=cache "Student view")

# Middleware #

The middleware component will be designed as independent as possible to the other components. It will probably use REST interfaces[TBD].

The middleware will store all the data related to the students. The mobile application will query the middleware and the middleware will sync with Moodle. Syncing with Moodle will be done either by using the Moodle 2.0 API or by other means.

The middleware will be implemented as a web service.

# Technologies #

Android: Java
Middleware: Python (Django) or PHP

# Motivation #
*   Build something useful.
*   Familiarize ourselves with the Android platform.
*   To win :)

# Team #
*   Alex Eftimie 
*   Sergiu Iordache 
*   Irina Preșa

# License #
*   GPL v3

Links

*   <http://blog.hansdezwart.info/2009/12/01/a-design-concept-for-a-mobile-moodle-application/>
*   <http://ignatiawebs.blogspot.com/2010/03/moodle-accessible-for-androids-first.html>
*   <http://beradrian.wordpress.com/2010/06/30/web-in-progress-registration-and-rules/>

Middleware:

*   <http://cipcnet.insa-lyon.fr/Members/ppollet/public/moodlews/>
*   <http://cipcnet.insa-lyon.fr/Members/ppollet/public/moodlews/python-notes/>
*   <http://pywebsvcs.sourceforge.net/>


