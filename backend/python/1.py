from utils import *

status = send_email('1.txt', 'ritwik.deshpande.8@gmail.com', 'blah', html=False)
print(f'status: <{status}>')
