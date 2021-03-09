from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from environment import *
import mimetypes
import smtplib


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


def hash_password(email: str, password: str):
    salt = hashlib.sha256(email.encode()).hexdigest()[0:4]
    return hashlib.sha256((salt + password).encode()).hexdigest()
    

def send_email(file_name: str, receiver_email: str, subject: str, sender_email=None, params=None):
    smtp_config = config.get('smtp')
    final_path = smtp_config.get('file_paths', 'emails') + file_name

    if sender_email is None:
        sender_email = smtp_config.get('sender')
    password = smtp_config.get('pw')
    port = smtp_config.get('port')
    file_type = mimetypes.guess_type(final_path)

    with open(final_path, 'r') as f:
        message = f.read()
        if params:
            message.format(params)
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = sender_email
    msg['To'] = receiver_email

    if file_type == 'text/html':
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
