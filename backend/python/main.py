#!/usr/local/bin/python

from api_models import *
from environment import *
from fastapi import FastAPI, Response
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


@app.post(paths.get('get_challenge_data'))
async def get_challenge_data(json: GetChallengeData):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_challenge_data(json.name)


@app.post(paths.get('get_challenges'))
async def get_challenges(json: GetChallenges):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_challenges()


@app.post(paths.get('get_group_data'))
async def get_group_data(json: GetGroupData):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_group_data(json.name)


@app.post(paths.get('get_groups'))
async def get_groups(json: GetGroups):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_groups()


@app.post(paths.get('get_group_members'))
async def get_group_members(json: GetGroupMembers):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_group_members(json.group_id)


@app.post(paths.get('join_group'))
async def join_group(json: JoinGroup):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.join_group(json.join_code)


@app.post(paths.get('login'))
async def login(json: LogIn):
    user = User(email=json.email, pw=json.pw)
    return user.login()


@app.post(paths.get('register'), status_code=201)
async def register(json: Register, response: Response):
    user = User(email=json.email, pw=json.pw, uname=json.username)
    response.status_code = user.register()


@app.post(paths.get('get_user_data'))
async def get_user_data(json: GetUserData):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_user_data()
