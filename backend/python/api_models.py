from pydantic import BaseModel
from typing import Optional

class CompleteChallenge(BaseModel):
    challenge_id: int
    pw: str
    user_id: int


class CreateChallenge(BaseModel):
    difficulty: str
    groups: list
    latitude: float
    longitude: float
    name: str
    puzzle: str
    pw: str
    user_id: int


class CreateGroup(BaseModel):
    name: str
    description: Optional[str] = None
    pw: str
    user_id: int


class DeleteChallenge(BaseModel):
    challenge_id: int
    pw: str
    user_id: int


class DownloadPfp(BaseModel):
    pw: str
    user_id: int


class GetChallengeData(BaseModel):
    name: str
    pw: str
    user_id: int


class GetChallenges(BaseModel):
    pw: str
    user_id: int


class GetGroupData(BaseModel):
    name: str
    pw: str
    user_id: int


class GetGroups(BaseModel):
    pw: str
    user_id: int


class GetUserChallenges(BaseModel):
    pw: str
    user_id: int


class GetGroupMembers(BaseModel):
    group_id: int
    pw: str
    user_id: int


class GetGroupRow(BaseModel):
    group_name: str
    pw: str
    user_id: int


class GetGroupSettings(BaseModel):
    group_name: str
    pw: str
    user_id: int


class GetInvitations(BaseModel):
    pw: str
    user_id: int


class JoinGroup(BaseModel):
    join_code: str
    pw: str
    user_id: int


class LeaveGroup(BaseModel):
    group_name: str
    pw: str
    user_id: int


class LogIn(BaseModel):
    email: str
    pw: str
    is_hashed: Optional[int] = 0


class Register(BaseModel):
    email: str
    pw: str
    username: str


class RemoveGroupMember(BaseModel):
    group_id: int
    pw: str
    user_id: int
    username: str


class ResetPassword(BaseModel):
    email: str
    new_password: str


class SendEmailResetPassword(BaseModel):
    email: str


class SendMessage(BaseModel):
    pw: str
    user_id: int
    group_id: int
    message: str


class GetMessages(BaseModel):
    pw: str
    user_id: int
    group_id: int


class UpdateChallenge(BaseModel):
    pw: str
    user_id: str
    challenge_id: int
    new_latitude: float
    new_longitude: float
    new_puzzle: str
    new_difficulty: str
    new_group_name: str


class UpdateGroupSettings(BaseModel):
    pw: str
    user_id: str
    group_id: int
    allow_members_code: int
    min_points: int
