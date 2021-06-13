package com.example.mit.MQTT

import android.content.Context
import android.util.Log
import com.example.mit.GoogleFit.StepCounter
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

fun MQTT(context: Context, total : String) {
    val TAG = "MqttClientHelper"

    val ip = "ip 주소"

    var serverURI = "tcp://$ip:1883"
    var mqttClient = MqttAndroidClient(context, serverURI, "SmartFarmerApp")

    mqttClient.setCallback(object: MqttCallback {
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
        }

        override fun connectionLost(cause: Throwable?) {
            Log.d(TAG, "Connection lost ${cause.toString()}")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            TODO("Not yet implemented")
        }
    })

    val options = mqttConnectionOption
    options.userName = "token"

    try {
        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d(TAG, "Connection success")

                while (true){
                    val random = Random()
                    val msg = "{\"steps\":$total}"
                    println("{\"steps\":$total}")
                    val message = MqttMessage()
                    message.payload = msg.toByteArray()
                    mqttClient.publish("topic 이름", message.payload, 1, false)
                    Thread.sleep(15000L)  // 15초
                    StepCounter().readData()
                }
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d(TAG, "Connection failure : $exception")
                Log.d(TAG, "Connection failure")
            }
        })
    }catch( e: MqttException){
        e.printStackTrace()
        Log.d(TAG, "Catch : $e")
    }
}

//private val disconnectedBufferOptions: DisconnectedBufferOptions
//    private get() {
//        val disconnectedBufferOptions = DisconnectedBufferOptions()
//        disconnectedBufferOptions.isBufferEnabled = true
//        disconnectedBufferOptions.bufferSize = 100
//        disconnectedBufferOptions.isPersistBuffer = true
//        disconnectedBufferOptions.isDeleteOldestMessages = false
//        return disconnectedBufferOptions
//    }
private val mqttConnectionOption: MqttConnectOptions
    private get() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.setWill("offline", "offline".toByteArray(), 1, true)
        return mqttConnectOptions
    }
