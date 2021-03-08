from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from environment import *
import mimetypes
import smtplib

config = Config(file_config(''))

def send_email(file_name: str, receiver_email, sender_email=None, subject: str):
    smtp_config = config.get('smtp')
    final_path = f'''{config.get('file_paths', 'emails')}{file_name}'''
    file_type = mimetypes.guess_type(final_path)
    with open(final_path, 'r') as f:
        message = f.read()
    msg = MIMEMultipart('alternative')
    msg['Subject'] = subject
    msg['From'] = sender_email
    msg['To'] = receiver_email
    if file_type == 'text/html':
        msg.attach(MIMEText(message, 'html'))
    else:
        msg.attach(MIMEText(message, 'plain'))

    try:
        with smtplib.SMTP(smtp_server, port) as server
            server.ehlo()
            server.starttls()
            server.ehlo()
            server.login(sender_email, password)
            server.sendmail(sender_email, receiver_email, msg.as_string())
            return True
    except smtplib.SMTPRecipientsRefused:
        return False
