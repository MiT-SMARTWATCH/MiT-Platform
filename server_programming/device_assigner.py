"""
editor : hyeonLIB
email : lemonjames96@gmail.com
os : ubuntu 20.04
python version : 3.8
"""

from ipaddress import ip_address
from time import sleep
from time import *
import os
import time
import datetime
import pandas as pd
import psycopg2
from sqlalchemy import create_engine
import logging
import paho.mqtt.client as paho
# Importing models and REST client class from Community Edition version
from tb_rest_client.rest_client_ce import *
from tb_rest_client.rest import ApiException
print("successed to import all the libraries you need")

ip_address = 'localhost'
password = "sselab0812!"
# ThingsBoard URL
url = "http://localhost:8080"
# Default Tenant Administrator credentials
tb_username = "tenant@thingsboard.org"
tb_password = "tenant"


def createEngineAccount(password):
    engine_Account = psycopg2.connect(f'postgresql://postgres:{password}@127.0.0.1:5432/postgres')
    return engine_Account


def createEngineThingsboard(password):
    engine_Thingsboard = create_engine(f'postgresql://postgres:{password}@127.0.0.1:5432/thingsboard')
    return engine_Thingsboard


def MakeDeviceForRegister(user_id, password, url, tb_username, tb_password):
    engine_account = create_engine(f'postgresql://postgres:{password}@127.0.0.1:5432/postgres')

    device_name = user_id + "_smartwatch"

    # Thingsboard API
    logging.basicConfig(level=logging.DEBUG,
                        format='%(asctime)s - %(levelname)s - %(module)s - %(lineno)d - %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')

    # Creating the REST client object with context manager to get auto token refresh
    with RestClientCE(base_url=url) as rest_client:
        try:
            # Auth with credentials
            rest_client.login(username=tb_username, password=tb_password)

            # creating a Device
            # device = Device(name=device_name, type="smartwatch")
            device = Device(name=device_name, type="smartwatch", device_profile_id="a6140800-80fb-11ec-b3b9-19d4272987ca")
            device = rest_client.save_device(device)

            logging.info(" Device was created:\n%r\n", device)
        except ApiException as e:
            logging.exception(e)

    engine_Thingsboard = createEngineThingsboard(password)

    device_id =  pd.read_sql_query(sql = f"SELECT id FROM device WHERE name = '{device_name}'",con=engine_Thingsboard)
    device_id = device_id["id"][0]
    print(device_id)

    token_key = pd.read_sql_query(sql = f"SELECT credentials_id FROM device_credentials WHERE device_id = '{device_id}'",con=engine_Thingsboard)
    token_key = token_key["credentials_id"][0]
    print(token_key)

    engine_account.execute(f"UPDATE account SET device_id = '{device_id}', token_key = '{token_key}' Where user_id = '{user_id}';")

    engine_Thingsboard.dispose()
    engine_account.dispose()

    return token_key


# mqtt tools
def initialize_key(ACCESS_TOKEN):
    try:
        """
        tools to send the result to server
        """
        broker=ip_address
        port=1883 

        def on_publish(client,userdata,result):
            print(f"{ACCESS_TOKEN} - Has been connected successfully \n")
            pass
        # initialize_msg = '{}'
        initialize_msg = {'condition':'','condition_score':'','heart_rate':50,'state':'','steps':1,'survey1':'','survey2':'','survey3':'','symptom':''}
        
        command = "mosquitto_pub -d -q 1 -h "+"localhost "+ "-t "+"v1/devices/me/telemetry " + "-u " +ACCESS_TOKEN+' ' +'-m ' +f"\"{initialize_msg}\""
        print(command)
        os.system(command)
    except:
        print("There's something wrong with network")
        

engine_Account = createEngineAccount(password)
print("Successed to create engine")

current_timestamp = datetime.datetime.now()
print(f"log [{current_timestamp}] : Application has been started!")

while True:

    cursor = engine_Account.cursor()
    cursor.execute("SELECT user_id FROM account WHERE token_key IS NULL")

    user_id_list = []
    for row in cursor:
        for field in row:
            user_id_list.append(field)

    current_timestamp = datetime.datetime.now()

    if len(user_id_list) != 0:
        print("assigning log : ",user_id_list)
        for user_id in user_id_list:
            try:
                ACCESS_TOKEN = MakeDeviceForRegister(user_id, password, url, tb_username, tb_password)
                current_timestamp = datetime.datetime.now()
                print(f"log [{current_timestamp}] : {user_id}device assigned successfully!")
                initialize_key(ACCESS_TOKEN)
            except:
                current_timestamp = datetime.datetime.now()
                print(f"ERROR [{current_timestamp}] : error has been occured!")
            # ACCESS_TOKEN = MakeDeviceForRegister(user_id, password, url, tb_username, tb_password)
            # current_timestamp = datetime.datetime.now()
            # print(f"log [{current_timestamp}] : {user_id}device assigned successfully!")
            # initialize_key(ACCESS_TOKEN)

    else:
        print(f"log [{current_timestamp}] : None has tried to register in our application")

    sleep(10)
