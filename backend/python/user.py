from fastapi.responses import FileResponse
from fastapi import File, UploadFile
import datetime
from db import *
from environment import *
from html_bodies import *
import itertools
import json
import os
import pprint
import pyrebase
import shutil
from threading import Timer
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


def get_user_id(value: str, column='email'):
    db = DB()
    db.connect()
    user_id = db.select(f'select user_id from users where {column} = %s', params=(value,), dict_cursor=True)
    if user_id != tuple():
        return user_id[0].get('user_id')


def get_users(self):
    db = DB()
    db.connect()
    return {'status': 200, 'body': list(db.select('select email, username from users', dict_cursor=True))}


def verify_account_web(email_verify_token: str):
    db = DB()
    db.connect()
    affected_rows = db.update('users', {'is_verified': 'true'}, {'email_verify_token': email_verify_token}, aff_rows=True)
    body = HTMLBody('account_verification.html')
    response = None
    if affected_rows != 1:
        body.params('Failure')
        response = body.get_response(404)
    else:
        body.params('Success')
        response = body.get_response(200)

    return response


def send_email_reset_password(email: str):
    db = DB()
    db.connect()
    if not email in list(itertools.chain(*db.select("select email from users where is_verified = 'true'"))):
        return {'status': 404}

    vcode = get_rand_string(6)
    status = send_email('forgot_password.html', email, 'Treasure Hunt Reset Password', params=(vcode,))
    return {'status': 200, 'body': {'vcode': vcode}}


class User:
    def __init__(self, email=None, pw=None, user_id=None, uname=None):
        self.email = email
        self.pw = pw
        if user_id is None:
            user_id = get_user_id(email)
        self.user_id = user_id
        self.uname = uname


    @authenticate
    def accept_invitation(invitation_id: int):
        db = DB()
        db.connect()
        group_id = db.select('select group_id from invitations where invitation_id = %s', params=(invitation_id,), dict_cursor=True)[0].get('group_id')
        row_id = db.update('user_groups', {'members': db.select("select JSON_ARRAY_APPEND(members, '$', %s) as 'result' from user_groups where group_id = %s", params=(self.user_id, group_id), dict_cursor=True)[0].get('result')}, {'group_id': group_id})
        if row_id is None:
            return 400

        db.delete('invitations', {'invitation_id': invitation_id})
        return 200


    @authenticate
    def decline_invitation(invitation_id: int):
        db = DB()
        db.connect()
        db.delete('invitations', {'invitation_id': invitation_id})
        return 200


    @authenticate
    def complete_challenge(self, challenge_id: int):
        db = DB()
        db.connect()
        db.delete('challenges', {'challenge_id': challenge_id})
        points = db.select('select points from users where user_id = %s', params=(self.user_id,))[0][0] + 1
        db.update('users', {'points', points}, {'user_id': self.user_id})
        return 200


    @authenticate
    def create_challenge(self, difficulty: str, latitude: float, longitude: float, name: str, puzzle: str, groups: list):
        db = DB()
        db.connect()
        groups_final = []
        for group_name in groups:
            groups_final.append(get_group_id(group_name))
        row = {
            'creator_id': self.user_id,
            'difficulty': difficulty,
            'latitude': latitude,
            'longitude': longitude,
            'name': name,
            'puzzle': puzzle,
            'user_groups': json.dumps(groups_final)
        }

        row_id = db.insert('challenges', row)
        if row_id is None:
            return 400
        return 201


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
            return 400
        return 200


    @authenticate
    def download_pfp(self):
        with open(config.get('paths', 'firebase_config'), 'r') as f:
            fb_config = json.loads(f.read())
        firebase = pyrebase.initialize_app(fb_config)
        storage = firebase.storage()
        cloud_path = config.get('firebase_storage', 'profile_pictures') + str(self.user_id)
        local_path = config.get('paths', 'tmp') + f'dpfp{self.user_id}'
        storage.child(cloud_path).download(local_path)
        try:
            Timer(1, lambda: os.remove(local_path)).start()
            print(f'local path: {local_path}')
            return local_path
        except:
            return None


    @authenticate
    def get_challenge_data(self, name: str):
        db = DB()
        db.connect()
        data = db.select('select * from challenges where name = %s', params=(name,), dict_cursor=True)[0]
        return {'body': data, 'status': 200}


    @authenticate
    def get_challenges(self):
        db = DB()
        db.connect()
        groups1 = db.select(f"select group_id, name from user_groups where JSON_CONTAINS(members, '{self.user_id}')")
        groups = {}
        for i in groups1:
            groups[i[0]] = i[1]
        print(f'groups: {groups}\ngroups1: {groups1}')
        final = {}
        pub_chals = list(itertools.chain(*db.select('select name from challenges where user_groups = cast(%s as json) and creator_id <> %s', params=('[]', self.user_id))))
        final['Public'] = pub_chals

        for group in groups:
            group_chals = list(itertools.chain(*db.select('select name from challenges where JSON_CONTAINS(user_groups, %s) and creator_id <> %s', params=(str(group), self.user_id))))
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
        return {'body': groups, 'status': 200}


    @authenticate
    def get_group_data(self, name: str):
        db = DB()
        db.connect()
        group_data = db.select('select members, creator_id, join_code from user_groups where name = %s', params=(name,), dict_cursor=True)[0]
        user_ids = json.loads(group_data.get('members'))
        creator_id = group_data.get('creator_id')
        final_data = list(db.select(f'''select username, points, user_id from users where user_id in ({','.join(str(i) for i in user_ids)}) order by points DESC''', dict_cursor=True)) # []
        admin_index = next((index for (index, d) in enumerate(final_data) if d['user_id'] == creator_id), None)
        admin_data = final_data[admin_index]
        admin_data['is_admin'] = 1
        final_data[admin_index] = admin_data

        final_data = {'table_layout': final_data}
        return {'status': 200, 'body': final_data}


    @authenticate
    def get_group_members(self, group_id: str):
        db = DB()
        db.connect()
        uids = json.loads(db.select('select members from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0].get('members'))
        names = []
        for uid in uids:
            names.append(db.select('select username from users where user_id = %s', params=(uid,))[0][0])
        return {'body': names, 'status': 200}


    @authenticate
    def get_group_row(self, group_name: str):
        db = DB()
        db.connect()
        group_id = get_group_id(group_name)
        return {'status': 200, 'body': db.select('select * from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0]}


    @authenticate
    def get_group_settings(self, group_name: str):
        db = DB()
        db.connect()
        group_id = get_group_id(group_name)
        info = db.select('select allow_members_code, join_code, minimum_points, name, creator_id from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0]
        info['group_id'] = group_id
        return {'status': 200, 'body': info}


    @authenticate
    def get_invitations(self):
        db = DB()
        db.connect()
        invites1 = db.select('select from_id, group_id, invitation_id from invitations where to_id = %s', params=(self.user_id,), dict_cursor=True)
        invites = []
        for invite1 in invites1:
            invite = {}
            invite['invitation_id'] = invite1.get('invitation_id')
            invite['from_username'] = db.select('select username from users where user_id = %s', params=(invite1.get('from_id'),))[0][0]
            invite['group_name'] = db.select('select name from user_groups where group_id = %s', params=(invite1.get('group_id'),))[0][0]
            invites.append(invite)

        print(f'invites: {invites}')
        return {'body': invites, 'status': 200}


    @authenticate
    def get_user_challenges(self):
        db = DB()
        db.connect()
        return {'body': list(db.select('select * from challenges where creator_id = %s', params=(self.user_id,), dict_cursor=True)), 'status': 200}


    @authenticate
    def invite_user(self, group_name: str, to_username: str):
        db = DB()
        db.connect()
        if len(db.select('select count(*) from users where username = %s', params=(to_username,))) != 1:
            return 404

        to_id = get_user_id(to_username, 'username')
        group_id = get_group_id(group_name)
        if len(db.select('select * from invitations where to_id = %s and group_id = %s', params=(to_id, group_id))) != 0:
            return 400

        if self.user_id in json.loads(db.select('select * from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0].get('members')):
            return 401

        row = {
            'from_id': self.user_id,
            'to_id': to_id,
            'group_id': group_id
        }
        row_id = db.insert('invitations', row)
        return 200


    @authenticate
    def join_group(self, join_code: str):
        db = DB()
        db.connect()
        group = db.select('select * from user_groups where join_code = %s', params=(join_code,), dict_cursor=True)
        if group == tuple():
            return 404
        if self.user_id in json.loads(group[0].get('members')):
            return 400

        db.update('user_groups', {'members': db.select("select JSON_ARRAY_APPEND(members, '$', %s) as 'result' from user_groups where group_id = %s", params=(self.user_id, group[0].get('group_id')), dict_cursor=True)[0].get('result')}, {'group_id': group[0].get('group_id')})
        return 200


    @authenticate
    def leave_group(self, group_name: str):
        db = DB()
        db.connect()
        group_id = get_group_id(group_name)
        members = json.loads(db.select('select members from user_groups where group_id = %s', params=(group_id,), dict_cursor=True)[0].get('members'))
        members.remove(self.user_id)
        row_id = db.update('user_groups', {'members': json.dumps(members)}, {'group_id': group_id})

        if row_id is None:
            return 404
        row_id = db.execute_sql('delete from challenges where creator_id = %s and JSON_CONTAINS(user_groups, %s)', params=(self.user_id, str(group_id)))
        if row_id is None:
            return 404

        return 200


    def login(self, is_hashed: int):
        db = DB()
        db.connect()
        if self.user_id is None:
            return {'status': 404}

        user_info = None
        if is_hashed == 0:
            user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, hash_password(self.email, self.pw)), dict_cursor=True)
        else:
            user_info = db.select('select * from users where user_id = %s and password = %s and is_verified = "true"', params=(self.user_id, self.pw), dict_cursor=True)

        if user_info == tuple():
            return {'status': 404}
        db.update('users', {'date_last_login': datetime.datetime.now()}, {'user_id': self.user_id})
        user_info = user_info[0]
        return {'status': 200, 'body': user_info}


    def register(self):
        db = DB()
        db.connect()

        if self.uname in list(itertools.chain(*db.select('select username from users'))):
            return 401
        email_verify_token = get_rand_string(10)
        while email_verify_token in list(itertools.chain(*db.select('select email_verify_token from users'))):
            email_verify_token = get_rand_string(10)

        status = send_email('account_verification.html', self.email, 'Treasure Hunt Account Verification', params=(self.uname, email_verify_token))
        if not status:
            return 402
        row = {
            'email': self.email,
            'password': hash_password(self.email, self.pw),
            'username': self.uname,
            'email_verify_token': email_verify_token
        }
        row_id = db.insert('users', row)
        if not row_id is None:
            return 201
        return 400


    def reset_password(self, new_password: str):
        db = DB()
        db.connect()
        row_id = db.update('users', {'password': hash_password(self.email, new_password)}, {'email': self.email})
        if row_id is None:
            return 404
        return 200


    @authenticate
    def send_message(self, group_id: int, message: str):
        db = DB()
        db.connect()
        chat_db = ChatDB(group_id)
        chat_db.send_message(db.select('select username from users where user_id = %s', params=(self.user_id,), dict_cursor=True)[0].get('username'), message)
        return {'body': chat_db.get_messages(), 'status': 201}


    @authenticate
    def get_messages(self, group_id: int):
        chat_db = ChatDB(group_id)
        return {'body': chat_db.get_messages(), 'status': 200}


    @authenticate
    def remove_group_member(self, group_id: int, username: str):
        db = DB()
        db.connect()
        members = json.loads(db.select('select members from user_groups where group_id = %s', params=(group_id,))[0][0])
        members.remove(get_user_id(username, column='username'))
        row_id = db.update('user_groups', {'members': json.dumps(members)}, {'group_id': group_id})
        if row_id is None:
            return 400
        return 200


    @authenticate
    def update_challenge(self, challenge_id: int, new_latitude: float, new_longitude: float, new_puzzle: str, new_difficulty: str, new_group_name: str):
        db = DB()
        db.connect()
        new_group_id = get_group_id(new_group_name)
        row_id = db.update('challenges', {'latitude': new_latitude, 'longitude': new_longitude, 'difficulty': new_difficulty, 'group_id': new_group_id, 'puzzle': new_puzzle}, {'challenge_id': challenge_id})
        if row_id is None:
            return 400
        return 200


    @authenticate
    def update_group_settings(self, group_id: int, allow_members_code: int, min_points: int):
        db = DB()
        db.connect()
        row_id = None
        if allow_members_code == 0:
            row_id = db.update('user_groups', {'allow_members_code': 'false', 'minimum_points': min_points}, {'group_id': group_id})
        else:
            row_id = db.update('user_groups', {'allow_members_code': 'true', 'minimum_points': min_points}, {'group_id': group_id})
        if row_id is None:
            return 400
        return 200


    @authenticate
    def upload_pfp(self, image: UploadFile = File(...)):
        with open(config.get('paths', 'tmp') + f'upfp{self.user_id}', 'wb') as f:
            shutil.copyfileobj(image.file, f)
        with open(config.get('paths', 'firebase_config'), 'r') as f:
            fb_config = json.loads(f.read())
        firebase = pyrebase.initialize_app(fb_config)
        storage = firebase.storage()

        cloud_path = config.get('firebase_storage', 'profile_pictures') + str(self.user_id)
        storage.child(cloud_path).put(config.get('paths', 'tmp') + f'upfp{self.user_id}')

        os.remove(config.get('paths', 'tmp') + f'upfp{self.user_id}')
        return 201
