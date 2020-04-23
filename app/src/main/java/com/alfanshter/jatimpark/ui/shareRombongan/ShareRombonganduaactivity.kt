package com.alfanshter.jatimpark.ui.shareRombongan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import com.alfanshter.jatimpark.Model.ModelSharing
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.ServiceLocation.MyReceiver
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.alfanshter.jatimpark.Utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_share_rombonganduaactivity.*
import kotlinx.android.synthetic.main.fragment_sharerombongandua.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast
import java.lang.ref.WeakReference
import java.util.*

class ShareRombonganduaactivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback,
    PermissionsListener, SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var databaseReference: DatabaseReference
    lateinit var mapboxMap: MapboxMap
    private var callback = MainActivityLocationCallbackBaru(this)
    private lateinit var customview: View
    private var markerViewManager: MarkerViewManager? = null
    var lokasi = LatLng(00.00, 00.00)
    private lateinit var marker: MarkerView
    private var setMarker: Boolean = false
    private lateinit var locationComponent: LocationComponent
    private var locationEngine: LocationEngine? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private var statusjoin: Boolean = false
    lateinit var sessionManager: SessionManager
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val TAG: String? = ShareRombonganduaactivity::class.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    @SuppressLint("UseSparseArrays")
    var markerMap: HashMap<Int, MarkerView> = HashMap<Int, MarkerView>()
    private val UPDATE_INTERVAL: Long = 15000 // Every 60 seconds.
    private val FASTEST_UPDATE_INTERVAL: Long = 10000
    private val MAX_WAIT_TIME: Long = UPDATE_INTERVAL * 5 // Every 5 minutes.
    private lateinit var auth: FirebaseAuth
    lateinit var userID: String
    lateinit var user: FirebaseUser
    lateinit var referensehapus: DatabaseReference
    var statusupdate = 0
    companion object {
        lateinit var nilaikode: String
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_share_rombonganduaactivity)
        if (!checkPermissions()) {
//            requestPermissions()
        }
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        Utils.setRequestingLocationUpdates(this, true)
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())

        mapviewshare.onCreate(savedInstanceState)
        mapviewshare.getMapAsync(this)
        sessionManager = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userID = user.uid
        val db = FirebaseFirestore.getInstance()

        home.setOnClickListener {
            startActivity<Tracking_Rombongan>()
        }

        keluar.setOnClickListener {
            statusupdate = 1
            Utils.setRequestingLocationUpdates(this, false)
            mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())

            sessionManager.setIDStatusUser("")
            statusjoin = false
            referensehapus =
                FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
            referensehapus.child(sessionManager.getKunci().toString()).child(userID).removeValue()
            db.collection("Sharing").document(sessionManager.getKunci().toString())
                .collection("share").document(userID)
                .delete()
            startActivity<Tracking_Rombongan>()

        }



    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.LIGHT) {

            enableLocationComponent(it)
            markerViewManager = MarkerViewManager(mapviewshare, mapboxMap)
            setMarker = true
            if (setMarker == true) {
                if (statusupdate==0)
                {
                    ambildata()

                }

            }
        }


    }

    fun ambildata() {


        databaseReference =
            FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                .child(sessionManager.getKunci().toString())
        info { "hasil ${sessionManager.getKunci().toString()}"  }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("InflateParams")
            override fun onDataChange(p0: DataSnapshot) {
                for (values in markerMap.values) {
                    markerViewManager?.removeMarker(values)
                }
                //   markerViewManager?.removeMarker(marker)
                var counter: Int = 0
                for (i in p0.children) {
                    var user: ModelSharing? =
                        i.getValue(ModelSharing::class.java)
                    var datalongitude = user!!.longitude
                    var datalatitude = user.latidude
                    var nama = user.name
                    var foto = user.image

                    customview = LayoutInflater.from(this@ShareRombonganduaactivity)
                        .inflate(R.layout.marker, null)
                    customview.layoutParams =
                        FrameLayout.LayoutParams(
                            ConstraintSet.WRAP_CONTENT, ConstraintSet.WRAP_CONTENT
                        )

                    val titleTextView: TextView = customview.findViewById(R.id.marker_window_title)
                    val gambarView: ImageView = customview.findViewById(R.id.gambarview)

                    lokasi = LatLng(datalatitude, datalongitude)
                    Picasso.get().load(foto).resize(50, 50).into(gambarView)

                    marker = MarkerView(lokasi, customview)
                    titleTextView.text = nama
                    markerViewManager?.addMarker(marker)
                    markerMap.put(counter, marker)
                    counter++
                    //break
                }


            }
        })

    }


    override fun onStart() {
        super.onStart()

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        mapviewshare.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapviewshare.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapviewshare.onPause()
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
        mapviewshare.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapviewshare.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapviewshare.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (markerViewManager != null) {
            markerViewManager!!.onDestroy()
        }
        mapviewshare.onDestroy()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        toast(R.string.user_location_permission_explanation)

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (mapboxMap.style != null) {
                enableLocationComponent(mapboxMap.style!!)
            }
        } else {
            toast(R.string.user_location_permission_not_granted)
            finish()
        }


    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                this!!,
                loadedMapStyle
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            initLocationEngine()


        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this as Activity?)
        }
    }

    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine!!.requestLocationUpdates(request, callback, this.mainLooper)
        locationEngine!!.getLastLocation(callback)
    }




    internal class MainActivityLocationCallbackBaru internal constructor(activity: ShareRombonganduaactivity) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<ShareRombonganduaactivity> =
            WeakReference(activity)
        lateinit var databaseReference: DatabaseReference

        override fun onSuccess(result: LocationEngineResult?) {
            val activity: ShareRombonganduaactivity? = activityWeakReference.get()


            if (activity != null) {
                val location: Location? = result?.lastLocation
                if (location == null) {
                    return

                }

                    databaseReference =
                        FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                    /* databaseReference.child(activity.sessionManager.getKunci().toString())
                         .child("${activity.userID}/latidude")
                         .setValue(result.lastLocation!!.latitude)
                     databaseReference.child(activity.sessionManager.getKunci().toString())
                         .child("${activity.userID}/longitude")
                         .setValue(result.lastLocation!!.longitude)*/
              /*      databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/name")
                        .setValue(activity.sessionManager.getprofil())
                    databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/image")
                        .setValue(activity.sessionManager.getFoto())
*/
            }

        }


        @SuppressLint("LogNotTimber")
        override fun onFailure(exception: Exception) {
            Log.d("LocationChange", exception.localizedMessage)
            val activity: ShareRombonganduaactivity? = activityWeakReference.get()
            if (activity != null) {
                activity.toast(exception.localizedMessage)

            }

        }

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest!!.interval = UPDATE_INTERVAL

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest!!.maxWaitTime = MAX_WAIT_TIME
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission was granted.
                requestLocationUpdates()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                    find(R.id.sharerombonganduaactivity),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) { // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    fun requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(this, true)
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            e.printStackTrace()
        }
    }

    private fun getPendingIntent(): PendingIntent {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val intent = Intent(this, MyReceiver::class.java)
        intent.action = MyReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

}
