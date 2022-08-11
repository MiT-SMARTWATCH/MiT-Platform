"""
editor : hyeonLIB
email : lemonjames96@gmail.com
os : ubuntu 20.04
python version : 3.8
"""

import os
import time
import datetime
import pandas as pd
import psycopg2
from sqlalchemy import create_engine, values
import argparse
import paho.mqtt.client as paho
import sql_function
import hrosad_offline
# import rhrad_offline

parser = argparse.ArgumentParser(description='function for postgresql')
parser.add_argument('--ip',default = 'localhost', metavar='', help ='ip x of database server')
parser.add_argument('--pw',default = 'sselab0812!', metavar='', help ='password for database')
args = parser.parse_args()

global ip_address, password
ip_address = args.ip
password = args.pw

ip_address = '127.0.0.1'
password = 'sselab0812!'
result_path = './data/result'
log_path = './data/log'

if not os.path.exists(result_path): 
    plot_path = result_path+'/plot'
    csv_path = result_path+'/csv'
    os.makedirs(plot_path)
    os.makedirs(csv_path)
if not os.path.exists(log_path):
    os.makedirs(log_path)


"""
## extract data of each person (interval = 1 month)
## data sampling by timestamp -> 1 minute
## execute anomaly detection
## post anomaly points from csv files made be anomaly detection, key=anomaly_points ++ hros, rhr interpolated ones 
   -> mqtt library and add logs
"""

def extract_hrsteps(device_id): # need to apply datetime 
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
    hr_data['heartrate'] = hr_data['long_v'] 
    hr_data = hr_data.drop(columns=['long_v','ts'])
    steps_data['steps'] = steps_data['long_v'] 
    steps_data = steps_data.drop(columns=['long_v','ts'])
    hr_data = hr_data.resample(time).mean()
    steps_data = steps_data.resample(time).sum() 

    return hr_data, steps_data


# execute anomaly detection
def hros_anomaly_detection(hr_data, steps_data, device_id, visualize=False):
    try:
        model = hrosad_offline.HROSAD_offline()
        df1 = model.HROS(hr_data, steps_data)
        roll = 300
        df2 = model.pre_processing(df1, roll)
        sdHR = df2[['heartrate']]
        sdSteps = df2[['steps']]
        data_seasnCorec = model.seasonality_correction(sdHR, sdSteps)
        data_seasnCorec += 0.1
        std_data = model.standardization(data_seasnCorec)
        data = model.anomaly_detection(std_data)
        if visualize==True:
            model.visualize(data, device_id)

        return data
    except:
        print("Error has been occured while detecting anomalies")


# mqtt tools
def mqtt_message(device_id, ACCESS_TOKEN, msg):
    try:
        """
        tools to send the result to server
        """
        broker=ip_address
        port=1883

        def on_publish(client,userdata,result):
            print(f"{device_id} - Has been connected successfully \n")
            pass

        ''' 
        # client1= paho.Client("control")
        # client1.on_publish = on_publish
        # client1.username_pw_set(ACCESS_TOKEN)
        # client1.connect(broker,port)
        # payload = '{"e":2}'
        # client1.publish("v1/devices/me/telemetry",payload) #topic-v1/devices/me/telemetry
        '''
        
        command = "mosquitto_pub -d -q 1 -h "+"localhost "+ "-t "+"v1/devices/me/telemetry " + "-u " +ACCESS_TOKEN+' ' +'-m ' +f"\"{msg}\""
        # command = f'mosquitto_pub -d -q 1 -h localhost -t v1/devices/me/telemetry -u {ACCESS_TOKEN} -m "{msg}"'
        print(command)
        os.system(command)
        print(f"{device_id} - Abnormal points have been sent to the server \n")
    except:
        print("There's something wrong with network")


# post anomaly points from csv files made by anomaly detection
def data_to_mqtt(result, anomalies, device_id, ACCESS_TOKEN):
    try:
        for index in range(len(result)):
            timestamp = result.iloc[index,0]
            timestamp = int(datetime.datetime.timestamp(timestamp)*1000)
            value = result.iloc[index,1]
            msg = {"ts" : timestamp,"values":{"std_hr":value}}
            mqtt_message(device_id, ACCESS_TOKEN, msg)
    except:
        print("Error has been occured while sending mqtt data")
    
    try:
        for index in range(len(anomalies)):
            timestamp = anomalies.iloc[index,0]
            timestamp = int(datetime.datetime.timestamp(timestamp)*1000)
            value = anomalies.iloc[index,1]
            msg = {"ts" : timestamp,"values":{"abnormal_point":value}}
            mqtt_message(device_id, ACCESS_TOKEN, msg)
    except:
        print("Error has been occured while sending mqtt data")
        

# log function
# def



def main():
    try:
        ## extract data of each device in thingsboard database
        sql = 'SELECT device_id, credentials_id FROM device_credentials;' # list of devices
        devices_data = sql_function.extractData(sql, database_name='thingsboard')
        list_device = devices_data[['device_id','credentials_id']]
    except:
        print("There's something wrong with PostgreSQL database")

    print('****************************************************************')
    print('Device information')
    print(list_device)
    print('****************************************************************')
    for index in range(len(list_device)):
        print('----------------------------------------------------------------')
        device_id = list_device.iloc[index,0]
        ACCESS_TOKEN = list_device.iloc[index,1]
        print(device_id, ACCESS_TOKEN)

        hr_data, steps_data = extract_hrsteps(device_id)
        if hr_data.empty:
            pass
        else:
            try:
                sampling_rate = 'T'
                hr_data, steps_data = down_sampling(sampling_rate, hr_data, steps_data)
            except:
                print("Error has been occured while sampling")
            
            # result = hros_anomaly_detection(hr_data, steps_data, device_id, visualize=False)
            result = hros_anomaly_detection(hr_data, steps_data, device_id, visualize=True)

            anomalies = result.loc[result['anomaly'] == -1, ('index', 'heartrate')]
            df_abnormal = anomalies[(anomalies['heartrate']> 0)]
            
            data_to_mqtt(result, df_abnormal, device_id, ACCESS_TOKEN)
            

        # LOG



if __name__ == "__main__":
    # opt = parse_opt()
    # main(opt)
    main()
