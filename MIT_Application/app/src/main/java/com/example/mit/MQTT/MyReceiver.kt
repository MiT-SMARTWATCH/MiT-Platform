package com.example.mit.MQTT

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager


class MyReceiver : BroadcastReceiver() {

    private val TAG = "MyReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called")
        val workManager = WorkManager.getInstance(context)
        val startServiceRequest = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        workManager.enqueue(startServiceRequest)
    }
}