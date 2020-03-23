package com.alfanshter.jatimpark.ui.Tracking

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alfanshter.jatimpark.Session.SessionManager
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import org.jetbrains.anko.toast
import java.lang.ref.WeakReference


class lokasi : AppCompatActivity() {
    lateinit var mapboks: MapboxMap
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private var callback = MainActivityLocationCallback(this)
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }



    private fun aktiflokasi(enablelokasi:Style){
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            lokasi = mapboks.locationComponent
            lokasi.activateLocationComponent(
                this,
                enablelokasi
            )

            lokasi.isLocationComponentEnabled = true
            lokasi.cameraMode = CameraMode.TRACKING
            lokasi.renderMode = RenderMode.COMPASS
            initLocationEngine()
        }


    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        lokasiEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        lokasiEngine!!.requestLocationUpdates(request, callback, this.mainLooper)
        lokasiEngine!!.getLastLocation(callback)
/*
        locationEngine.removeLocationUpdates(request,callback,mainLooper)
*/
/*
        locationEngine.getLastLocation(callback)
*/
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    internal class MainActivityLocationCallback internal constructor(activity: lokasi) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<lokasi> = WeakReference(activity)
        lateinit var sessionManager: SessionManager

        @SuppressLint("SetTextI18n", "InflateParams")
        override fun onSuccess(result: LocationEngineResult?) {
            val activity: lokasi? = activityWeakReference.get()


            if (activity != null) {
                val location: Location? = result?.lastLocation
                if (location == null) {
                    return

                }

                sessionManager.setlongitude(result.lastLocation!!.longitude.toString())
                sessionManager.setLatidude(result.lastLocation!!.latitude.toString())

                if (result.lastLocation != null) {
                    activity.mapboks.locationComponent.forceLocationUpdate(result.lastLocation)
                }

            }
        }

        @SuppressLint("LogNotTimber")
        override fun onFailure(exception: java.lang.Exception) {
            Log.d("LocationChange", exception.localizedMessage)
            val activity: lokasi? = activityWeakReference.get()
            if (activity != null) {
                activity.toast(exception.localizedMessage)

            }

        }





    }

    companion object {
        lateinit var lokasi: LocationComponent

        lateinit var lokasiEngine: LocationEngine
    }
}