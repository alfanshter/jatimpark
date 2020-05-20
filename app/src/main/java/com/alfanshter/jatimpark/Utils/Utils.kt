@file:Suppress("DEPRECATION")

package com.alfanshter.jatimpark.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.ServiceLocation.MyIntentService
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.alfanshter.jatimpark.ui.shareRombongan.ShareRombongan
import com.alfanshter.jatimpark.ui.shareRombongan.ShareRombonganduaactivity
import com.alfanshter.jatimpark.ui.shareRombongan.Sharerombongandua
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.*
internal object Utils : AppCompatActivity() {
    lateinit var sessionManager: SessionManager


    fun sessionmaanager(string: String){
        var alfan = string
    }

    const val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"
    const val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
    const val CHANNEL_ID = "channel_01"
    var alfan = "20"
    var useridbaru = ""
    fun setRequestingLocationUpdates(
        context: Context?,
        value: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
            .apply()
    }

    fun getRequestingLocationUpdates(context: Context?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false)
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    fun sendNotification(
        context: Context,
        notificationDetails: String?
    ) {
        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(context, Sharerombongandua::class.java)
        notificationIntent.putExtra("from_notification", true)

        // Construct a task stack.
        val stackBuilder =
            TaskStackBuilder.create(context)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(Tracking_Rombongan::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(context)

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher) // In a real app, you may want to use a library like Volley
            // to decode the Bitmap.
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setColor(Color.RED)
            .setContentTitle("Location update")
            .setContentText(notificationDetails)
            .setContentIntent(notificationPendingIntent)

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)

            // Channel ID
            builder.setChannelId(CHANNEL_ID)
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    /**
     * Returns the title for reporting about a list of [Location] objects.
     *
     * @param context The [Context].
     */
    fun getLocationResultTitle(
        context: Context,
        locations: List<Location>
    ): String {
        val numLocationsReported = context.resources.getQuantityString(
            R.plurals.num_locations_reported, locations.size, locations.size
        )
        return "$numLocationsReported: " + DateFormat.getDateTimeInstance()
            .format(Date())
    }

    /**
     * Returns te text for reporting about a list of  [Location] objects.
     *
     * @param locations List of [Location]s.
     */
    private fun getLocationResultText(
        context: Context,
        locations: List<Location>, string: String, userid:String
    ): String {

        if (locations.isEmpty()) {
            return context.getString(R.string.unknown_location)
        }


        val sb = StringBuilder()


        for (location in locations) {
            sb.append("(")
            sb.append(location.latitude)
            sb.append(", ")
            sb.append(location.longitude)
            sb.append(")")
            sb.append("\n")
            val auth  = FirebaseAuth.getInstance()

/*
                val myRef = database.getReference("Selecta").child("sharing").child(kode).child(userida)
                myRef.child("longitude/").setValue(location.longitude)
                myRef.child("latidude/").setValue(location.latitude)
            myRef.child("image/").setValue(gambar)
            myRef.child("name/").setValue(nama)
*/


        }
        return sb.toString()
    }


    fun setLocationUpdatesResult(
        context: Context,
        locations: List<Location>, string: String,userid: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(
                KEY_LOCATION_UPDATES_RESULT, """
     ${getLocationResultTitle(context, locations)}
     ${getLocationResultText(context, locations,string,userid)}
     """.trimIndent()
            )
            .apply()
    }

    fun getLocationUpdatesResult(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_LOCATION_UPDATES_RESULT, "")
    }
}


