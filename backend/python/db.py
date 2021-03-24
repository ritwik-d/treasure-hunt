from environment import *
import MySQLdb

db_connections = {}

class DB:
    def __init__(self, autocommit=True, db_config=None):
        if db_config is None:
            db_config = config.get('mysql')

        self.host = db_config.get('host')
        self.user = db_config.get('user')
        self.pw = db_config.get('pw')
        self.name = db_config.get('name')
        self.autocommit = autocommit
        self.db = None


    # creates a mysql connection or gets existing one
    def connect(self):
        key = ':'.join([self.host, self.user, self.pw, self.name])
        global db_connections
        db = db_connections.get(key)
        if db is None:
            db = MySQLdb.connect(self.host, self.user, self.pw, self.name)
            db.autocommit(self.autocommit)
            db_connections[key] = db
        self.db = db


    # executes select sql statements and returns in a dict/tuple
    def select(self, sql: str, params=None, dict_cursor=False):
        if self.db is None:
            return None
        cursor = None
        if dict_cursor:
            cursor = self.db.cursor(MySQLdb.cursors.DictCursor)
        else:
            cursor = self.db.cursor()
        if params:
            cursor.execute(sql, params)
        else:
            cursor.execute(sql)
        results = cursor.fetchall()
        return results


    # executes insert sql statments and returns last row id
    def insert(self, table_name: str, params: dict):
        if self.db is None:
            return None
        try:
            cursor = self.db.cursor()
            columns = params.keys()
            sql = f"""INSERT INTO {table_name} ({', '.join(columns)}) VALUES ({', '.join(['%s' for i in columns])})"""
            print(f'insert sql: {sql}')
            cursor.execute(sql, tuple(params.values()))
            return cursor.lastrowid
        except Exception as e:
            if e.args and e.args[0] == 1062:
                return False
            else:
                raise


    # update colums in mysql and returns last row id
    def update(self, table_name: str, update_params: dict, where_params: dict, aff_rows=False):
        if self.db is None:
            return None
        cursor = self.db.cursor()
        sql = f"""UPDATE {table_name} set {', '.join([f'{k} = %s' for k in update_params])} WHERE {', '.join([f'{k} = %s' for k in where_params])}"""
        print(f'update sql: {sql}')
        cursor.execute(sql, tuple(list(update_params.values()) + list(where_params.values())))
        if aff_rows:
            return self.db.affected_rows()
        return cursor.lastrowid


    # deletes rows from mysql and returns last row id
    def delete(self, table_name: str, where_params: dict):
        if self.db is None:
            return None
        cursor = self.db.cursor()
        sql = f"""DELETE FROM {table_name} WHERE {' and '.join([f'{k} = %s' for k in where_params])}"""
        params = tuple(where_params.values())
        print(f'delete sql: <{sql}>, params: {params}')
        cursor.execute(sql, params)
        return cursor.lastrowid


    def execute_sql(sql: str, params=None):
        if self.db is None:
            return None
        cursor = self.db.cursor()
        if params:
            cursor.execute(sql, params)
        else:
            cursor.execute(sql)
        return cursor.lastrowid
