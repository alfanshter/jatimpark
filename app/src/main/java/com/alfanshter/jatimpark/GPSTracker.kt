package com.alfanshter.jatimpark

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import java.lang.Exception
import java.util.jar.Manifest

class GPSTracker (c:Context) : Service(),LocationListener {
    private var context : Context? = null
    internal  var isGPSEnabled = false
    internal var isNetworkEnabled = false
    internal var canGetLocation = false
    internal var location:Location? = null
    internal var latitude:Double = 0.toDouble()
    internal var longitude:Double = 0.toDouble()
    protected var locationManager:LocationManager? = null

    val locations: Location?
    get() = if ( location!=null){
        location
    } else null

    init {
        this.context = c
        getlocation()
    }

    private fun getlocation():Location?{
        try {
            locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && isNetworkEnabled){
                showSettingGps()
            }
            else{
                canGetLocation = true
                    //get lat/lng by network
                if (isNetworkEnabled){
                    if (checkPermission(context)){
                        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DISTANCE.toFloat(),this)
                    }
                    Log.d("network", "network enabled")
                    if (locationManager !=null){
                        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location!=null){
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }else{

                }
            }
            if (isGPSEnabled){
                if (location ==null){
                    if (checkPermission(context)){
                        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DISTANCE.toFloat(),this)
                        Log.d("GPS","GPS Enabled")
                        if (locationManager!=null){
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location!=null){
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                    else{

                    }
                }
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
        return location
    }

    fun getLatitude():Double{
        if (location!=null){
            latitude = location!!.latitude
        }
        return latitude
    }

    fun getLongitude():Double{
        if (location!=null){
            longitude = location!!.longitude
        }
        return longitude
    }

    fun canGetLocation():Boolean{
        return canGetLocation
    }

    fun showSettingGps(){
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle("GPS Setting")
        alertBuilder.setMessage("GPS is not enabled. do you want to go setting menu?")
        alertBuilder.setPositiveButton("Setting"){
            dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        alertBuilder.setNegativeButton("cancel"){
            dialog, which ->
            dialog.cancel()
        }
        alertBuilder.show()
    }

    override fun onLocationChanged(location: Location?) {
        if (location!=null){
            if (this.location !== location){
                this.location = location
            }
        }
    }

    override fun onProviderDisabled(provider: String?) {
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

companion object{
    private val MIN_DISTANCE = 1.toLong() // 10 meter
    private val MIN_TIME = (1000 * 1 *1).toLong()
    val NEW_POSITION = "newPosition"

    fun checkPermission(context: Context?):Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
    }
}
}