package com.example.test_project

import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button

class MainActivity :WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1_yes = findViewById<Button>(R.id.button3)
        button1_yes.setOnClickListener{
            val intent = Intent(this, main_menu::class.java)
            startActivity(intent)
        }

        val button2_no = findViewById<Button>(R.id.button4)
        button2_no.setOnClickListener {
            finish()
        }
        setAmbientEnabled()
    }

}