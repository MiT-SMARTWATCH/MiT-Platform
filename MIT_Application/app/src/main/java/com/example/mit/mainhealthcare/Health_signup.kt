package com.example.mit.mainhealthcare

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.R
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*


class Health_signUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_signup)


        val button1: Button = findViewById(R.id.signup_ok) // 회원가입 완료 버튼
        val button2: Button = findViewById(R.id.id_check_btn) //아이디 중복확인 버튼

        val sign_id: EditText = findViewById(R.id.sign_up_id) //아이디
        val sign_pw: EditText = findViewById(R.id.sign_up_pw) //비밀번호
        val sign_name: EditText = findViewById(R.id.name) //이름
        val sign_birt: EditText = findViewById(R.id.birth) //생일

        //val gender : RadioGroup = findViewById(R.id.gender)



        // 아이디 중복 체크 구현
        button2.setOnClickListener {
            val ID: String = sign_id.text.toString()
            id_check("$ID")
        }



        //postgreSQL로 입력값 전달
        button1.setOnClickListener {

            val gender : RadioGroup = findViewById(R.id.gender)
            val GENDER = when (gender.checkedRadioButtonId) {
                R.id.male -> "남"
                else -> "여"
            }

            val ID: String = sign_id.text.toString() //아이디
            val PW: String = sign_pw.text.toString() //패스워드
            //val PW_CHECK = sign_pw_check.text.toString()
            val NAME: String = sign_name.text.toString() //이름
            val BIRTH: String = sign_birt.text.toString() //생일

            //입력받은 값들을 데이터 베이스에 전송

            connect("$ID", "$PW", "$NAME", "$BIRTH", "$GENDER")


        }




    }

    /** postgreSQL 연결 및 botton1 클릭 시 값 전달 */
    private fun connect(ID: String, PW: String, NAME: String, BIRTH: String, GENDER: String) {

        //이 부분 없으면 오류 이유 파익 x
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val jdbcURL = "jdbc:postgresql://ip 주소:5432/postgres"
        val username = "postgres" // 유저 이름
        val password = "" //비밀번호


        try {
            val connection = DriverManager.getConnection(jdbcURL, username, password) //연결한다,
            println("Connected to PostgreSQL server")

            /** 입력 */
            // 쿼리에 입력한다.
            var sql = "INSERT INTO account ( 아이디, 패스워드, 이름, 생년월일, 성별)" + " VALUES (?,?,?,?,?)"

            val statement: PreparedStatement = connection.prepareStatement(sql)

            // 이 값을 테이블에 넣음
            statement.setString(1, "$ID")
            statement.setString(2, "$PW")
            statement.setString(3, "$NAME")
            statement.setString(4, "$BIRTH")
            statement.setString(5, "$GENDER")


            val rows = statement.executeUpdate()


            if (rows > 0) {
                println("A new contact has been inserted.")
                val intent = Intent(this, Health_data_signup::class.java)
                intent.putExtra("ID", ID) //입력받은 값 Health_data_signup으로 전달
                intent.putExtra("NAME",NAME)
                intent.putExtra("BIRTH", BIRTH)
                intent.putExtra("GENDER",GENDER)

                print("===== $ID,$NAME,$BIRTH,$GENDER =====")
                Toast.makeText(this, " 회원가입 완료입니다.", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
            connection.close()

        } catch (e: SQLException) {
            println("Error in connected to PostgreSQL server")
            e.printStackTrace()
            Toast.makeText(this, " 회원가입 실패입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //아이디 중복확인
    private fun id_check(ID: String) {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val jdbcURL = "jdbc:postgresql://ip 주소:5432/postgres"
        val username = "postgres" // 유저 이름
        val password = "" // 비밀번호



        try {
            val connection = DriverManager.getConnection(jdbcURL, username, password) //연결한다,
            println("Connected to PostgreSQL server")

            /** 입력 */
            // 쿼리에 입력한다.
            var sql = "SELECT EXISTS (SELECT * FROM account WHERE 아이디 = '$ID') AS success;"
            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)

            while (result.next()) {
                //입력되 아이디값이 중복되는지 확인
                val output = result.getBoolean("success")

                if (output == true) {
                    //중복될 경우
                    Toast.makeText(this, "아이디가 중복됩니다.", Toast.LENGTH_SHORT).show()
                } else {
                    //중복아닌 경우
                    Toast.makeText(this, "아이디 사용이 가능합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            connection.close()
        } catch (e: SQLException) {
            println("Error in connected to PostgreSQL server")
            e.printStackTrace()
        }

    }
}



