#!/usr/local/bin/python

from api_models import *
from environment import *
from fastapi import FastAPI
from user import *

app = FastAPI()
paths = config.get('api', 'paths')

@app.post(paths.get('create_challenge'))
async def create_challenge(json: CreateChallenge):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.create_challenge(json.difficulty, json.latitude, json.longitude, json.name, json.puzzle, group_name=json.group_name)


@app.post(paths.get('create_group'))
async def create_group(json: CreateGroup):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.create_group(json.name)


@app.post(paths.get('get_challenges'))
async def get_challenges(json: GetChallenges):
    user = User(pw=json.pw, user_id=json.user)


@app.post(paths.get('login'))
async def login(json: LogIn):
    user = User(email=json.email, pw=json.pw)
    return user.login()


@app.post(paths.get('register'))
async def register(json: Register):
    user = User(email=json.email, fname=json.fname, lname=json.lname, pw=json.pw, uname=json.username)
    return user.register()
