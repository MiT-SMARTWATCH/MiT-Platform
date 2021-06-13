package com.example.mit.mainhealthcare

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.GoogleFit.HeartRate
import com.example.mit.GoogleFit.StepCounter
import com.example.mit.R


class Health_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_main)

        val button1 : Button = findViewById(R.id.title_login)
        val button2 : Button = findViewById(R.id.title_signup)

        // 로그인
        button1.setOnClickListener {
            val nextlogin = Intent(this,Health_scroll::class.java) //Health_login 바꾸기
            startActivity(nextlogin)
        }

        //회원가입
        button2.setOnClickListener {
            val nextsignup = Intent(this, Health_ToS ::class.java)
            startActivity(nextsignup)
        }

    }



}