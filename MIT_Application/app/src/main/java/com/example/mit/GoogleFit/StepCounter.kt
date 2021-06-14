package com.example.mit.GoogleFit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mit.BuildConfig
import com.example.mit.R
import com.example.mit.mainhealthcare.Health_main
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.material.snackbar.Snackbar
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.text.Typography.times

val TAG = "StepCounter"
enum class FitActionRequestCode { SUBSCRIBE, READ_DATA }
// eum class : 성공적으로 로그인한 후 수행할 수 있는 작업을 정의하는 데 사용
//              이 값 중 하나가 Fit 로그인으로 전달되어 콜백에 성공하여 원하는 작업을 나중에 실행할 수 있습니다.

class StepCounter: AppCompatActivity() {

    private val fitnessOptions = FitnessOptions.builder()
        .accessActivitySessions(FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .build()

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stepcounter)

        val start: Button = findViewById(R.id.start)
        val stop: Button = findViewById(R.id.stop)

        //실행간격 지정(10초)
        val sleepSec = 15

        // 시간 출력 포맷
        val fmt = SimpleDateFormat("HH:mm:ss")
        // 주기적인 작업을 위한
        val exec = ScheduledThreadPoolExecutor(1)
        exec.scheduleAtFixedRate({
            try {
                val cal = Calendar.getInstance()
                // 콘솔에 현재 시간 출력
                println(fmt.format(cal.time))
                println("*****************" + cal.time)

                fitSignIn(FitActionRequestCode.READ_DATA)
                checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 발생시 Executor를 중지시킨다
                exec.shutdown()
            }
        }, 0, sleepSec.toLong(), TimeUnit.SECONDS)

//        fitSignIn(FitActionRequestCode.READ_DATA)
//        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

        start.setOnClickListener {
            fitSignIn(FitActionRequestCode.READ_DATA)
            checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)
        }

        stop.setOnClickListener {
            Toast.makeText(this, "아직 구현되지 않음", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) {
            fitSignIn(fitActionRequestCode)
        } else {
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    /** 구글 피트니스 로그인 확인
     * 사용자가 로그인했는지 확인하고, 로그인된 경우 지정된 기능을 실행합니다.
     * 사용자가 로그인하지 않은 경우, 로그인 후 함수를 지정하여 로그인을 시작합니다.
     */

    private fun fitSignIn(requestCode: FitActionRequestCode) {
        if (oAuthPermissionsApproved()) {
            performActionForRequestCode(requestCode)
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                    this,
                    requestCode.ordinal,
                    getGoogleAccount(), fitnessOptions
                )
            }
        }
    }

    /** 로그인이 되었을 경우
     *  로그인 콜백을 처리
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> {
                val postSignInAction = FitActionRequestCode.values()[requestCode]
                performActionForRequestCode(postSignInAction)
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    /** 구글피트니스 로그인 전달되고 성공 콜백과 함께 반환됩니다.
     * 이를 통해 호출자는 로그인 방법을 지정할 수 있습니다.
     */

    private fun performActionForRequestCode(requestCode: FitActionRequestCode) =
        when (requestCode) {
            FitActionRequestCode.READ_DATA -> readData()
            //FitActionRequestCode.READ_DATA -> historyAPI()
            FitActionRequestCode.SUBSCRIBE -> subscribe()
        }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(
        getGoogleAccount(),
        fitnessOptions
    )


    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    /** 구글피트니스 로그인 전달되고 성공 콜백과 함께 반환됩니다.
     * 이를 통해 호출자는 로그인 방법을 지정할 수 있습니다.
     */
    private fun subscribe() {

        Fitness.getRecordingClient(this, getGoogleAccount())
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Successfully subscribed!")
                } else {
                    Log.w(TAG, "There was a problem subscribing.", task.exception)
                }
            }
    }



    private fun dumpDataSet(dataSet: DataSet) {
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i(TAG,"Data point:")
            Log.i(TAG,"\tType: ${dp.dataType.name}")
            Log.i(TAG,"\tStart: ${dp.getStartTimeString()}")
            Log.i(TAG,"\tEnd: ${dp.getEndTimeString()}")
            for (field in dp.dataType.fields) {
                Log.i(TAG,"\tField: ${field.name} Value: ${dp.getValue(field)}")
            }
        }
    }

    private fun DataPoint.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    private fun DataPoint.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    /** 구글피트니스 로그인 전달되고 성공 콜백과 함께 반환됩니다.
     * 이를 통해 호출자는 로그인 방법을 지정할 수 있습니다.
     */
    fun readData() {


        val data_list = mutableListOf<Int>()

        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1) // 기간 : 현재 1주일로 선택
        Log.i(TAG, "Range Start: $startTime")
        Log.i(TAG, "Range End: $endTime")

        // 오늘 걸음수 데이터
        Fitness.getHistoryClient(this, getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                var total = when { //total 읽어옴
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                }

                var text7 : TextView = findViewById(R.id.textView7)
                //data_list.add(total)
                text7.text = "총 걸음 수 : $total" // total 출력
                Log.i(TAG, "Total steps: $total")



                val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                    .build()

                // 과거 데이터 출력
                Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                    .readData(readRequest)
                    .addOnSuccessListener { response ->
                        for (dataSet in response.buckets.flatMap { it.dataSets }) {
                            dumpDataSet(dataSet)

                            val hit_total = when { // hit_total : 과거 데이터 값
                                dataSet.isEmpty -> 0
                                else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                            }
                            //Log.i(TAG, "과거 걸음 수 : $hit_total")

                            data_list.add(hit_total) //mutableList에 나오는 for문 동안 값을 넣는다
                        }
                        //println(data_list)
                        data_list.add(total) // 과거 걸음에 오늘 걸음 수를 넣는다

                        val ID = intent.getStringExtra("ID")
                        graph(data_list, total)

                        //val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
                        //val startTime = endTime.minusWeeks(1)

                        /////////////////////////

                        connect(this, total,"$ID")
                        //////////////////////////

                    }
                    .addOnFailureListener { e -> Log.w(TAG,"There was an error reading data from Google Fit", e) }
            }
            .addOnFailureListener { e -> Log.w(TAG, "There was a problem getting the step count.", e) }
    }

    private fun graph(data_list : MutableList<Int>, total : Int) {

        // 7일간 걸음 수 데이터 화면에 출력
        var text6 : TextView = findViewById(R.id.textView6)
        var totala = data_list[0]+data_list[1]+data_list[2]+data_list[3]+data_list[4]+data_list[5]+data_list[6]
        text6.text = ("◼ 일주일간 총 걸음수 : "+"$totala"+"\n"+"\n" + "✔ 7일전 : "
                + data_list[0]+"  ✔ 6일전 : " + data_list[1]+"\n"+"✔ 5일전 : " + data_list[2]+"  ✔ 4일전 : "
                + data_list[3]+"\n"+"✔ 3일전 : " + data_list[4]+"  ✔ 2일전 : "
                + data_list[5]+"\n"+"✔ 1일전 : " + data_list[6])

        val lineChart : LineChart = findViewById(R.id.Chart)
        val visitors = ArrayList<Entry>() // 함수 List 인거 같아요

        //데이터 값들의 형식을 변형하여 리스트에 추가

        visitors.add(Entry(1.0f, data_list[0].toFloat()))
        visitors.add(Entry(2.0f, data_list[1].toFloat())) // (8, 400)
        visitors.add(Entry(3.0f, data_list[2].toFloat()))
        visitors.add(Entry(4.0f, data_list[3].toFloat()))
        visitors.add(Entry(5.0f, data_list[4].toFloat()))
        visitors.add(Entry(6.0f, data_list[5].toFloat()))
        visitors.add(Entry(7.0f, data_list[6].toFloat()))
        visitors.add(Entry(8.0f, total.toFloat()))


        // 걸음수 데이터 그래프에 표현

        val lineDataSet = LineDataSet(visitors, "7일 전부터 오늘의 걸음 수 그래프")

        lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.valueTextSize = 16f

        val lineData = LineData(lineDataSet)

        lineChart.data = lineData
        lineChart.invalidate()
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        // barChart.description.text = "Bar Chart Example"
        lineChart.animateY(2000)
    }

    //MQTT 연결
    private fun connect(context: Context, total: Int, ID :String ) {

        val topic = "topic 명"
        val mqttAndroidClient = MqttAndroidClient(context, "tcp://" + "ip 주소" + ":1883", MqttClient.generateClientId())

        try {
            val options = MqttConnectOptions()
            options.userName =  "token"
            mqttAndroidClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")

                    try {


                        val Timestamp = System.currentTimeMillis()

                        val msg = "{\"ts\":$Timestamp,\"values\":{\"steps_daily\":$total}}"

                        val message = MqttMessage()
                        message.payload = msg.toByteArray()

                        mqttAndroidClient.publish("$topic", message.payload, 0, false)
                        Log.d(TAG, "보낸 값 : $msg")

                    } catch (e: MqttException                                                                                                                                                                                                                                                                                                 ) {
                        e.printStackTrace()
                        Log.d(TAG, "Connection Fail : $e")
                    }
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

    }

    private val disconnectedBufferOptions: DisconnectedBufferOptions
        private get() {
            val disconnectedBufferOptions = DisconnectedBufferOptions()
            disconnectedBufferOptions.isBufferEnabled = true
            disconnectedBufferOptions.bufferSize = 100
            disconnectedBufferOptions.isPersistBuffer = true
            disconnectedBufferOptions.isDeleteOldestMessages = false
            return disconnectedBufferOptions
        }
    private val mqttConnectionOption: MqttConnectOptions
        private get() {
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isCleanSession = false
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.setWill("offline", "offline".toByteArray(), 1, true)
            return mqttConnectOptions
        }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the main; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_read_data) {
            fitSignIn(FitActionRequestCode.READ_DATA)
            return true
        }
        if (id == R.id.action_logout) {
            val logout_intent = Intent(this, Health_main::class.java)
            Toast.makeText(this, "로그아웃을 누르셨습니다.", Toast.LENGTH_SHORT)
            startActivity(logout_intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun permissionApproved(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            true
        }
    }

    /** 허용 권한 관련 함수
     * 이전에 요청을 거부했지만 "다시 묻지 않음" 확인란을 선택하지 않은 경우 이 문제가 발생합니다.
     */
    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            requestCode.ordinal
                        )
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode.ordinal
                )
            }
        }
    }

    /** 권한이 거부 되었을 시
     *이 메시지는 스낵바에서 전달
     * 단, 허용하지 않으면 앱이 응답하지 않는 것으로 나타날 수 있습니다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when {
            grantResults.isEmpty() -> {

                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                val fitActionRequestCode = FitActionRequestCode.values()[requestCode]
                fitActionRequestCode.let {
                    fitSignIn(fitActionRequestCode)
                }
            }
            else -> {
                Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }
}
