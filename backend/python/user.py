import datetime
from db import *
from environment import *
import hashlib
from utils import *


def get_group_id(gname: str):
    db = DB()
    db.connect()
    group_id = db.select('select group_id from user_groups where name = %s', params=(gname,), dict_cursor=True)
    if group_id != tuple():
        return group_id[0].get('user_id')


def get_user_id(email: str):
    db = DB()
    db.connect()
    user_id = db.select('select user_id from users where email = %s', params=(email,), dict_cursor=True)
    if user_id != tuple():
        return user_id[0].get('user_id')
        

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


    def create_challenge(self, difficulty: str, name: str, puzzle: str, group_name=None):
        db = DB()
        db.connect()
        row = {
            'creator_id': self.user_id,
            'difficulty': difficulty,
            'name': name,
            'puzzle': puzzle
        }
        if not group_name is None:
            row['group_id'] = get_group_id(group_name)
        row_id = db.insert('challenges', row)
        if row_id is None:
            return fail
        return suc


    def login(self):
        db = DB()
        db.connect()
        if self.user_id is None:
            return fail
        user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        if user_info == tuple():
            return fail
        db.update('users', {'date_last_login': datetime.datetime.now()}, {'user_id': self.user_id, 'password': hash_password(self.email, self.pw)})
        return user_info[0]


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
