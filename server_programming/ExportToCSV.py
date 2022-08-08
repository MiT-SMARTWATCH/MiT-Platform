import pandas as pd
import psycopg2
from sqlalchemy import create_engine

# Database to csv for analysis
def exportToCSV(device_id):
    engine_thingsboard = create_engine('postgresql://postgres:sselab0812!@127.0.0.1:5432/thingsboard')

    metrics_data = pd.read_sql_query(sql=f"SELECT * FROM ts_kv WHERE entity_id = '{device_id}'", con = engine_thingsboard)
    filename = str(device_id)+"_data.csv"
    metrics_data.to_csv(f"./data/{filename}", index = False)

    engine_thingsboard.dispose()

engine_thingsboard = create_engine('postgresql://postgres:sselab0812!@127.0.0.1:5432/thingsboard')
users = pd.read_sql_query(sql=f"SELECT device_id from device_credentials", con = engine_thingsboard)
users = users["device_id"]
for device_id in users:
    print(device_id)
    exportToCSV(device_id)

engine_thingsboard.dispose()





# 22f29ec0-7ffa-11ec-8fb9-ef1137f3c6f0










# ----------------------------------------------------------------------------

# # timewindow_unix = 2,678,400 # 1 month

# def get_data(entity_id,timewindow,key):
#     start_date = time.now() - timewindow_unix
#     end_date = time.now()
#     sql_txt = f"SELECT long_v FROM ts_kv WHERE entity_id = {entity_id} and key = 36 and ts >= {start_date} and ts < {end_date}"
#     hr_data = pd.read_sql_query(sql=sql_txt,con=engine_Thingsboard)
#     hr_data.to_csv(f"./{entity_id}_hr.csv",index=False)


































# #################################################################################

# import threading, time

# from kafka import KafkaAdminClient, KafkaConsumer, KafkaProducer
# from kafka.admin import NewTopic

# class Consumer(threading.Thread):
#     def __init__(self):
#         threading.Thread.__init__(self)
#         self.stop_event = threading.Event()

#     def stop(self):
#         self.stop_event.set()
    
#     def run(self):
#         consumer = KafkaConsumer(bootstrap_servers = 'localhost:9092',
#                                 auto_offset_reset='earliest',
#                                 consumer_timeout_ms=1000)
#         consumer.subscribe(['alarm'])
#         alert = []

#         while not self.stop_event.is_set():
#             for message in consumer:
#                 user = message[0]
#                 value = message[1]
#                 timestamp = message[2]
#                 count = alert[user] + 1
#                 if user not in alert:
#                     alert.append({user:count, 'timestamp':timestamp})
#                 else:
#                     if time.now() - alert[user]['timestamp'] < 
#                     alert.append({'user':count})
                

#                 print(message)

#                 if self.stop_event.is_set():
#                     break

#         consumer.close()

# Consumer().run(self)

