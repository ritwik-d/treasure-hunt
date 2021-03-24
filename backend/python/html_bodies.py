from environment import *
from fastapi.responses import HTMLResponse

class HTMLBody:
    def __init__(self, file_name: str):
        file_name = config.get('paths', 'html_bodies') + file_name
        with open(file_name, 'r') as f:
            self.html = f.read()


    def params(self, *params):
        self.html = self.html.format(*params)


    def get_response(self, status_code=200):
        return HTMLResponse(content=self.html, status_code=status_code)
