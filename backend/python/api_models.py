from pydantic import BaseModel
from typing import Optional

class CreateChallenge(BaseModel):
    difficulty: str
    group_name: Optional[str] = None
    name: str
    puzzle: str
    pw: str
    user_id: int


class LogIn(BaseModel):
    email: str
    pw: str


class Register(BaseModel):
    email: str
    fname: str
    lname: str
    pw: str
    username: str
