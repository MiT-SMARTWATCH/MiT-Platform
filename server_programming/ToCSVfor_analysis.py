import psycopg2
from sqlalchemy import create_engine
import pandas as pd

timewindow_unix = 2,678,400 # 1 month

def get_data(entity_id,timewindow,key):
    start_date = time.now() - timewindow_unix
    end_date = time.now()
    sql_txt = f"SELECT long_v FROM ts_kv WHERE entity_id = {entity_id} and key = 36 and ts >= {start_date} and ts < {end_date}"
    hr_data = pd.read_sql_query(sql=sql_txt,con=engine_Thingsboard)
    hr_data.to_csv(f"./{entity_id}_hr.csv",index=False)
