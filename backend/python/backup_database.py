#!/usr/bin/python3

import datetime
from environment import *
import json
import pyrebase
import os

config = Config()

def get_config():
    with open(fb_config.get('paths', 'firebase_config'), 'r') as f:
        return json.loads(f.read())


def main():
    dump_file_name = config.get('paths', 'tmp') + datetime.datetime.now().strftime('%m/%d/%Y') + '.sql'
    os.system(f"""root_pw='{config.get('mysql', 'root_pw')}'""")
    os.system(f'''mysqldump -u root -p$root_pw --databases {config.get('mysql', 'name')} > {dump_file_name}''')
    fb_config = get_config()
    firebase = pyrebase.initialize_app(fb_config)
    storage = firebase.storage()

    cloud_path = config.get('firebase_storage', 'db_backups') + datetime.datetime.now().strftime('%m/%d/%Y') + '.sql'
    storage.child(cloud_path).put(dump_file_name)

    os.remove(dump_file_name)


if __name__ == '__main__':
    main()
