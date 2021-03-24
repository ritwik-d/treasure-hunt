#!/usr/bin/python3

import datetime
from environment import *
import gzip
import json
import pyrebase
import os

config = Config(file_config())

def get_config():
    with open(config.get('paths', 'firebase_config'), 'r') as f:
        return json.loads(f.read())


def gzip_file(file_path: str):
    with open(file_path, 'rb') as f_in, gzip.open(file_path + '.gz', 'wb') as f_out:
        f_out.writelines(f_in)
        return file_path + '.gz'


def main():
    dump_file_name = config.get('paths', 'tmp') + datetime.datetime.now().strftime('%m-%d-%Y') + '.sql'
    os.system(f'''mysqldump -u root -p{config.get('mysql', 'root_pw')} --databases {config.get('mysql', 'name')} > {dump_file_name}''')
    dump_file_name = gzip_file(dump_file_name)

    fb_config = get_config()
    firebase = pyrebase.initialize_app(fb_config)
    storage = firebase.storage()

    cloud_path = config.get('firebase_storage', 'db_backups') + datetime.datetime.now().strftime('%m-%d-%Y') + '.sql.gz'
    storage.child(cloud_path).put(dump_file_name)
    

if __name__ == '__main__':
    main()
