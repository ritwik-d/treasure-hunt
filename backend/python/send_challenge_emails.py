#!/usr/bin/python3

from utils import *
from db import *

def get_users():
    db = DB()
    db.connect()
    return db.select('select user_id, email from users', dict_cursor=True)


def send_challenge_email(email: str, challenges: dict):
    pass


def main():
    for user in get_users():
        user_id = user.get('user_id')


if __name__ == '__main__':
    main()
