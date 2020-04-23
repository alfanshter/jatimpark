package com.alfanshter.jatimpark.ServiceLocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Utils.Utils
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyReceiver: BroadcastReceiver() {
    lateinit var sessionManager: SessionManager
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)

                if (result != null) {
                    sessionManager = SessionManager(context)
                    val user = FirebaseAuth.getInstance()
                    val UserID = user.currentUser!!.uid

                    val string = ""
                    val locations =
                        result.locations
                    val userid=""
                    val usermap : MutableMap<String,Any?> = HashMap()
                    usermap["image"] = sessionManager.getFoto().toString()
                    usermap["latidude"] = result.lastLocation.latitude
                    usermap["longitude"] = result.lastLocation.longitude
                    usermap["name"] = sessionManager.getprofil().toString()
                    Utils.setLocationUpdatesResult(context, locations,string,userid)

                    var reference = FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                        .child(sessionManager.getKunci().toString()).child(UserID)
                    reference.updateChildren(usermap)
                    Utils.sendNotification(
                        context,
                        Utils.getLocationResultTitle(context, locations)
                    )
                    Log.i(
                        TAG,
                        Utils.getLocationUpdatesResult(context)
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "LUBroadcastReceiver"
        const val ACTION_PROCESS_UPDATES =
            "com.alfanshter.jatimpark.action" +
                    ".PROCESS_UPDATES"
    }
}
