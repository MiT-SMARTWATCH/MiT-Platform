package com.example.mit.mainhealthcare

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mit.databinding.ActivitySurveyBinding
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class Activity_survey : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding


    //mqtt로 데이터 보내기
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_survey)
        val topic = "topic "
        val mqttAndroidClient = MqttAndroidClient(this, "tcp://" + "ip 주소" + ":1883", MqttClient.generateClientId())

        try {
            val options = MqttConnectOptions()
            options.userName = "token" // 토큰
            mqttAndroidClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(com.example.mit.GoogleFit.TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure $exception")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
            Log.e("connect_fail", "Failure $e")

        }

        mqttAndroidClient.setCallback(object : MqttCallback {
            //클라이언트의 콜백을 처리하는부분
            override fun connectionLost(cause: Throwable) {}

            @Throws(Exception::class)
            override fun messageArrived(
                topic: String,
                message: MqttMessage
            ) {    //모든 메시지가 올때 Callback method
                if (topic == "$topic") {     //topic 별로 분기처리하여 작업을 수행할수도있음
                    val msg = String(message.payload)
                    Log.e("arrive message : ", msg)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}

        })


        binding = ActivitySurvyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSur.setOnClickListener {

            /** 설문 값들을 저장할 수 있는 변수 선언  */
            val one = mutableListOf<String>() //1번
            val two = mutableListOf<String>() //2번

            if (binding.checkBox1.isChecked) {
                one.add("normal")
            }
            //"평소와 유사"}

            if (binding.checkBox2.isChecked) {
                one.add("stress")
            }
            // "피곤/스트레스" }

            if (binding.checkBox3.isChecked) {
                one.add("beginningillness")
            }
            // "질병초기" }

            if (binding.checkBox4.isChecked) {
                one.add("currentlyill")
            }
            // "질병" }

            if (binding.checkBox5.isChecked) {
                one.add("recovered")
            }
            // "질병회복" }

            if (binding.checkBox6.isChecked) {
                one.add("other")
            }

            ///////////////////////////////////////////"1-1기타" }

            if (binding.checkBox7.isChecked) {
                two.add("headaches")
            }
            // "두통" }

            if (binding.checkBox8.isChecked) {
                two.add("stomachache")
            }
            // "복통" }

            if (binding.checkBox9.isChecked) {
                two.add("cough")
            }
            //"기침" }

            if (binding.checkBox10.isChecked) {
                two.add("fatigue")
            }
            // "피로" }

            if (binding.checkBox11.isChecked) {
                two.add("chill")
            }
            //"오한" }

            if (binding.checkBox12.isChecked) {
                two.add("chill")
            }
            // "설사" }

            if (binding.checkBox13.isChecked) {
                two.add("sorethroat")
            }
            // "인후통" }

            if (binding.checkBox14.isChecked) {
                two.add("dyspnea")
            }
            //"호흡곤란" }

            if (binding.checkBox15.isChecked) {
                two.add("loseoftaste")
            }
            // "미각상실" }

            if (binding.checkBox16.isChecked) {
                two.add("Ocularpain")
            }
            // "안구통증" }

            if (binding.checkBox17.isChecked) {
                two.add("blurredvision")
            }
            // "시야 흐림" }

            if (binding.checkBox18.isChecked) {
                two.add("iddiness")
            }
            // "어지러움" }

            if (binding.checkBox19.isChecked) {
                two.add("lossofappetite")
            }
            // "식욕부진" }

            if (binding.checkBox20.isChecked) {
                two.add("other")
            }
            // "기타 " }





            // 설문 내용이 포함된 리스트 출력

            println("========================")
            println("$one")
            println("$two")
            println("========================")

            try {
                val msg = "{\"survey\":$one}" // 1번 질문
                val msg2 = "{\"survey2\":$two}" //2번 질문

                val message = MqttMessage()
                message.payload = msg.toByteArray()
                message.payload = msg2.toByteArray()

                mqttAndroidClient.publish("$topic", message.payload, 0, false)
                Log.d(com.example.mit.GoogleFit.TAG, "보낸 값 : $msg")
                Log.d(com.example.mit.GoogleFit.TAG, "보낸 값 : $msg2")
            } catch (e: MqttException) {
                e.printStackTrace()
            }


        }
    }

//    private val disconnectedBufferOptions: DisconnectedBufferOptions
//        private get() {
//            val disconnectedBufferOptions = DisconnectedBufferOptions()
//            disconnectedBufferOptions.isBufferEnabled = true
//            disconnectedBufferOptions.bufferSize = 100
//            disconnectedBufferOptions.isPersistBuffer = true
//            disconnectedBufferOptions.isDeleteOldestMessages = false
//            return disconnectedBufferOptions
//        }
//    private val mqttConnectionOption: MqttConnectOptions
//        private get() {
//            val mqttConnectOptions = MqttConnectOptions()
//            mqttConnectOptions.isCleanSession = false
//            mqttConnectOptions.isAutomaticReconnect = true
//            mqttConnectOptions.setWill("offline", "offline".toByteArray(), 1, true)
//            return mqttConnectOptions
//        }
}

//
//    fun thread() {
//
//        // 실행간격 지정(3초)
//        val sleepSec = 3
//
//        // 시간 출력 포맷
//        val fmt = SimpleDateFormat("HH:mm:ss")
//        // 주기적인 작업을 위한
//        val exec = ScheduledThreadPoolExecutor(1)
//        exec.scheduleAtFixedRate({
//            try {
//                val cal = Calendar.getInstance()
//
//                // 콘솔에 현재 시간 출력
//                println(fmt.format(cal.time))
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//
//                // 에러 발생시 Executor를 중지시킨다
//                exec.shutdown()
//            }
//        }, 0, sleepSec.toLong(), TimeUnit.SECONDS)
//    }
