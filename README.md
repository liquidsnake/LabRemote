An Android application to help teaching assistants.
# Overview #

Using this application the assistant can easily take attendance and grade the students using the smartphone.

The application will allows retrieving the student list (name and avatar) according to the timetable, grading the students and exporting/synchronizing the information back to Moodle and/or Google Docs / other interfaces. It also allows viewing/editing the timetable, group assignment and attendance in the middleware using the smartphone information.

![System architecture](http://eftimie.ro/wiki/_media/general-architecture.png?cache=cache&w=624&h=253 "System architecture")

Base functionalities:

*   obtaining the list of students (according to the timetable)
*   marking the attendance / grading
*   synchronizing with the server

# System requirements #
Mobile application:

*   (development only) Android 2.2 SDK
*   Android phone with 2.2 firmware(tested) or lower(untested)

Middleware:

*   Pyhton 2.6
*   Django 1.2.3
*   django-uni-form

# Workflow #

The main workflow is:
![Basic workflow](http://eftimie.ro/wiki/_media/workflow-en.png?w=&h=&cache=cache "Basic workflow")

For screenshots and more information about the way the application works read the application "Manual":http://labremote.korect.ro/manual/

The basic ui views are:

![Student list view](http://eftimie.ro/wiki/_media/1._home.png?w=&h=&cache=cache "Student list view")
![Timetable](http://eftimie.ro/wiki/_media/2._timetable.png?id=moodle-remote&cache=cache "Timetable")
![Student view](http://eftimie.ro/wiki/_media/3._individual.png?id=moodle-remote&cache=cache "Student view")

# Middleware #

The middleware component will is designed to be as independent as possible to the other components. It uses REST interfaces.

The middleware stores all the data related to the students. The mobile application will query the middleware and the middleware will sync with Moodle. Syncing with Moodle will be done either by using the Moodle 2.0 API or by other means.

The middleware is implemented as a web service.

# Development #
We recommend using the Android Eclipse-based IDE for developing the mobile application. After cloning the git repository just open the "android" folder as a project.

To run the middleware locally all you need to do is install the <code> python-django </code> package and run the following commands from the  <code>middleware </code> repository directory:

<pre>
./manage.py syncdb # only the fist time
./manage.py runserver # to start the server
</pre>

The server will be available at the following address: "http://127.0.0.1:8000/api/":http://127.0.0.1:8000/api/

If you wish to use the server on another port or want the server to be accessible to the outside use:

./manage.py runserver out.side.ip.address:port # to start the server

To populate the database with info you can use the administration interface at the address: "http://127.0.0.1:8000/admin/":http://127.0.0.1:8000/admin

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

*   <http://blog.hansdezwart.info/2010/12/01/a-design-concept-for-a-mobile-moodle-application/>
*   <http://ignatiawebs.blogspot.com/2010/03/moodle-accessible-for-androids-first.html>
*   <http://beradrian.wordpress.com/2010/06/30/web-in-progress-registration-and-rules/>

Middleware:

*   <http://cipcnet.insa-lyon.fr/Members/ppollet/public/moodlews/>
*   <http://cipcnet.insa-lyon.fr/Members/ppollet/public/moodlews/python-notes/>
*   <http://pywebsvcs.sourceforge.net/>


