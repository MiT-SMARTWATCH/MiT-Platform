package com.example.mit.mainhealthcare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.R
import com.google.android.material.snackbar.Snackbar


class Health_ToS : AppCompatActivity() {

    // 약관 동의
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_tos)

        //전체동의
        val checkBox = findViewById<CheckBox>(R.id.checkbox)
        //필수 서비스이용약관
        val checkBox2 = findViewById<CheckBox>(R.id.checkbox2)
        //필수 개인정보
        val checkBox3 = findViewById<CheckBox>(R.id.checkbox3)
        //선택 위치정보
        val checkBox4 = findViewById<CheckBox>(R.id.checkbox4)
        //
        val button = findViewById<Button>(R.id.button5)




        //전체동의 클릭시
        //전체 true / 전체 false 로 변경
        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                checkBox2.isChecked = true
                checkBox3.isChecked = true
                checkBox4.isChecked = true
                button.isEnabled = true


                button.setOnClickListener {

                    val intent = Intent(this, Health_signUp::class.java)
                    Toast.makeText(this, "약관동의가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                    // 약관동의 버튼이 다 클릭되었을 경우
                }
            } else {
                checkBox2.isChecked = false
                checkBox3.isChecked = false
                checkBox4.isChecked = false
                button.isEnabled = false
                Toast.makeText(this, "----------", Toast.LENGTH_SHORT).show()

            }
        }

        //2 클릭시 - 서비스 이용 약관
        checkBox2.setOnClickListener {
            //만약 전체 클릭이 true 라면 false로 변경
            if (checkBox.isChecked) { checkBox.isChecked = false
                //각 체크박스 체크 여부 확인해서  전체동의 체크박스 변경
            } else if (checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked) { checkBox.isChecked = true }
        }

        //3 클릭시 - 개인정보 이용 약관
        checkBox3.setOnClickListener {
            if (checkBox.isChecked) { checkBox.isChecked = false }
            else if (checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked) { checkBox.isChecked = true }
        }

        //4클릭시 - 위치정보 이용 약관
        checkBox4.setOnClickListener {
            if (checkBox.isChecked) { checkBox.isChecked = false}
            else if (checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked) { checkBox.isChecked = true }
        }

        // 상세 약관 보여주기
        //이용약관 버튼 - 서비스 이용약관
        val btn_agr: Button = findViewById(R.id.btn_agr)
        btn_agr.setText(R.string.underlined_text)
        btn_agr.setOnClickListener {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_tos, null)
            val textView: TextView = view.findViewById(R.id.textView_t)
            textView.setText(R.string.app_arg1)
            val textView1: TextView = view.findViewById(R.id.textView_c)
            textView1.setText(R.string.app_arg1_1)

            val alertDialog = AlertDialog.Builder(this)
                    .setTitle("서비스 이용약관")
                    .setPositiveButton("동의") { _, _ -> checkBox2.isChecked = true }
                    .setNeutralButton("비동의", null)
                    .create()

            alertDialog.setView(view)
            alertDialog.show()
        }

        //이용약관 버튼2 - 위치 정보 이용 약관
        val btn_agr2: Button = findViewById(R.id.btn_agr2)
        btn_agr2.setText(R.string.underlined_text)
        btn_agr2.setOnClickListener {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_tos, null)
            val textView: TextView = view.findViewById(R.id.textView_t)
            textView.setText(R.string.app_arg2)
            val textView1: TextView = view.findViewById(R.id.textView_c)
            textView1.setText(R.string.app_arg2_1)


            val alertDialog = AlertDialog.Builder(this)
                    .setTitle("개인 정보 이용 약관")
                    .setPositiveButton("동의") { _, _ -> checkBox3.isChecked = true }
                    .setNeutralButton("비동의", null)
                    .create()

            alertDialog.setView(view)
            alertDialog.show()
        }


        //이용약관 버튼3 - 개인정보처리방침
        val btn_agr3: Button = findViewById(R.id.btn_agr3)
        btn_agr3.setText(R.string.underlined_text)
        btn_agr3.setOnClickListener {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_tos, null)
            val textView: TextView = view.findViewById(R.id.textView_t)
            textView.setText(R.string.app_arg3)
            val textView1: TextView = view.findViewById(R.id.textView_c)
            textView1.setText(R.string.app_arg3_1)

            val alertDialog = AlertDialog.Builder(this)
                    .setTitle("위치 정보 처리방침")
                    .setPositiveButton("동의") { _, _ -> checkBox4.isChecked = true }
                    .setNeutralButton("비동의", null)
                    .create()

            alertDialog.setView(view)
            alertDialog.show()
        }
    }
}
