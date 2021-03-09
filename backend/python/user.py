from db import *
from environment import *
import hashlib
from utils import *


def get_user_id(email: str):
    db = DB()
    db.connect()
    user_id = db.select('select user_id from users where email = %s', params=(email,), dict_cursor=True)
    print(user_id)
    if user_id != tuple():
        return user_id[0].get('user_id')


def hash_password(email: str, password: str):
    salt = hashlib.sha256(email.encode()).hexdigest()[0:4]
    return hashlib.sha256((salt + password).encode()).hexdigest()


class User:
    def __init__(self, email=None, fname=None, lname=None, pw=None, user_id=None, uname=None):
        self.email = email
        self.fname = fname
        self.lname = lname
        self.pw = pw
        if user_id is None:
            user_id = get_user_id(email)
        self.user_id = user_id
        self.uname = uname


    def complete_challenge(self, challenge_id: int):
        pass


    # def create_challenge(self, name: str, group_id=None, puzzle: str):
    #     pass


    def get_challenges(self):
        pass


    def login(self):
        db = DB()
        db.connect()
        if self.user_id is None:
            return 'blach'
        user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        print(user_info)
        if user_info == tuple():
            return fail
        return user_info


    def register(self):
        # status = send_email('register.html', self.email, 'Treasure Hunt Account Verification', params=(f'{self.fname} {self.lname}', '\t', '<link>', '\t', '\t'))
        # if not status:
        #     return fail
        db = DB()
        db.connect()
        row = {
            'email': self.email,
            'fname': self.fname,
            'is_verified': 'true',
            'lname': self.lname,
            'password': hash_password(self.email, self.pw),
            'username': self.uname
        }
        row_id = db.insert('users', row)
        if not row_id is None:
            return suc
        return fail


    def upload_pfp(self):
        pass
