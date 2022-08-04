"""
editor : hyeonLIB
os : ubuntu 20.04
python version : 3.8
"""

import os
import time
import pandas as pd
import psycopg2
from sqlalchemy import create_engine
import argparse
import paho.mqtt.client as mqtt

import sql_function
# import hrosad_offline
# import rhrad_offline

# parser = argparse.ArgumentParser(description='function for postgresql')
# parser.add_argument('--ip_address', metavar='', help ='ip x of database server')
# parser.add_argument('--password',metavar='', help ='password for database')
# args = parser.parse_args()

# global ip_address, password
# ip_address = args.ip_address
# password = args.password

global ip_address, password
ip_address = '203.255.56.50'
password = 'sselab0812!'

"""
## extract data of each person (interval = 1 month)
## data sampling by timestamp -> 1 minute
## execute anomaly detection
## post anomaly points from csv files made be anomaly detection, key=anomaly_points ++ hros, rhr interpolated ones 
   -> mqtt library and add logs
"""

def extract_hrsteps(device_id):
    sql = 'SELECT * FROM ts_kv_dictionary;'
    keys = sql_function.extractData(sql, database_name='thingsboard')
    key_hr = keys[keys['key']=='heart_rate']['key_id'].item()
    key_steps = keys[keys['key']=='steps']['key_id'].item()

    sql = f"SELECT ts, long_v FROM ts_kv WHERE entity_id = '{device_id}' AND key = {key_hr};"  # + AND timestamp ?
    hr_data = sql_function.extractData(sql, database_name='thingsboard')
    sql = f"SELECT ts, long_v FROM ts_kv WHERE entity_id = '{device_id}' AND key = {key_hr};"
    steps_data = sql_function.extractData(sql, database_name='thingsboard')

    return hr_data, steps_data


# data sampling by timestamp -> 1 minute
def down_sampling(time, hr_data, steps_data):
    hr_data = hr_data.set_index(pd.to_datetime(hr_data['ts'], unit='ms')) 
    steps_data = steps_data.set_index(pd.to_datetime(steps_data['ts'], unit='ms'))
    hr_data = hr_data['long_v'] # needs column name?
    steps_data = steps_data['long_v']

    hr_data = hr_data.resample(time).mean()
    steps_data = steps_data.resample(time).sum() # is that right?

    return hr_data, steps_data


# execute anomaly detection
def anomaly_detection():
    print("hros")
    print("rhr")
    print("both")


# post anomaly points from csv files made by anomaly detection
# def


# log function
# def


# plot capturing
# def


# def run()



# def main(opt):
def main():
    ## extract data of each device in thingsboard database
    sql = 'SELECT device_id, credentials_id FROM device_credentials;' # list of devices
    devices_data = sql_function.extractData(sql, database_name='thingsboard')
    list_device_id = devices_data['device_id']
    list_token_key = devices_data['credentials_id']

    for device_id in list_device_id:
        hr_data, steps_data = extract_hrsteps(device_id)
        if hr_data.empty:
            pass
        
        else:
            print(device_id)
            sampling_rate = 'T'
            hr_data, steps_data = down_sampling(sampling_rate, hr_data, steps_data)



if __name__ == "__main__":
    # opt = parse_opt()
    # main(opt)
    main()
