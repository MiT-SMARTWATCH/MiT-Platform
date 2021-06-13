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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mit.R
import com.example.mit.mainhealthcare.Health_main
import com.example.mit.mainhealthcare.Health_settings
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.material.snackbar.Snackbar
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


const val tag = "Heart Rate"

class HeartRate : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100 //권한 변수

    private val fitnessOptions = FitnessOptions.builder()
        .accessActivitySessions(FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
        .build()

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)

        val button : Button = findViewById(R.id.button3)


        //권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) { //권한없음
            //권한 요청 코드
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), PERMISSION_REQUEST_CODE)
        } else {

            button.setOnClickListener {
                val sleepSec = 63

                // 시간 출력 포맷
                val fmt = SimpleDateFormat("HH:mm:ss")
                // 주기적인 작업을 위한
                val exec = ScheduledThreadPoolExecutor(1)
                exec.scheduleAtFixedRate({
                    try {
                        val cal = Calendar.getInstance()
                        // 콘솔에 현재 시간 출력
                        println("현재시간 : " + fmt.format(cal.time))
                        fitSignIn(FitActionRequestCode.READ_DATA)
                        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 에러 발생시 Executor를 중지시킨다
                        exec.shutdown()
                    }
                }, 0, sleepSec.toLong(), TimeUnit.SECONDS)
            }

            val sleepSec = 65

            // 시간 출력 포맷
            val fmt = SimpleDateFormat("HH:mm:ss")
            // 주기적인 작업을 위한
            val exec = ScheduledThreadPoolExecutor(1)
            exec.scheduleAtFixedRate({
                try {
                    val cal = Calendar.getInstance()
                    // 콘솔에 현재 시간 출력
                    println(fmt.format(cal.time))
                    fitSignIn(FitActionRequestCode.READ_DATA)
                    checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // 에러 발생시 Executor를 중지시킨다
                    exec.shutdown()
                }
            }, 0, sleepSec.toLong(), TimeUnit.SECONDS)


        }
    }
    /** 권한 허용
     * if ~ : 허용 했을 시 실행되는 코드
     * else ~ - 거부 : alertDialog 클릭 시 기본설정 이동 -> 권한 허용 -> 어플 정상실행
     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    fitSignIn(FitActionRequestCode.READ_DATA)
                    checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)
                } else {
                    // 하나라도 거부한다면.
                    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                    alertDialog.setTitle("앱 권한")
                    alertDialog.setMessage("해당 앱의 원할한 기능을 이용하시려면 애플리케이션 정보>권한> 에서 모든 권한을 허용해 주십시오")
                    // 권한설정 클릭시 이벤트 발생
                    alertDialog.setPositiveButton("권한설정") { dialog, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + applicationContext.packageName))

                        fitSignIn(FitActionRequestCode.READ_DATA)
                        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

                        startActivity(intent)
                        dialog.cancel()
                    }
                    //취소
                    alertDialog.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
                    alertDialog.show()
                }
                return
            }
        }
    }

    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) { fitSignIn(fitActionRequestCode) }
        else { requestRuntimePermissions(fitActionRequestCode) }
    }

    /** 구글 피트니스 로그인 확인
     * 사용자가 로그인했는지 확인하고, 로그인된 경우 지정된 기능을 실행합니다.
     * 사용자가 로그인하지 않은 경우, 로그인 후 함수를 지정하여 로그인을 시작합니다.
     */

    private fun fitSignIn(requestCode: FitActionRequestCode) {
        if (oAuthPermissionsApproved()) {
            performActionForRequestCode(requestCode)
        } else { requestCode.let { GoogleSignIn.requestPermissions(this, requestCode.ordinal, getGoogleAccount(), fitnessOptions) }
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
            }else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    /** 구글피트니스 로그인 전달되고 성공 콜백과 함께 반환됩니다.
     * 이를 통해 호출자는 로그인 방법을 지정할 수 있습니다.
     */

    private fun performActionForRequestCode(requestCode: FitActionRequestCode) =
        when (requestCode) {
            FitActionRequestCode.READ_DATA -> readData()
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

    private fun oAuthPermissionsApproved() = GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    /** 데이터 등록 요청 및 심박수 데이터 기록*/
    private fun subscribe() {
        Fitness.getRecordingClient(this, getGoogleAccount())
            .subscribe(DataType.TYPE_HEART_RATE_BPM)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { Log.i(tag, "Successfully subscribed!") }
                else { Log.w(tag, "There was a problem subscribing.", task.exception) }
            }
    }

    /** 심박 수  데이터 값 불러오는 함수
     * 현재 시간의 6시간 전부터 현재까지의 심박 수를 읽어옵니다
     * (시간은 설정에 따라 상이될 수 있음)
     */

    private fun readData() {

        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.time = now
//        cal.add(Calendar.DATE, -32)
//        cal.add(Calendar.HOUR, -5)
        val endTime: Long = cal.timeInMillis
        cal.add(Calendar.HOUR, -6)
        val startTime: Long = cal.timeInMillis

        val data_list = mutableListOf<String>()
        val heart_list =  mutableListOf<Int>()

        val dateFormat: DateFormat = getDateInstance()
        Log.d(tag, "---------------------------------")
        Log.d(tag, "Range Start: " + dateFormat.format(startTime))
        Log.d(tag, "Range End: " + dateFormat.format(endTime))
        Log.d(tag, "---------------------------------")

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { dataReadResult ->
                if (dataReadResult.dataSets.size > 0) {

                    for (dataSet in dataReadResult.dataSets) {
                        //Log.d(tag, "Data returned for Data type: " + dataSet.dataType.name)
                        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                        for (dataPoint in dataSet.dataPoints) {

                            for (field in dataPoint.dataType.fields) {
                                val mLastHeartBPM = dataPoint.getValue(field).asFloat().toInt()

                                data_list.add(dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)).toString())
                                heart_list.add(mLastHeartBPM)
                            }
                        }
                    }
                }
//
//                    if (dataReadResult.buckets.size > 0) {
//
//                        for (bucket in dataReadResult.buckets) {
//                            val dataSets: List<DataSet> = bucket.dataSets
//                            for (dataSet in dataSets) {
//                                val dateFormat: DateFormat = DateFormat.getTimeInstance()
//                                for (dataPoint in dataSet.dataPoints) {
//                                    for (field in dataPoint.dataType.fields) {
//                                        val mLastHeartBPM = dataPoint.getValue(field).asFloat().toInt()
//                                        data_list.add("시간 : " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)).toString())
//                                        data_list.add("심박수 : $mLastHeartBPM BPM")
//                                        heart_list.add(mLastHeartBPM)
//                                        data_list.add("\n")
//                                        println("+++++++++++++++buckets")
//                                    }
//                                }
//                            }
//                        }
//                    } else if (dataReadResult.dataSets.size > 0) {
//
//                        for (dataSet in dataReadResult.dataSets) {
//                            //Log.d(tag, "Data returned for Data type: " + dataSet.dataType.name)
//                            val dateFormat: DateFormat = SimpleDateFormat("YYYY.MM.dd - HH:mm:ss")
//                            for (dataPoint in dataSet.dataPoints) {
//
//                                for (field in dataPoint.dataType.fields) {
//                                    val mLastHeartBPM = dataPoint.getValue(field).asFloat().toInt()
//
//                                    data_list.add("시간 : " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)).toString())
//                                    data_list.add("심박수 : $mLastHeartBPM BPM")
//                                    heart_list.add(mLastHeartBPM)
//                                    data_list.add("\n")
//                                    println("+++++++++++++++dataSets")
//                                }
//                            }
//                        }
//                    }

                val avg : TextView = this.findViewById(R.id.textView16)
                val min_max : TextView = this.findViewById(R.id.textView20)
                println(heart_list)
                println(data_list)

                //MQTT에 심박수의 가장 최근 값을 시간과 심박 수 값을 전송합니다.

                connect(this, heart_list[heart_list.size - 1], data_list[heart_list.size - 1],"ID")

                val heartdata = mutableListOf<Int>()


                /** 심박 수 화면 출력 방식
                 * 1. heart_list 7개 이하 시 오류가 생겨 나눠주었습니다.
                 * 2. list값을 평균, 최대, 최소 를 만들어 출력시켜줍니다.
                 */

                if (heart_list.size > 7) {
                    val reverse = heart_list.reversed()
                    for (i in 0..6 ) { heartdata.add(reverse[i]) }
                    val list_sorted = heartdata.sorted()
                    val list_size = heartdata.sorted().size
                    val min = list_sorted[0]
                    val max = list_sorted[list_size - 1]
                    var sum = 0
                    for(i in heartdata) sum += i
                    val list_avg = sum / list_size
                    avg.text = "평균 심박수 : $list_avg"
                    min_max.text = "최소 심박수 : $min   |   최대 심박수 : $max"
                } else if (heart_list.size > 0){
                    val reverse = heart_list.reversed()
                    for (i in heart_list.indices ) { heartdata.add(reverse[i]) }
                    val list_sorted = heartdata.sorted()
                    val list_size = heartdata.sorted().size
                    val min = list_sorted[0]
                    val max = list_sorted[list_size - 1]
                    var sum = 0
                    for(i in heartdata) sum += i
                    val list_avg = sum / list_size
                    avg.text = "평균 심박수 : $list_avg"
                    min_max.text = "최소 심박수 : $min   |   최대 심박수 : $max"
                }

                val lineChart : LineChart = findViewById(R.id.Chart1)
                val heart = ArrayList<Entry>()

                /** 그래프
                 * heartdata에는 7개의 값이 들어있습니다.
                 * 역순으로 리스트를 바꿔 준 후 그래프로 출력 시켜줍니다.
                 * (뒤에서부터 값을 reversed해주었기에 그래프를 그리기 위해선 다시 reversed 해주었니다.
                 */

                for (i in heartdata.indices) {
                    val reverse_heartdata = heartdata.reversed()
                    heart.add(Entry(i.toFloat(), reverse_heartdata[i].toFloat()))
                }

                val lineDataSet = LineDataSet(heart, "오늘의 심박 수 그래프")

                lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                lineDataSet.valueTextColor = Color.BLACK
                lineDataSet.valueTextSize = 10f

                val lineData = LineData(lineDataSet)

                lineChart.data = lineData
                lineChart.invalidate()
                lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                //barChart.description.text = "Bar Chart Example"
                lineChart.animateY(200)


            }
    }

    private fun connect(context: Context, total: Int, times : String, ID :String) {

        val topic = "v1/devices/me/telemetry"
        val mqttAndroidClient = MqttAndroidClient(context, "tcp://" + "203.255.56.50" + ":1883", MqttClient.generateClientId())

        try {
            val options = MqttConnectOptions()
//            options.userName = "G7Y9k68xJUzG3OeDo8vO"
            options.userName = "FLlvBxWbtZVun7XklaTG"
            mqttAndroidClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd-kk-mm")
                        val Timestamp = sdf.parse(times).time
                        val msg = "{\"ts\":$Timestamp,\"values\":{\"heart_rate\":$total}}"
//                        val msg = "{\"heart_rate\":$total}"
//                        val msg = "{\"ts\":$Timestamp,\"heart_rate\":$total}"

                        val message = MqttMessage()
                        message.payload = msg.toByteArray()
                        mqttAndroidClient.publish("$topic", message.payload, 0, false)
                        Log.d(TAG, "보낸 값 : $msg")
                        //mqttAndroidClient.subscribe("$topic", 0) //연결에 성공하면 jmlee 라는 토픽으로 subscribe함
                    } catch (e: MqttException) {
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
//        try {
//            mqttAndroidClient.publish("$topic", "$total".toByteArray(), 0, false)
//
//        } catch (e: MqttException) {
//            e.printStackTrace()
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_logout) {
            val logout_intent = Intent(this, Health_main::class.java)
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT)
            startActivity(logout_intent)
        }
        if (id == R.id.action_settings){
            val settings = Intent(this, Health_settings::class.java)
            startActivity(settings)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun permissionApproved(): Boolean {
        return if (runningQOrLater) { PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) }
        else { true }
    }

    /** 허용 권한 관련 함수
     * 이전에 요청을 거부했지만 "다시 묻지 않음" 확인란을 선택하지 않은 경우 이 문제가 발생합니다.
     */
    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BODY_SENSORS)
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(tag, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), requestCode.ordinal) }
                    .show()
            } else {
                Log.i(tag, "Requesting permission")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), requestCode.ordinal)
            }
        }
    }
}