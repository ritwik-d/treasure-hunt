import redis

class ChatDB:
    def __init__(self, group_id: int):
        self.rdb = redis.Redis()
        self.group_id = group_id


    def send_message(user_id: int, message: str):
        self.rdb.hset(self.group_id, user_id, message)


    def get_messages():
        return self.rdb.hgetall(self.group_id)


x = ChatDB(10)
x.send_message(5, 'myfirstmessage')
print(x.get_messages())
