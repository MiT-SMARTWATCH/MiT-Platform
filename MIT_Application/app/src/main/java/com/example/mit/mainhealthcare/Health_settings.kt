package com.example.mit.mainhealthcare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.databinding.ActivitySettingsBinding
import com.example.mit.mainhealthcare.Alarm.Time_Settings
import java.sql.Time


class Health_settings : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.layoutLogout.setOnClickListener {
            intent = Intent(this, Health_main::class.java)
            startActivity(intent)

        }

        binding.layoutSurvay.setOnClickListener {
            intent = Intent(this, Time_Settings ::class.java)
            startActivity(intent)
        }

        // 문의 클릭시
        binding.layoutContact.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            val address = arrayOf("dbsfls00@gnu.ac.kr")
            email.putExtra(Intent.EXTRA_EMAIL, address)
            email.putExtra(Intent.EXTRA_SUBJECT, "MIT health 애플리케이션 문의 메일")
            email.putExtra(Intent.EXTRA_TEXT, "[애플리케이션 문의]\n \n " +
                    "기기명 (Device):\n안드로이드 OS (Android OS):\n내용 (Content):\n")
            startActivity(email)
        }

    }
}