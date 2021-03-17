from pydantic import BaseModel
from typing import Optional

class CreateChallenge(BaseModel):
    difficulty: str
    email: str
    group_name: Optional[str] = None
    latitude: float
    longitude: float
    name: str
    puzzle: str
    pw: str
    user_id: int


class CreateGroup(BaseModel):
    email: str
    name: str
    description: Optional[str] = None
    pw: str
    user_id: int


class DeleteChallenge(BaseModel):
    challenge_id: int
    email: str
    pw: str
    user_id: int


class GetChallengeData(BaseModel):
    email: str
    name: str
    pw: str
    user_id: int


class GetChallenges(BaseModel):
    email: str
    pw: str
    user_id: int


class GetGroupData(BaseModel):
    email: str
    name: str
    pw: str
    user_id: int


class GetGroups(BaseModel):
    email: str
    pw: str
    user_id: int


class GetUserChallenges(BaseModel):
    email: str
    pw: str
    user_id: int


class GetUserData(BaseModel):
    email: str
    pw: str
    user_id: int


class GetGroupMembers(BaseModel):
    email: str
    group_id: int
    pw: str
    user_id: int


class GetUsers(BaseModel):
    pass


class JoinGroup(BaseModel):
    email: str
    join_code: str
    pw: str
    user_id: int


class LogIn(BaseModel):
    email: str
    pw: str


class Register(BaseModel):
    email: str
    pw: str
    username: str


class UpdateChallenge(BaseModel):
    email: str
    pw: str
    user_id: str
    challenge_name: str
    new_name: str
    new_puzzle: str
    new_difficulty: str
    new_group_name: str
