from ipaddress import ip_address
import paho.mqtt.client as paho             #mqtt library
import os
import json
import time
from datetime import datetime

# mqtt tools
def mqtt_message(device_id, ACCESS_TOKEN):
    """
    tools to send the result to server
    """
    print("hello")
    # import context  # Ensures paho is in PYTHONPATH
    # import paho.mqtt.client as mqtt
    # import paho.mqtt.publish as publish

    # msgs = [{'topic': "paho/test/multiple", 'payload': "multiple 1"}, ("paho/test/multiple", "multiple 2", 0, False)]
    # publish.multiple(msgs, hostname="mqtt.eclipseprojects.io")
    
    import paho.mqtt.client as mqtt #import the client1
    broker_address="203.255.56.50" 
    #broker_address="iot.eclipse.org" #use external broker

    client = mqtt.Client(device_id)
    client.connect(broker_address)
    topic_name = ''
    client.publish("house/main-light","OFF")


def message(device_id, ACCESS_TOKEN, message):
    # ACCESS_TOKEN='XVvlK6WaVhezNSC3dXoL' 
    broker=ip_address
    port=1883 

    def on_publish(client,userdata,result):
        print("Abnormal points have been sent to the server \n")
        pass

    client1= paho.Client("control")
    client1.on_publish = on_publish
    client1.username_pw_set(ACCESS_TOKEN)
    client1.connect(broker,port)

    # # message
    # payload="{"
    # payload+="\"Humidity\":60,"; 
    # payload+="\"Temperature\":25"; 
    # payload+="}"
    client1.publish("v1/devices/me/telemetry",message) #topic-v1/devices/me/telemetry