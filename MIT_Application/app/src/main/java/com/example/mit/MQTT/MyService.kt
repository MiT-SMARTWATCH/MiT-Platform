package com.example.mit.MQTT

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mit.MainActivity
import com.example.mit.R

class MyService : Service() {
    private val TAG = "MyService"
    private val CHANNEL_ID = "NOTIFICATION_CHANNEL"

    override fun onCreate() {  //----2번
        super.onCreate()
        Log.d(TAG, "onCreate called")
        createNotificationChannel()
        isServiceRunning = true
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int { // ---3번
        Log.d(TAG, "onStartCommand called")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service is Running")
            .setContentText("Listening for Screen Off/On events")
            .setSmallIcon(R.drawable.ic_wallpaper_black_24dp)
            .setContentIntent(pendingIntent)
            .setColor(resources.getColor(R.color.design_default_color_primary))
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = getString(R.string.app_name)
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                appName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        isServiceRunning = false
        stopForeground(true)

        // call MyReceiver which will restart this service via a worker
        val broadcastIntent = Intent(this, MyReceiver::class.java)
        sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

    companion object {
        var isServiceRunning: Boolean = false
    }

    init {
        Log.d(TAG, "constructor called")   // ----1번
        isServiceRunning = false
    }
}
