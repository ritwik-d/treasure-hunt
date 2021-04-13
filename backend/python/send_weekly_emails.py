#!/usr/bin/python3

import db
from utils import send_email


def get_users():
    db = DB()
    db.connect()
    return db.select("select * from users where receive_emails = 'true'", dict_cursor=True)


def main():
    for user in get_users():
        pass


if __name__ == '__main__':
    main()
