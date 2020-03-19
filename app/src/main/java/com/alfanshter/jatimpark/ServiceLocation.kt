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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus

class ServiceLocation : Service() {
    companion object{
        private val CHANNEL_ID = "channel_01"
        private val PACKAGE_NAME ="com.alfanshter.jatimpark"
        private val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"
        private val UPDATE_INTERVAL_IN_MILL : Long = 10000
        private val FASTED_UPDATE_INTERVAL_IN_MIL : Long = UPDATE_INTERVAL_IN_MILL/2
        private val NOTIFICATION_ID = 1234
    }

    private val mBinder = LocalBinder()
    inner class LocalBinder():Binder(){
        internal val service:ServiceLocation
        get() = this@ServiceLocation
    }

    private var mChangingConfiguration = false
    private var mNotificationManager : NotificationManager? = null
    private var locationRequest:LocationRequest? = null
    private var fusedLocationProviderClient:FusedLocationProviderClient? = null
    private var locationCallback : LocationCallback? = null
    private var mServiceHandler:Handler? = null
    private var mLocation : Location?=null

    private val notification:Notification
    get() {
        val intent = Intent(this,ServiceLocation::class.java)
        val text = Commond.getLocationText(mLocation)
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true)
        val servicePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this)
            .addAction(R.drawable.ic_arrow_up,"Lauch",activityPendingIntent)
            .addAction(R.drawable.ic_arrow_head,"Cancel",servicePendingIntent)
            .setContentText(text)
            .setContentTitle(Commond.getLocationTitle(this))
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            builder.setChannelId(CHANNEL_ID)
        return builder.build()
    }


    private var mAuth: FirebaseAuth? = null
    protected var stopReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(this)

            //Stop the Service//

            stopSelf()
        }
    }



    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                onNewLocation(p0!!.lastLocation)
            }
        }
        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread("com")
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = packageName
            val mChannel = NotificationChannel(CHANNEL_ID,name,NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
//
//        mAuth = FirebaseAuth.getInstance()
//        requestLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startedFromNotification = intent!!.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false)
        if (startedFromNotification)
        {
            removeLocationUpdates()
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

     fun removeLocationUpdates() {
        try {
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
            Commond.setRequestingLocationUpdates(this,false)
            stopSelf()
        }catch (ex:SecurityException){
            Commond.setRequestingLocationUpdates(this,true)
            Log.e("com","lost location. $ex")
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationProviderClient!!.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result !=null)
                        mLocation = task.result
                    else
                        Log.e("com","Failde get location")
                }
        }catch (ex:SecurityException)
        {
            Log.e("com","" + ex.message)

        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILL
        locationRequest!!.fastestInterval = FASTED_UPDATE_INTERVAL_IN_MIL
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun onNewLocation(lastLocation: Location?) {
        mLocation = lastLocation!!
        EventBus.getDefault().postSticky(BackgroundLocation(mLocation!!))
        if (serviceIsRunningInForeground(this)){
            mNotificationManager!!.notify(NOTIFICATION_ID,notification)
        }

    }

    private fun serviceIsRunningInForeground(context:Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)){
            if (javaClass.name.equals(service.service.className))
                if (service.foreground)
                    return true
        }
        return false
    }

    override fun onBind(intent: Intent): IBinder {
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!mChangingConfiguration && Commond.requestingLocationUpdates(this))
            startForeground(NOTIFICATION_ID,notification)
            return true
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun requestLocationUpdates(){
        Commond.setRequestingLocationUpdates(this,true)
        startService(Intent(applicationContext,ServiceLocation::class.java))
        try {
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest!!,locationCallback!!,
                Looper.myLooper())
        }catch (ex:SecurityException){
            Commond.setRequestingLocationUpdates(this,false)
            Log.e("com","Lost Location permisssion. $ex")
        }
    }
    private fun requestLocationUpdatesa() {
        val ref = FirebaseDatabase.getInstance().reference.child("Selecta")

        val query = ref.orderByChild("uid").equalTo(mAuth?.currentUser?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (issue in p0.children) {
                    val key = issue.key

                    val request = LocationRequest()
                    request.interval = 1000
                    request.fastestInterval = 1000
                    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    val client =
                        LocationServices.getFusedLocationProviderClient(this@ServiceLocation)
                    val permission = ContextCompat.checkSelfPermission(
                        this@ServiceLocation,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permission == PackageManager.PERMISSION_GRANTED) {

                        client.requestLocationUpdates(request, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult?) {
                                val location = locationResult?.lastLocation
                                if (location != null) {
                                    key?.let {
                                        ref.child(it).child("latitude")
                                            .setValue(location.latitude.toString())
                                    }
                                    key?.let {
                                        ref.child(it).child("longitude")
                                            .setValue(location.longitude.toString())
                                    }


                                }
                            }
                        }, null)
                    }

                }

            }
        })

    }
}
