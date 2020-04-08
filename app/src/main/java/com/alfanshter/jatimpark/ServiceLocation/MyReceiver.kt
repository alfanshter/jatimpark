package com.alfanshter.jatimpark.ServiceLocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Utils.Utils
import com.google.android.gms.location.LocationResult

class MyReceiver: BroadcastReceiver() {
    lateinit var sessionManager: SessionManager
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)

                if (result != null) {

                    val string = ""
                    val locations =
                        result.locations
                    val userid =""

                    Utils.setLocationUpdatesResult(context, locations,string,userid)

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
