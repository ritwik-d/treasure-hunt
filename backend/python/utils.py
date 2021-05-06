import datetime
from db import *
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from environment import *
import hashlib
import json
import math
import mimetypes
import smtplib
import ssl
import random
import string
import redis


def authenticate(func):
    def wrapper(user, *args, **kwargs):
        db = DB()
        db.connect()
        user_data = db.select('select user_id from users where user_id = %s and password = %s', params=(user.user_id, user.pw), dict_cursor=True)
        if user_data != tuple():
            with open(config.get('logs', 'authentication'), 'a') as f:
                f.write(f'''{datetime.datetime.now()}: Authenticated {user_data[0].get('user_id')}\n''')
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


def get_race_id(race_title: str):
    db = DB()
    db.connect()
    return db.select('select race_id from races where title = %s', params=(race_title,), dict_cursor=True)[0].get('race_id')


redis_connections = {}

class ChatDB:
    def __init__(self, group_id: int):
        global redis_connections
        if redis_connections.get('rdb'):
            self.rdb = redis_connections.get('rdb')
        else:
            self.rdb = redis.Redis()
            redis_connections['rdb'] = self.rdb
        self.group_id = group_id


    def send_message(self, username: str, message: str):
        self.rdb.rpush(str(self.group_id), json.dumps({
            'timestamp': str(datetime.datetime.now()),
            'message': message,
            'username': username
        }))


    def get_messages(self):
        messages = [json.loads(message) for message in self.rdb.lrange(str(self.group_id), 0, -1)]
        return messages


class Race:
    def __init__(self, title=None, start_time=None, latitude=None, longitude=None, group_id=None):
        self.title = title
        self.start_time = start_time
        self.latitude = latitude
        self.longitude = longitude
        self.group_id = group_id


    def create(self, creator_id: int):
        db = DB()
        db.connect()
        row = {
            'latitude': self.latitude,
            'longitude': self.longitude,
            'group_id': self.group_id,
            'start_time': self.start_time,
            'title': self.title,
            'creator_id': creator_id
        }
        row_id = db.insert('races', row)
        if row_id is None:
            return False
        return True


class RaceLocation:
    def __init__(self, user_id: int, race_id: int, latitude=None, longitude=None):
        self.user_id = user_id
        self.race_id = race_id
        self.latitude = latitude
        self.longitude = longitude


    def insert(self):
        db = DB()
        db.connect()
        row = {
            'race_id': self.race_id,
            'user_id': self.user_id,
            'latitude': self.latitude,
            'longitude': self.longitude
        }
        row_id = db.insert('race_locations', row)
        if row_id is None:
            return False
        return True


    def update(self):
        db = DB()
        db.connect()
        row_id = db.update('race_locations', {'latitude': self.latitude}, {'race_id': self.race_id, 'user_id': self.user_id})
        row_id2 = db.update('race_locations', {'longitude': self.longitude}, {'race_id': self.race_id, 'user_id': self.user_id})
        if row_id is None or row_id2 is None:
            return False
        return True


    def delete(self):
        db = DB()
        db.connect()
        row_id = db.delete('race_locations', {'race_id': self.race_id, 'user_id': self.user_id})
        if row_id is None:
            return False
        return True


class RaceInProgress:
    def __init__(self, race_id):
        self.race_id = race_id


    def add_user(self, user_id: int, latitude: float, longitude: float):
        user_location = RaceLocation(user_id, self.race_id, latitude, longitude)
        user_location.insert()


    def get_users(self):
        db = DB()
        db.connect()
        result = db.select('select user_id, latitude, longitude from race_locations where race_id = %s', params=(self.race_id,), dict_cursor=True)
        for i in result:
            i["username"] = db.select("select username from users where user_id = %s", params=(i["user_id"],))[0][0]
        return result


    def remove_user(self, user_id: int):
        user_location = RaceLocation(user_id, self.race_id)
        user_location.delete()


    def update_user(self, user_id: int, latitude: float, longitude: float):
        user_location = RaceLocation(user_id, self.race_id, latitude, longitude)
        user_location.update()


def get_jwt(user_id: int):
    secret_key = config.get('security', 'secret_key')
    json = {'user_id': user_id}
    hash = hashlib.sha256((secret_key + json.dumps(json)).encode()).hexdigest()
    return {'json': json, 'hash': hash}


def verify_jwt(hash: str, json: dict):
    secret_key = config.get('security', 'secret_key')
    real_hash = hashlib.sha256((secret_key + json.dumps(json)).encode()).hexdigest()
    if hash == real_hash:
        return True


def get_rand_string(length: int):
    chars = string.printable[0:62]
    final = ''
    for _ in range(length):
        final += chars[random.randint(0, 61)]
    return final


def hash_password(email: str, password: str):
    salt = hashlib.sha256(email.encode()).hexdigest()[0:4]
    return hashlib.sha256((salt + password).encode()).hexdigest()


def send_email(file_name: str, receiver_email: str, subject: str, sender_email=None, html=True, params=None):
    smtp_config = config.get('smtp')
    final_path = config.get('paths', 'emails') + file_name

    if sender_email is None:
        sender_email = smtp_config.get('email')

    password = smtp_config.get('pw')
    port = smtp_config.get('port')
    smtp_server = smtp_config.get('server')

    with open(final_path, 'r') as f:
        message = f.read()
        if not params is None:
            message = message.format(*params)
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = sender_email
    msg['To'] = receiver_email

    if html:
        msg.attach(MIMEText(message, 'html'))
    else:
        msg.attach(MIMEText(message, 'plain'))

    try:
        with smtplib.SMTP(smtp_server, port) as server:
            server.ehlo()
            server.starttls()
            server.ehlo()
            server.login(sender_email, password)
            server.sendmail(sender_email, receiver_email, msg.as_string())
            return True
    except smtplib.SMTPRecipientsRefused:
        return False


def create_random_race_location(latitude: float, longitude: float, difficulty: str):
    direction = random.randint(1, 360)
    magnitude = random.randint(-1, 1) / 10 # kilometers
    if difficulty == 'easy':
        magnitude += 0.5
    elif difficulty == 'medium':
        magnitude += 1
    elif difficulty == 'hard':
        magnitude += 1.5
    else:
        return False

    direction_rad = direction * math.pi / 180
    x_displacement = magnitude * math.cos(direction_rad) # kilometers displacement x
    y_displacement = magnitude * math.sin(direction_rad) # kilometers displacement y
    radius_earth = 6371

    final_longitude = longitude + (x_displacement / radius_earth) * (180 / math.pi) / math.cos(latitude * math.pi / 180)
    final_latitude = latitude + (y_displacement / radius_earth) * (180 / math.pi)

    return [round(final_latitude, 10), round(final_longitude, 10)]
