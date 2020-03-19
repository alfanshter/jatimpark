package com.alfanshter.jatimpark.ui.Calling.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class FirebaseNotifikasi : FirebaseMessagingService(),AnkoLogger {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        info { "token : $token" }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        info { "From : ${remoteMessage.from}" }
        if (remoteMessage.notification !=null){
            info { "sms : ${remoteMessage.notification?.body}" }
            tampilkannotif(remoteMessage)
        }
    }
    val channelId = "Default"

    private fun tampilkannotif(remoteMessage: RemoteMessage) {
        val intent = Intent(this,Tracking_Rombongan::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(this,channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,"Default channel",NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }


        //trigger notifikasi
        manager.notify(0,builder.build())
    }
}
