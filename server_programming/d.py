
from ipaddress import ip_address
from time import sleep
from time import *
import time
import datetime
import pandas as pd
import psycopg2
from sqlalchemy import create_engine
import logging
import paho.mqtt.client as paho
import argparse

parser = argparse.ArgumentParser(description='function for postgresql')
parser.add_argument('--ip',default = '203.255.56.50', metavar='', help ='ip x of database server')
parser.add_argument('--pw',metavar='',default = 'sselab0812!', help ='password for database')
args = parser.parse_args()

# global ip_address, password
ip_address = args.ip
password = args.pw


# mqtt tools
def initialize_key(ACCESS_TOKEN):
    broker='203.255.56.50'
    port=1883 

    def on_publish(client,userdata,result):
        print(f"{ACCESS_TOKEN} - Has been connected successfully \n")
        pass

    # initialize_msg = '{}'
    # initialize_msg = '{"condition":"","condition_score":"","heart_rate":50,"state":"","steps":1,"survey1":"","survey2":"","survey3":"","symptom":""}'
    # initialize_msg = '{' + '\''

    payload="{"
    payload+="\"Humidity\":65,"; 
    payload+="\"Temperature\":21"; 
    payload+="}"
    initialize_msg = '{"condition" : "0"}'
    client1= paho.Client("control")
    client1.on_publish = on_publish
    client1.username_pw_set(ACCESS_TOKEN)
    client1.connect(broker,port)
    client1.publish("v1/devices/me/telemetry",payload) #topic-v1/devices/me/telemetry
    print("hi")

token = 'XVvlK6WaVhezNSC3dXoL'
initialize_key(token)