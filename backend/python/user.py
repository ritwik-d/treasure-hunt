import datetime
from db import *
from environment import *
import json
from utils import *


def authenticate(func):
    def wrapper(user, *args, **kwargs):
        db = DB()
        db.connect()
        if db.select('select * from users where user_id = %s and password = %s', params=(user.user_id, user.pw), dict_cursor=True) != tuple():
            return func(user, *args, **kwargs)
    return wrapper


def get_group_id(gname: str):
    db = DB()
    db.connect()
    group_id = db.select('select group_id from user_groups where name = %s', params=(gname,), dict_cursor=True)
    if group_id != tuple():
        return group_id[0].get('group_id')


def get_user_id(email: str):
    db = DB()
    db.connect()
    user_id = db.select('select user_id from users where email = %s', params=(email,), dict_cursor=True)
    if user_id != tuple():
        return user_id[0].get('user_id')


def get_users(self):
    db = DB()
    db.connect()
    return {'status': 200, 'body': list(db.select('select email, username from users', dict_cursor=True))}


class User:
    def __init__(self, email=None, pw=None, user_id=None, uname=None):
        self.email = email
        self.pw = pw
        if user_id is None:
            user_id = get_user_id(email)
        self.user_id = user_id
        self.uname = uname


    @authenticate
    def complete_challenge(self, challenge_id: int):
        db = DB()
        db.connect()
        db.delete('challenges', {'challenge_id': challenge_id})
        points = db.select('select points from users where user_id = %s', params=(self.user_id,))[0][0] + 1
        db.update('users', {'points', points}, {'user_id': self.user_id})


    @authenticate
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


    @authenticate
    def create_group(self, name: str, description=None):
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
            'description': description,
            'join_code': join_code,
            'members': json.dumps([self.user_id]),
            'name': name
        }
        row_id = db.insert('user_groups', row)
        if row_id is None:
            return 400
        return 201


    @authenticate
    def delete_challenge(self, challenge_id: int):
        db = DB()
        db.connect()
        row_id = db.delete('challenges', {'challenge_id': challenge_id, 'creator_id': self.user_id})
        if row_id is None:
            return {'status': 400}
        return {'status': 200}


    @authenticate
    def get_challenge_data(self, name: str):
        db = DB()
        db.connect()
        data = db.select('select * from challenges where name = %s', params=(name,), dict_cursor=True)[0]
        return data


    @authenticate
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
        return {'body': final, 'status': 200}


    @authenticate
    def get_groups(self):
        db = DB()
        db.connect()
        groups1 = db.select(f"select name from user_groups where JSON_CONTAINS(members, '{self.user_id}')")
        groups = []
        for i in groups1:
            groups.append(i[0])
        return groups


    @authenticate
    def get_group_data(self, name: str):
        db = DB()
        db.connect()
        return db.select('select * from user_groups where name = %s', params=(name,), dict_cursor=True)[0]


    @authenticate
    def get_group_members(self, group_id: int):
        db = DB()
        db.connect()
        uids = json.loads(db.select('select members from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0].get('members'))
        names = []
        for uid in uids:
            names.append(db.select('select username from users where user_id = %s', params=(uid,))[0][0])
        return names


    @authenticate
    def get_user_challenges(self):
        db = DB()
        db.connect()
        return {'body': list(db.select('select * from challenges where creator_id = %s', params=(self.user_id,), dict_cursor=True)), 'status': 200}


    @authenticate
    def get_user_data(self):
        db = DB()
        db.connect()
        return db.select('select * from users from user_id = %s', params=(self.user_id,), dict_cursor=True)[0]


    @authenticate
    def join_group(self, join_code: str):
        db = DB()
        db.connect()
        group = db.select('select * from user_groups where join_code = %s', params=(join_code,), dict_cursor=True)
        if group == tuple():
            return {'status': 'nogroup'}
        if self.user_id in json.loads(group[0].get('members')):
            return {'status': 'alreadyjoined'}

        db.update('user_groups', {'members': db.select("select JSON_ARRAY_APPEND(members, '$', %s) as 'result' from user_groups where group_id = %s", params=(self.user_id, group[0].get('group_id')), dict_cursor=True)[0].get('result')}, {'group_id': group[0].get('group_id')})
        return suc


    def login(self):
        db = DB()
        db.connect()
        if self.user_id is None:
            return {'status': 404}
        user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        if user_info == tuple():
            return {'status': 404}
        db.update('users', {'date_last_login': datetime.datetime.now()}, {'user_id': self.user_id})
        user_info = user_info[0]
        return {'status': 200, 'body': user_info}


    def register(self):
        # status = send_email('register.html', self.email, 'Treasure Hunt Account Verification', params=(f'{self.fname} {self.lname}', '\t', '<link>', '\t', '\t'))
        # if not status:
        #     return fail
        db = DB()
        db.connect()
        row = {
            'email': self.email,
            'is_verified': 'true',
            'password': hash_password(self.email, self.pw),
            'username': self.uname
        }
        row_id = db.insert('users', row)
        if not row_id is None:
            return 201
        return 400


    @authenticate
    def update_challenge(self, challenge_name: str, new_name: str, new_puzzle: str, new_difficulty: str, new_group_name: str):
        db = DB()
        db.connect()
        new_group_id = get_group_id(new_group_name)
        row_id = db.update('challenges', {'difficulty': new_difficulty, 'group_id': new_group_id, 'name': new_name, 'puzzle': new_puzzle}, {'challenge_name': challenge_name})
        if row_id is None:
            return 400
        return 200
