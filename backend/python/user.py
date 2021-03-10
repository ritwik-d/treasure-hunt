import datetime
from db import *
from environment import *
import json
from utils import *


def authenticate(func, user_id: str, pw: str):
    def wrapper(*args):
        db = DB()
        db.connect()
        if db.select('select * from users where user_id = %s and pw = %s', params=(user_id, pw), dict_cursor=True) != tuple():
            func(args)
        return fail
    return wrapper


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


    # @authenticate(self.user_id, self.pw)
    def create_challenge(self, difficulty: str, latitude: float, longitude: float, name: str, puzzle: str, group_name=None):
        db = DB()
        db.connect()
        row = {
            'creator_id': self.user_id,
            'difficulty': difficulty,
            'latitude': latitude,
            'longitude': longitude,
            'name': name,
            'puzzle': puzzle
        }
        if not group_name is None:
            row['group_id'] = get_group_id(group_name)
        row_id = db.insert('challenges', row)
        if row_id is None:
            return fail
        return suc


    def create_group(self, name: str):
        db = DB()
        db.connect()
        join_code = get_rand_string(6)
        jcodes1 = db.select('select join_code from user_groups', dict_cursor=True)
        jcodes = []
        for jcode in jcodes1:
            jcodes.append(jcode.get('join_code'))
        while join_code in jcodes:
            join_code = get_rand_string(6)

        row = {
            'creator_id': self.user_id,
            'join_code': join_code,
            'members': json.dumps([self.user_id]),
            'name': name
        }
        row_id = db.insert('user_groups', row)
        if row_id is None:
            return fail
        return suc


    def get_challenges(self):
        db = DB()
        db.connect()
        groups1 = db.select(f"select group_id, name from user_groups where JSON_CONTAINS(members, '{self.user_id}')")
        groups = {}
        for i in groups1:
            groups[i[0]] = i[1]
        final = {}
        pub_chals1 = db.select('select name from challenges where group_id is null and creator_id <> %s', params=(self.user_id,))
        pub_chals = []
        for i in pub_chals1:
            pub_chals.append(pub_chals1[0])
        final['Public'] = pub_chals

        for group in groups:
            group_chals1 = db.select('select name from challenges where group_id = %s and creator_id <> %s', params=(group, self.user_id))
            group_chals = []
            for chal in group_chals1:
                group_chals.append(chal[0])
            final[groups[group]] = group_chals

        return final


    def join_group(self, join_code: str):
        db = DB()
        db.connect()
        group = db.select('select * from user_groups where join_code = %s', params=(join_code,), dict_cursor=True)
        if group == tuple():
            return {'status': 'nogroup'}
        if self.user_id in json.loads(group[0].get('members')):
            return {'status': 'alreadyjoined'}

        db.update('user_groups', {'members': db.select("select JSON_ARRAY_APPEND(members, '$', %s) as 'result' from user_groups where group_id = %s", params=(self.user_id, group[0].get('group_id')))[0][0]}, {'group_id': group[0].get('group_id')})
        return suc


    def login(self):
        db = DB()
        db.connect()
        if self.user_id is None:
            return fail
        user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        if user_info == tuple():
            return fail
        db.update('users', {'date_last_login': datetime.datetime.now()}, {'user_id': self.user_id})
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
