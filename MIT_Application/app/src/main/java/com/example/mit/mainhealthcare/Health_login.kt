package com.example.mit.mainhealthcare

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.GoogleFit.logger.Log
import com.example.mit.R
import java.net.InetAddress
import java.net.NetworkInterface
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*


class Health_login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_login)

        val button1: Button = findViewById<Button>(R.id.login_btn)
        val button2: Button = findViewById(R.id.sign_up_btn)
        val login_id: EditText = findViewById(R.id.login_id)
        val login_pw: EditText = findViewById(R.id.login_pw)

        button2.setOnClickListener {
            val nextsignup = Intent(this, Health_signUp::class.java)
            startActivity(nextsignup)
        }

        button1.setOnClickListener {

            val PW = login_pw.text.toString()
            val ID = login_id.text.toString()
            login("$ID", "$PW")

            //아이디 입력창에 입력된 정보를 문자형식으로 저장 -> Health_data ID_data 로 출력

//            val intent = Intent(this,Health_data::class.java)
//            intent.putExtra("ID", login_id.text.toString())
//            startActivity(intent)

        }
    }





    private fun login(ID : String, PW : String) {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        val jdbcURL = "jdbc:postgresql://ip 주소:5432/postgres"
        val username = "postgres" // 유저이름
        val password = "" //비밀번호

        try {
            val connection = DriverManager.getConnection(jdbcURL, username, password)
            println("Connected to PostgreSQL server")
            val sql = "SELECT 패스워드 FROM account WHERE 아이디 = '$ID'"
            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)

            println("로그인 : 1차")

            while (result.next()) {
                val password = result.getString("패스워드")
                //System.out.print("패스워드 : $password")

                println("로그인 : 2차")

                if (PW == password) {

                    /**입력한 패스워드와 데이터베이스의 패스워드가 같을 경우 해당하는 아이디의
                     이름, 성별, 이름을 데이터베이스에서 검색한다.
                     * */
                    val sql1 = "SELECT 생년월일 FROM account WHERE 아이디 = '$ID'"
                    val statement1 = connection.createStatement()
                    val result1 = statement1.executeQuery(sql1)

                    val sql2 = "SELECT 성별 FROM account WHERE 아이디= '$ID'"
                    val statement2 = connection.createStatement()
                    val result2 = statement2.executeQuery(sql2)

                    val sql3 = "SELECT 이름 FROM account WHERE 아이디='$ID'"
                    val statement3 = connection.createStatement()
                    val result3 = statement3.executeQuery(sql3)


                    while (result1.next() and result2.next() and result3.next() ) {
                        //and result4.next() and result5.next()

                        val birth = result1.getString("생년월일")
                        val gender = result2.getString("성별")
                        val name = result3.getString("이름")

                        if (birth != null && gender != null && name != null ) {
                            //&& height != null && weight != null

                            val intent = Intent(this, Health_scroll::class.java)
                            Toast.makeText(this, " 로그인 완료입니다.", Toast.LENGTH_SHORT).show()


                            intent.putExtra("GENDER", gender)
                            intent.putExtra("ID", ID)
                            intent.putExtra("BIRTH", birth)
                            intent.putExtra("NAME", name)
                            println("-------------------------------------생일 : $birth")
                            println("-------------------------------------아이디 : $ID")
                            println("-------------------------------------성별 : $gender")
                            println("-------------------------------------이름 : $name")

                            startActivity(intent)
                            finish()
                        }
                    }

                } else {
                    Toast.makeText(this, "로그인 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

            }

            connection.close()

        } catch (e: SQLException) {
            println("Error in connected to PostgreSQL server")
            Toast.makeText(this, " 로그인 실패했습니다.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
