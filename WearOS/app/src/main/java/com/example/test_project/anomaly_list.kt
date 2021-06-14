package com.example.test_project

import android.content.Context
import android.graphics.Color
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.sql.DriverManager
import java.sql.SQLException

class anomaly_list : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anomaly_list)
//
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try{
            var connection = DriverManager.getConnection("jdbc:postgresql://203.255.56.50:5432/postgres",
                "postgres","7452")
            println("Connected to PostgreSQL server")
            val sql = "select time,rate from anomaly_list order by index desc limit 24"
            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)
            val hr = mutableListOf<String>()
            val tm = mutableListOf<String>()
            val anomaly_list = mutableListOf<String>()

            while(result.next()){

                val rate = result.getInt("rate")
                println("heart_rate :$rate")
                hr.add(rate.toString())

                val time = result.getString("time")
                println("time :$time")
                tm.add(time.toString())

                connection.close()

            }
            val result_hr = hr.reversed()
            println(result_hr)

            val result_tm = tm.reversed()
            println(result_tm)

            val arrayAdapter: ArrayAdapter<*>

            var mListView = findViewById<ListView>(R.id.listview)

            for(i in 0..result_tm.size-1){
                var cnt = result_tm[i] + "       " + result_hr[i]
                anomaly_list.add(cnt)
            }

            println(anomaly_list)



            arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, anomaly_list)
            mListView.adapter = arrayAdapter


        }catch (e: SQLException){

            println(e)
            e.printStackTrace()
            println("@@@@@@@@@@@@@")
        }


    }
}



