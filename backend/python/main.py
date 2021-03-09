#!/usr/local/bin/python

from api_models import *
from environment import *
from fastapi import FastAPI
from user import *

app = FastAPI()
paths = config.get('api', 'paths')

@app.post(paths.get('login'))
async def login(json: LogIn):
    user = User(email=json.email, pw=json.pw)
    return user.login()


@app.post(paths.get('register'))
async def register(json: Register):
    user = User(email=json.email, fname=json.fname, lname=json.lname, pw=json.pw, uname=json.username)
    return user.register()
