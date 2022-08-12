from time import sleep
from time import *             #meaning from time import EVERYTHING
import time
import datetime
import pandas as pd
import sql_function as sql_f
import psycopg2
from sqlalchemy import create_engine

#con = psycopg2.connect(database="postgres", user="postgres", password="sselab0812!", host="localhost", port = "5432")
#cur = con.cursor()
#token_key = 'k0hb4k2jW8bBo9oEUHzE'
#user_id = 'aa'

# UPDATE
# sql = f"UPDATE account SET device_id = '{device_id}', token_key = '{token_key}' Where user_id = '{user_id}';"

# DELETE
#user_id = 'jj'
#sql = f"DELETE FROM account WHERE user_id = '{user_id}';"

#cur.execute(sql)
#con.commit()
#con.close()


global ip_address, password
ip_address = '203.255.56.50'
password = 'sselab0812!'
test1 = 'thingsboard'
device_id = 'e0a29b20-9de4-11ec-9f42-1fa5eee10f3f'
sql_f.exportToCSV(device_id, test1 )



