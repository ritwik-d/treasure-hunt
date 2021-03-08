from db import *
from utils import send_email

class User:
    def __init__(self, email=None, fname=None, lname=None, pw=None, user_id=None):
        self.email = email
        self.fname = fname
        self.lname = lname
        self.pw = pw
        self.user_id = user_id


    def complete_challenge(self):
        pass


    def create_challenge(self):
        pass


    def get_challenges(self):
        pass


    def login(self):
        pass


    def register(self):
        pass


    def upload_pfp(self):
        pass
