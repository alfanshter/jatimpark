package com.alfanshter.jatimpark

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.greenrobot.eventbus.EventBus

class ServiceLocation : Service() {

    lateinit var databaseReference: DatabaseReference
    private val REQUEST_CODE_PERMISSIONS = 101
    private var mAuth: FirebaseAuth? = null
    protected var stopReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(this)

            //Stop the Service//

            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mAuth = FirebaseAuth.getInstance()


        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {


        val request = LocationRequest()
        request.interval = 1000
        request.fastestInterval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client =
            LocationServices.getFusedLocationProviderClient(this@ServiceLocation)


        val permissionAccessCoarseLocationApproved = ActivityCompat
            .checkSelfPermission(
                this@ServiceLocation,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {

            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val location = locationResult?.lastLocation
                    if (location != null) {
                        databaseReference =
                            FirebaseDatabase.getInstance().reference.child("Selecta")
                                .child("latidude")
                        databaseReference.setValue(location.latitude.toString())
                    }
                }
            }, null)
        } else {
            // Make a request for foreground-only location access.
            ActivityCompat.requestPermissions(
                applicationContext as Activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }
}