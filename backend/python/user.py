from db import *
import hashlib
from utils import *

def authenticator(func, user_id: int, pw: str):
    def wrapper():
        db = DB()
        db.connect()
        date_created = db.select('select date_created from users where user_id = %s and password = %s', params=(user_id, pw), dict_cursor=True)
        if date_created = tuple():
            return fail
        func()
    return wrapper


def get_user_id(email: str):
    db = DB()
    db.connect()
    user_id = db.select('select user_id from users where email = %s', params=(email,), dict_cursor=True)
    if user_id != tuple():
        return user_id[0].get('user_id')


def hash_password(email: str, pw: str):
    salt = hashlib.sha256(email.encode()).hexdigest()[0:4]
    return hashlib.sha256((salt + password).encode()).hexdigest()


class User:
    def __init__(self, email=None, fname=None, lname=None, pw=None, user_id=None):
        self.email = email
        self.fname = fname
        self.lname = lname
        self.pw = pw
        if user_id is None:
            user_id = get_user_id(email)
        self.user_id = user_id


    def complete_challenge(self, challenge_id: int):
        pass


    def create_challenge(self, name: str, group_id=None, puzzle: str):
        pass


    def get_challenges(self):
        pass


    def login(self):
        db = DB()
        db.connect()
        if self.user_id is None:
            return fail
        user_info = db.select('select * from users where user_id = %s and password = %s', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        if user_info == tuple():
            return fail
        return user_info


    def register(self):
        db = DB()
        db.connect()
        row = {
            'email': self.email,
            'fname': self.fname,
            'lname': self.lname,
            'password': hash_password(self.email, self.pw)
        }
        row_id = db.insert('users', row)
        if not row_id is None:
            return suc
        return fail


    def upload_pfp(self):
        pass
