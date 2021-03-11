from pydantic import BaseModel
from typing import Optional

class CreateChallenge(BaseModel):
    difficulty: str
    group_name: Optional[str] = None
    latitude: float
    longitude: float
    name: str
    puzzle: str
    pw: str
    user_id: int


class CreateGroup(BaseModel):
    name: str
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


class GetUserData(BaseModel):
    pw: str
    user_id: int


class GetGroupMembers(BaseModel):
    group_id: int
    pw: str
    user_id: int


class JoinGroup(BaseModel):
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


class VerifyEmail(BaseModel):
    email: str


class VerifyUsername(BaseModel):
    username: str
