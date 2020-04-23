package com.alfanshter.jatimpark.ServiceLocation

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.alfanshter.jatimpark.Model.ModelBaru
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Utils.Utils
import com.alfanshter.jatimpark.Utils.Utils.getLocationUpdatesResult
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MyIntentService :
    IntentService(TAG),AnkoLogger {
    lateinit var sessionManager: SessionManager
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations =
                        result.locations
                    sessionManager = SessionManager(this)
                    val session = sessionManager.getKunci()
                    val user = FirebaseAuth.getInstance()
                    val userid = user.currentUser!!.uid
                    Utils.setLocationUpdatesResult(this, locations, session.toString(), userid)
                    Utils.useridbaru = "20"
                    Utils.sendNotification(this, Utils.getLocationResultTitle(this, locations))
                    Log.i(
                        TAG,
                        getLocationUpdatesResult(this)
                    )
                }
            }
        }
    }

    companion object {
        private const val ACTION_PROCESS_UPDATES =
            "com.alfanshter.jatimpark.action" +
                    ".PROCESS_UPDATES"
        private val TAG = MyIntentService::class.java.simpleName
    }
}
