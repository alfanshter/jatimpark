@file:Suppress("DEPRECATION")

package com.alfanshter.jatimpark.ui.shareRombongan


import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.alfanshter.jatimpark.Model.ModelSharing
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.ServiceLocation.MyReceiver
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Utils.Utils
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.UsersFragment
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
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_sharerombongandua.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast
import java.lang.ref.WeakReference
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class Sharerombongandua : Fragment(), OnMapReadyCallback, MapboxMap.OnMapClickListener,
    PermissionsListener, AnkoLogger, SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var sessionManager: SessionManager
    private lateinit var butonkode: Button
    private lateinit var mapboxMap: MapboxMap
    private var markerViewManager: MarkerViewManager? = null
    private lateinit var locationComponent: LocationComponent
    private var callback = MainActivityLocationCallbackBaru(this)
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private var locationEngine: LocationEngine? = null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    private lateinit var referencesharing: DatabaseReference
    private lateinit var referencejoin: DatabaseReference
    lateinit var referensehapus: DatabaseReference
    lateinit var userID: String
    private lateinit var marker: MarkerView
    private var setMarker: Boolean = false
    private var hasDraw: Boolean = false
    private var statusjoin: Boolean = false
    private lateinit var animator: ValueAnimator
    private lateinit var geoJsonSource: GeoJsonSource
    var lokasi = LatLng(-7.81958192107, 112.52652012)
    private var currentPosition = LatLng(-7.81958192107, 112.52652012)
    var latAwal: Double? = null
    var lonAwal: Double? = null
    var latAkhir: Double? = null
    var lonAkhir: Double? = null


    val PERMISSIONS_REQUEST = 1


    companion object {
        private val TAG: String? = Sharerombongandua::class.simpleName
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private var root: View? = null
        private val UPDATE_INTERVAL: Long = 15000 // Every 60 seconds.
        private val FASTEST_UPDATE_INTERVAL: Long = 10000
        private val MAX_WAIT_TIME: Long = UPDATE_INTERVAL * 5 // Every 5 minutes.

        var setmarker = false
        lateinit var customview: View

    }

    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null


    @SuppressLint("UseSparseArrays")
    var markerMap: HashMap<Int, MarkerView> = HashMap<Int, MarkerView>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Check whether GPS tracking is enabled//
        statusjoin = true
        Mapbox.getInstance(context!!.applicationContext, getString(R.string.access_token))
         root = inflater.inflate(R.layout.fragment_sharerombongandua, container, false)
        if (!checkPermissions()) {
//            requestPermissions()
            }

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context!!.applicationContext)
        createLocationRequest()

        Utils.setRequestingLocationUpdates(context!!.applicationContext, true)
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())


        val mapView = root!!.findViewById(R.id.mapboxfamily) as MapView
        val butonview: Button = root!!.find(R.id.viewuser)
        val refresh: Button = root!!.find(R.id.refresh)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        setMarker = true
        sessionManager = SessionManager(context)
        Utils.alfan = sessionManager.getKunci().toString()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userID = user.uid
        val db = FirebaseFirestore.getInstance()

        butonkode = root!!.find(R.id.keluarshare)

        butonview.setOnClickListener {
            val fr = fragmentManager?.beginTransaction()
            fr?.replace(R.id.nav_host_fragment, UsersFragment())
            fr?.commit()

        }

        refresh.setOnClickListener {
            val ft = fragmentManager!!.beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }
            ft.detach(this).attach(this).commit()
        }
        // Inflate the layout for this fragment
        butonkode.setOnClickListener {
            Utils.setRequestingLocationUpdates(context!!.applicationContext, false)
            mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())

            sessionManager.setIDStatusUser("")
            statusjoin = false
            referensehapus =
                FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
            referensehapus.child(sessionManager.getKunci().toString()).child(userID).removeValue()
            db.collection("Sharing").document(sessionManager.getKunci().toString())
                .collection("share").document(userID)
                .delete()
            val fr = fragmentManager?.beginTransaction()
            fr?.replace(R.id.nav_host_fragment, ShareRombongan())
            fr?.commit()


        }

        return root

    }



    fun ambildata() {

        referencesharing =
            FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                .child(sessionManager.getKunci().toString())

        referencesharing =
            FirebaseDatabase.getInstance().reference.child("Selecta")
                .child("sharing")
                .child(sessionManager.getKunci().toString())
        referencesharing.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("InflateParams")
            override fun onDataChange(p0: DataSnapshot) {

                mapboxMap.clear()
                mapboxMap.markers.clear()

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
                    var foto = user.foto
                    lokasi = LatLng(datalatitude, datalongitude)
                    /*               activity.updateMarkerPosition(lokasi)
                    */

                    //
                    mapboxMap.addMarker(
                        MarkerOptions().position(lokasi)
                            .title(nama)
                    )

                    if (setMarker == true) {

                        if (markerViewManager != null) {
                            customview = LayoutInflater.from(context!!.applicationContext)
                                .inflate(R.layout.marker, null)
                            customview.layoutParams =
                                FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                            val titleTextView: TextView =
                                customview.findViewById(R.id.marker_window_title)
                            val gambarView: ImageView =
                                customview.findViewById(R.id.gambarview)

                            Picasso.get().load(foto)
                                .into(gambarView)
                            marker = MarkerView(lokasi, customview)
                            titleTextView.text = nama
                            markerViewManager?.addMarker(marker)
                            markerMap.put(counter, marker)
                            counter++
                            //break

                        }

                    }

                }


            }
        })

    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {

            enableLocationComponent(it)
            markerViewManager = MarkerViewManager(mapboxfamily, mapboxMap)
            mapboxMap.addOnMapClickListener(this)

        }
        ambildata()


    }

    @SuppressLint("MissingPermission", "WrongConstant")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context?.applicationContext)) {
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                context?.applicationContext!!,
                loadedMapStyle
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            initLocationEngine()


        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(context?.applicationContext as Activity?)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(context?.applicationContext!!)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine!!.requestLocationUpdates(request, callback, context!!.mainLooper)
        locationEngine!!.getLastLocation(callback)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val preferences = context.getSharedPreferences("pref", 0)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(context!!.applicationContext)
            .registerOnSharedPreferenceChangeListener(this)

        mapboxfamily.onStart()
    }


    override fun onResume() {
        super.onResume()
        mapboxfamily.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapboxfamily.onPause()
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(context!!.applicationContext)
            .unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
        mapboxfamily.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapboxfamily.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapboxfamily.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (locationEngine != null) {
            locationEngine!!.removeLocationUpdates(callback)
        }
        mapboxfamily.onDestroy()

    }


    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        toast(R.string.user_location_permission_explanation)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (mapboxMap.style != null) {
                enableLocationComponent(mapboxMap.style!!)
            }
        } else {
            toast(R.string.user_location_permission_not_granted)
            activity?.finish()
        }

    }

    override fun onMapClick(point: LatLng): Boolean {
        /*     if (animator != null && animator.isStarted)
             {
                 currentPosition = animator.animatedValue as LatLng
                 animator.cancel()
             }
             animator = ObjectAnimator
                 .ofObject(latLngEvaluator, currentPosition, point)
                 .setDuration(2000)
             animator.addUpdateListener(animatorUpdateListener)
             animator.start()
             currentPosition = point
     */
        return true

    }

    @Suppress(
        "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION", "UNREACHABLE_CODE",
        "UNREACHABLE_CODE"
    )
    internal class MainActivityLocationCallbackBaru internal constructor(activity: Sharerombongandua) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<Sharerombongandua> =
            WeakReference(activity)
        lateinit var databaseReference: DatabaseReference

        override fun onSuccess(result: LocationEngineResult?) {
            val activity: Sharerombongandua? = activityWeakReference.get()


            if (activity != null) {
                val location: Location? = result?.lastLocation
                if (location == null) {
                    return

                }
                if (activity.statusjoin == true) {
                    databaseReference =
                        FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                   /* databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/latidude")
                        .setValue(result.lastLocation!!.latitude)
                    databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/longitude")
                        .setValue(result.lastLocation!!.longitude)*/
                    databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/name")
                        .setValue(activity.sessionManager.getprofil())
                    databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/foto")
                        .setValue(activity.sessionManager.getFoto())

                }

            }

        }


        @SuppressLint("LogNotTimber")
        override fun onFailure(exception: Exception) {
            Log.d("LocationChange", exception.localizedMessage)
            val activity: Sharerombongandua? = activityWeakReference.get()
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
        val intent = Intent(context!!.applicationContext, MyReceiver::class.java)
        intent.action = MyReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(
            context!!.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            context!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            context!!.applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun requestPermissions() {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                context!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val backgroundLocationPermissionApproved =
            (ActivityCompat.checkSelfPermission(
                context!!.applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val shouldProvideRationale =
            permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
               TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                find(R.id.sharerombongandua),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) { // Request permission
                    ActivityCompat.requestPermissions(
                        root!!.context.applicationContext as Activity, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                root!!.context as Activity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
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
                    find(R.id.sharerombongandua),
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


    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        s: String
    ) {

    }

    fun requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(context!!.applicationContext, true)
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            Utils.setRequestingLocationUpdates(context!!.applicationContext, false)
            e.printStackTrace()
        }
    }


}
