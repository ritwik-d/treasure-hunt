from api_models import *
from environment import *
from fastapi import FastAPI, Form, Response
from user import *

app = FastAPI()
paths = config.get('api', 'paths')


@app.post(paths.get('accept_invitation'))
async def accept_invitation(json: AcceptInvitation, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.accept_invitation(json.invitation_id)


@app.post(paths.get('get_races'))
async def get_races(json: GetRaces, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_races()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('decline_invitation'))
async def decline_invitation(json: DeclineInvitation, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.decline_invitation(json.invitation_id)


@app.post(paths.get('complete_challenge'))
async def complete_challenge(json: CompleteChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.complete_challenge(json.challenge_id)


@app.post(paths.get('create_challenge'))
async def create_challenge(json: CreateChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.create_challenge(json.difficulty, json.latitude, json.longitude, json.name, json.puzzle, groups=json.groups)


@app.post(paths.get('create_group'))
async def create_group(json: CreateGroup, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.create_group(json.name, json.description)


@app.post(paths.get('create_race'))
async def get_challenges(json: CreateRace, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.create_race(json.title, json.start_time, json.latitude, json.longitude, json.group_name)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('delete_challenge'))
async def delete_challenge(json: DeleteChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.delete_challenge(json.challenge_id)


@app.post(paths.get('download_pfp'))
async def download_pfp(json: DownloadPfp):
    user = User(pw=json.pw, user_id=json.user_id)
    try:
        return FileResponse(user.download_pfp())
    except:
        return None


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
    response_2 = user.get_groups(json.is_admin)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_group_members'))
async def get_group_members(json: GetGroupMembers, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_group_members(json.group_id)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_group_row'))
async def get_group_row(json: GetGroupRow, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_group_row(json.group_name)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_group_settings'))
async def get_group_settings(json: GetGroupSettings, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_group_settings(json.group_name)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_invitations'))
async def get_challenge_data(json: GetInvitations, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_invitations()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_user_challenges'))
async def get_user_challenges(json: GetUserChallenges, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_user_challenges()
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('invite_user'))
async def invite_user(json: InviteUser, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    scode = user.invite_user(json.group_name, json.to_username)
    print({'status': scode})
    return {'status': scode}


@app.post(paths.get('join_group'))
async def join_group(json: JoinGroup, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.join_group(json.join_code)


@app.post(paths.get('leave_group'))
async def leave_group(json: LeaveGroup, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.leave_group(json.group_name, json.new_admin)


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


@app.post(paths.get('send_email_reset_password'))
async def register(json: SendEmailResetPassword, response: Response):
    response_2 = send_email_reset_password(json.email)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('reset_password'))
async def register(json: ResetPassword, response: Response):
    user = User(email=json.email)
    response.status_code = user.reset_password(json.new_password)


@app.post(paths.get('remove_group_member'))
async def remove_group_member(json: RemoveGroupMember, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.remove_group_member(json.group_id, json.username)


@app.post(paths.get('send_message'))
async def send_message(json: SendMessage, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.send_message(json.group_id, json.message)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('get_messages'))
async def get_messages(json: GetMessages, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response_2 = user.get_messages(json.group_id)
    response.status_code = response_2.get('status')
    return response_2.get('body')


@app.post(paths.get('update_challenge'))
async def update_challenge(json: UpdateChallenge, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.update_challenge(json.challenge_id, json.new_latitude, json.new_longitude, json.new_puzzle, json.new_difficulty, json.new_groups)


@app.post(paths.get('update_group_settings'))
async def update_group_settings(json: UpdateGroupSettings, response: Response):
    user = User(pw=json.pw, user_id=json.user_id)
    response.status_code = user.update_group_settings(json.group_id, json.allow_members_code, json.min_points)


@app.post(paths.get('upload_pfp'))
async def upload_pfp(response: Response, image: UploadFile = File(...), user_id: int = Form(...), pw: str = Form(...)):
    user = User(pw=pw, user_id=user_id)
    response.status_code = user.upload_pfp(image)


@app.get(paths.get('verify_account'))
async def verify_account(email_verify_token: str):
    return verify_account_web(email_verify_token)
