from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from environment import *
import hashlib
import json
import mimetypes
import smtplib
import random
import string


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
    chars = [i for i in string.printable[0:62]]
    random.shuffle(chars)
    return ''.join(chars[0: length])


def hash_password(email: str, password: str):
    salt = hashlib.sha256(email.encode()).hexdigest()[0:4]
    return hashlib.sha256((salt + password).encode()).hexdigest()


def send_email(file_name: str, receiver_email: str, subject: str, sender_email=None, params=None, html=True):
    smtp_config = config.get('smtp')
    final_path = config.get('paths', 'emails') + file_name

    if sender_email is None:
        sender_email = smtp_config.get('email')

    password = smtp_config.get('pw')
    port = smtp_config.get('port')
    smtp_server = smtp_config.get('server')

    with open(final_path, 'r') as f:
        message = f.read()
        if params:
            message.format(params)
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
        

fail = {'status': 'failed'}
suc = {'status': 'success'}
