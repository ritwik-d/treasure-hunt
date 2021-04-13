import datetime
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from environment import *
import hashlib
import json
import mimetypes
import smtplib
import ssl
import random
import string
import redis

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
            server.starttls(ssl.create_default_context())
            server.ehlo()
            server.login(sender_email, password)
            server.sendmail(sender_email, receiver_email, msg.as_string())
            return True
    except smtplib.SMTPRecipientsRefused:
        return False
