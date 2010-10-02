# A worker process connecting to moodle and fetching data
# Uses cURL as backend

import os, sys

from scrapper.moodle import MoodleSession, LoginError

MOODLE_URL = "https://cs09.curs.pub.ro/"
USER = "ioan.eftimie"
PASS = "***"

COURSE_ID = 9

if __name__ == "__main__":
    if len(sys.argv) >= 3:
        USER, PASS = sys.argv[1:3]
        if len(sys.argv) >= 4:
            MOODLE_URL = sys.argv[3]
    else:
        print "Usage: %s <user> <password> [host]" % sys.argv[0]
        
    session = MoodleSession(MOODLE_URL)
    session.set_verbosity(0)
    
    try:
        session.login(USER, PASS)
    except LoginError:
        sys.stderr.write("Failed to connect.\n")
        sys.exit(1)
    
    if session.answered("Overview of my courses"):
        print "Ok"
        for c in session.list_courses():
            print c[0],c[1]
        #print session.list_groups(COURSE_ID)
        session.logout()
        sys.exit(0)
    elif session.answered("forgot_password.php"):
        sys.stderr.write("Wrong username/password\n")
        sys.exit(-1)
    else:
        sys.stderr.write("Unexpected page (%d bytes)\n" % len(session.body()))
        #sys.stdout.write(session.body())
        sys.exit(-2)
