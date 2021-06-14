package com.example.test_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Button
import androidx.core.os.postDelayed
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*


class main_menu : WearableActivity() {
//    var handler = Handler()
//    var runnable = Runnable {
//        connect()
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)


        val button3_graph = findViewById<Button>(R.id.button3)
        button3_graph.setOnClickListener {
            val intent = Intent(this, view_graph::class.java)
            startActivity(intent)
        }

        val button4_anomaly = findViewById<Button>(R.id.button4)
        button4_anomaly.setOnClickListener{
            val intent2 = Intent(this, anomaly_list::class.java)
            startActivity(intent2)
        }

        setAmbientEnabled()
    }



}

