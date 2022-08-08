from time import sleep
from time import *             #meaning from time import EVERYTHING
import time
import datetime
import pandas as pd

import psycopg2
from sqlalchemy import create_engine

con = psycopg2.connect(database="postgres", user="postgres", password="sselab0812!", host="localhost", port = "5432")
cur = con.cursor()

device_id = 'c4eebe50-80fb-11ec-b3b9-19d4272987ca'
token_key = 'k0hb4k2jW8bBo9oEUHzE'
user_id = 'aa'

# UPDATE
# sql = f"UPDATE account SET device_id = '{device_id}', token_key = '{token_key}' Where user_id = '{user_id}';"

# DELETE
user_id = 'jj'
sql = f"DELETE FROM account WHERE user_id = '{user_id}';"

cur.execute(sql)
con.commit()
con.close()