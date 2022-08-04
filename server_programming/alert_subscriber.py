"""
editor : hyeonLIB
os : ubuntu 20.04
python version : 3.8
"""

import threading, time
from kafka import KafkaAdminClient, KafkaConsumer, KafkaProducer
from kafka.admin import NewTopic

# Kafka consumer 생성 후 alarm topic에 들어오는 warning message를 count
class Consumer(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.stop_event = threading.Event()

    def stop(self):
        self.stop_event.set()
    
    def run(self):
        consumer = KafkaConsumer(bootstrap_servers = 'localhost:9092',
                                auto_offset_reset='earliest',
                                consumer_timeout_ms=1000)
        consumer.subscribe(['alarm'])
        alert = []
        
        # 지속적으로 사용자에게 warning message 간 횟수를 counting 
        # 30분 이내에 다시 발생할 경우 counting, 30분 넘어가 발생하는 경우 다시 1부터
        while not self.stop_event.is_set():
            for message in consumer:
                user = message[0]
                value = message[1]
                timestamp = message[2]
                count = alert[user] + 1
                if user not in alert:
                    alert.append({user:count, 'timestamp':timestamp})
                else:
                    if time.now() - alert[user]['timestamp'] < 30*60
                    alert.append({'user':count})
                

                print(message)

                if self.stop_event.is_set():
                    break

        consumer.close()
