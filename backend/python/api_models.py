from pydantic import BaseModel

class LogIn(BaseModel):
    email: str
    pw: str


class Register(BaseModel):
    email: str
    fname: str
    lname: str
    pw: str
