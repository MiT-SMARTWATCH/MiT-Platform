package com.example.test_project

import android.graphics.Color
import android.graphics.Insets.add
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.support.wearable.activity.WearableActivity
import androidx.core.graphics.Insets.add
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.sql.DriverManager
import java.sql.SQLException

class view_graph : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_graph)

        val lineChart : LineChart = findViewById(R.id.Chart1)
        val values = ArrayList<Entry>()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try{
            var connection = DriverManager.getConnection("jdbc:postgresql://203.255.56.50:5432/postgres",
                "postgres","7452")
            println("Connected to PostgreSQL server")
            val sql = "select heartrate from heartrate order by datetime desc limit 10"
            val statement = connection.createStatement()
            val result = statement.executeQuery(sql)
            val hr = mutableListOf<Float>()

            while(result.next()){

                val rate = result.getFloat("heartrate")
                println("heart_rate :$rate")

                hr.add(rate)
                println(hr)
                connection.close()

            }
            val result_hr = hr.reversed()
            println(hr)
            for (i in 0..9) {
                values.add(Entry(i.toFloat(), result_hr[i]))
            }

            val lineDataSet = LineDataSet(values, "그래프")
            lineDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
            lineDataSet.valueTextColor = Color.WHITE
            lineDataSet.valueTextSize = 10f
            val lineData = LineData(lineDataSet)

            //print
            lineChart.data = lineData //dusruf
            lineChart.invalidate() //??
            lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER //Rnalrl
            // barChart.description.text = "Bar Chart Example"
            lineChart.animateY(2000) //speed




        }catch (e: SQLException){

            println(e)
            e.printStackTrace()
            println("@@@@@@@@@@@@@")
        }





    }


}


