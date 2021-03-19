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
    description: Optional[str] = None
    pw: str
    user_id: int


class DeleteChallenge(BaseModel):
    challenge_id: int
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


class GetUserData(BaseModel):
    pw: str
    user_id: int


class GetGroupMembers(BaseModel):
    group_id: int
    pw: str
    user_id: int


class GetUsers(BaseModel):
    pass


class JoinGroup(BaseModel):
    join_code: str
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


class UpdateChallenge(BaseModel):
    pw: str
    user_id: str
    challenge_name: str
    new_name: str
    new_puzzle: str
    new_difficulty: str
    new_group_name: str
