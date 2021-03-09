from pydantic import BaseModel

class LogIn:
    email: str
    pw: str


class Register:
    email: str
    fname: str
    lname: str
    pw: str
