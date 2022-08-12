"""
editor : hyeonLIB
os : ubuntu 20.04
python version : 3.8
"""

import pandas as pd
import psycopg2
from sqlalchemy import create_engine


"""
## as arguments ##
# parser = argparse.ArgumentParser(description='function for postgresql')
# parser.add_argument('--ip_address', metavar='', help ='ip address of database server')
# parser.add_argument('--password',metavar='', help ='password for database')
# parser.add_argument('--database_name',metavar='', help ='database name')
# args = parser.parse_args()

# ip_address = args.ip_address
# password = args.password
# database_name = args.database_name
"""


# set ip, password, database name
global ip_address, password
ip_address = '203.255.56.50'
password = 'sselab0812!'


def exportToCSV(device_id, database_name, ip_address=ip_address, password=password):
    # Database to csv for analysis
    engine_thingsboard = create_engine(f'postgresql://postgres:{password}@{ip_address}:5432/{database_name}')
    #sql_sen = "SELECT * FROM ts_kv_dictionary"
    sql_sen = f"SELECT * FROM ts_kv WHERE entity_id = '{device_id}'"
    metrics_data = pd.read_sql_query(sql=sql_sen, con = engine_thingsboard)
    filename = str(device_id)+"_data.csv"
    #filename = "key_dictionary.csv"
    metrics_data.to_csv(f"./data/{filename}", index = False)

    engine_thingsboard.dispose()

def extractData(sql, database_name, ip_address=ip_address, password=password):
    # Extract data from database
    # Return pandas dataframe
    engine_postgresql = create_engine(f'postgresql://postgres:{password}@{ip_address}:5432/{database_name}')

    metrics_data = pd.read_sql_query(sql=sql, con = engine_postgresql)
    
    engine_postgresql.dispose()

    return metrics_data

def executeSQL(sql, database_name, ip_address=ip_address, password=password):
    # Execute sql query without return
    con = psycopg2.connect(database=database_name, user="postgres", password=password, host=ip_address, port = "5432")

    cur = con.cursor()
    cur.execute(sql)

    con.commit()
    con.close()
