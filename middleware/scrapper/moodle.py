# A scrapper class connecting to moodle and fetching data
# Uses cURL as backend

import os, sys
import curl
import pycurl

class LoginError(pycurl.error): pass

class MoodleSession(curl.Curl):
    """ Moodle-specific methods. Sensitive to changes in site design. """
    def login(self, name, password):
        """Establish a login session."""
        try:
            self.post("login/index.php", (("username", name),
                                        ("password", password)))
        except pycurl.error:
            raise LoginError(pycurl.error)
            
    def logout(self):
        """Log out 
        TODO
            - find more about the sesskey problem
        """
        self.get("login/logout.php")
        
    def list_courses(self):
        """ Get a list of courses. To be called right after login (/my/)
        TODO: 
            - make a get call before this
            - better parsing
        """
        ids = []
        content = self.body()
        body = content[content.find('id="content"'):]
        index = 0
        
        while True:
            index = body.find('/course/view.php?id=', index + 1)
            if index == -1: break
            id = body[index + 20:body.find('"', index + 1)]
            name = body[index + 22 + len(id):body.find('<', index + 1)]
            ids.append((int(id), name))
        #print "Course ids: ", ids
        return ids
        
    def list_groups(self, course_id):
        """ Get the list of groups and students inside
        TODO:
            - fix unicode bug at student's name
        """
        self.get("group/overview.php?id=%d" % course_id)
        content = self.body()
        body = content[content.find('id="selectgroup_jump"'):]
        
        # Get group name/ids
        index = 0
        last_index = 0
        groups = {}
        while True:
            index = body.find('group=', index + 1)
            if index == -1: break
            id = body[index + 6:body.find('"', index + 1)]
            end_index = body.find('>', index + 6 + len(id))
            name = body[end_index + 1:body.find('<', end_index)]
            if id != '0' and id != '':
                groups[id] = {"name": name, "users": None}
            if index != -1:
                last_index = index
                
        # Get members
        body = body[last_index + 20:]
        for id, data in groups.items():
            index = body.find(data['name'])
            end = body.find('<tr ', index)
            userstext = body[index:end]
            users = []
            j = 0
            while True:
                j = userstext.find('/user/view.php?id=', j + 20)
                if j == -1: break
                id = int(userstext[j + 18:userstext.find('&', j + 18)])
                name = userstext[userstext.find('>', j) + 1:userstext.find('<', j)]
                users.append((id, name, self.get_avatar(id)))
            data['users'] = users
            
        return groups
        
    def get_avatar(self, user_id):
        """ Return user's avatar url """
        return "%suser/pix.php/%d/f1.jpg" % (self.base_url, user_id)
