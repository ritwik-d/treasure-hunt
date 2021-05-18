import argparse
from evironment import *
import os

parser = argparse.ArgumentParser()
parser.add_argument('--nbg', help='Change the API service to NOT run in the background.', action='store_true')
is_bg = parser.parse_args().nbg

os.system(f'''{config.get('paths', 'gunicorn_run')} {str(is_bg).lower()}''')
