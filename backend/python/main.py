from api_models import *
from environment import *
from fastapi import FastAPI, Response
from user import *

app = FastAPI()
paths = config.get('api', 'paths')


@app.post(paths.get('complete_challenge'))
async def complete_challenge(json: CompleteChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.complete_challenge(json.challenge_id)


@app.post(paths.get('create_challenge'))
async def create_challenge(json: CreateChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.create_challenge(json.difficulty, json.latitude, json.longitude, json.name, json.puzzle, group_name=json.group_name)


@app.post(paths.get('create_group'))
async def create_group(json: CreateGroup, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.create_group(json.name, json.description)


@app.post(paths.get('delete_challenge'))
async def delete_challenge(json: DeleteChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.delete_challenge(json.challenge_id)


@app.post(paths.get('get_challenge_data'))
async def get_challenge_data(json: GetChallengeData, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_challenge_data(json.name)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_challenges'))
async def get_challenges(json: GetChallenges, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_challenges()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_group_data'))
async def get_group_data(json: GetGroupData, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_group_data(json.name)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_groups'))
async def get_groups(json: GetGroups, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_groups()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_group_members'))
async def get_group_members(json: GetGroupMembers):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_group_members(json.group_id)


@app.post(paths.get('get_user_challenges'))
async def get_user_challenges(json: GetUserChallenges, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_user_challenges()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_user_data'))
async def get_user_data(json: GetUserData):
    user = User(pw=json.pw, user_id=json.user_id)
    return user.get_user_data()


@app.post(paths.get('get_users'))
async def get_users(json: GetUsers, response: Response):
    response_2 = get_users()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('join_group'))
async def join_group(json: JoinGroup, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.join_group(json.join_code)


@app.post(paths.get('login'))
async def login(json: LogIn, response: Response):
    user = User(email=json.email, pw=json.pw)
    request = user.login(json.is_hashed)
    response.status_code = request.get('status')
    return request.get('body')


@app.post(paths.get('register'))
async def register(json: Register, response: Response):
    user = User(email=json.email, pw=json.pw, uname=json.username)
    response.status_code = user.register()


@app.post(paths.get('update_challenge'))
async def update_challenge(json: UpdateChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.update_challenge(json.challenge_name, json.new_name, json.new_puzzle, json.new_difficulty, json.new_group_name)


@app.get(paths.get('verify_account'))
async def verify_account(email_verify_token: str):
    return verify_account_web(email_verify_token)
