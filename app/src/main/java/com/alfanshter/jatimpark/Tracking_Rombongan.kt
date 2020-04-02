@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.alfanshter.jatimpark

import android.Manifest
import android.Manifest.*
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SyncRequest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.auth.Login
import com.alfanshter.jatimpark.ui.Calling.Calling
import com.alfanshter.jatimpark.ui.Calling.Panggilan
import com.alfanshter.jatimpark.ui.Tracking.TrackingFragment
import com.alfanshter.jatimpark.ui.dashboard.DashboardFragment
import com.alfanshter.jatimpark.ui.generate.GenerateCode
import com.alfanshter.jatimpark.ui.shareRombongan.ShareRombongan
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.UsersFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_tracking__rombongan.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.net.CacheRequest
import java.util.*

class Tracking_Rombongan : AppCompatActivity() {
    val PERMISSIONS_REQUEST = 1

    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mAuth: FirebaseAuth? = null
    var latAwal:Double? = null
    var lonAwal:Double? = null
    var latAkhir:Double? = null
    var lonAkhir:Double? = null

    private lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var referencebaru: DatabaseReference
    lateinit var userID: String
    private lateinit var mainPresenter: MainPresenter
    private lateinit var sessionManager: SessionManager
    private var mFirestore: FirebaseFirestore? = null
    private var mUserId: String? = null
    private val REQUEST_CODE_PERMISSIONS = 101

    var namaprofil = ""
    var emailprofil = ""
    var gambarprofil = ""
        var nilai = false
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_dashboard -> {
                mainPresenter.changeFragment(supportFragmentManager,
                    DashboardFragment(),R.id.nav_host_fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                mainPresenter.changeFragment(supportFragmentManager,
                    TrackingFragment(),R.id.nav_host_fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_sharingmaps -> {
                mainPresenter.changeFragment(supportFragmentManager,
                    ShareRombongan(),R.id.nav_host_fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications ->{
                mainPresenter.changeFragment(supportFragmentManager,
                    Calling(),R.id.nav_host_fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    //Start the TrackerService//
//    private fun startTrackerService() {
//        startService(Intent(this, ServiceTracking::class.java))
//        //Notify the user that tracking has been enabled//
//        Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show()
//        //Close MainActivity//
//    }


//    companion object{
//        var instance:Tracking_Rombongan?=null
//
//        fun getMainInstance():Tracking_Rombongan{
//            return instance!!
//        }
//    }
//
//    fun updateTextView(value:String)
//    {
//        this@Tracking_Rombongan.runOnUiThread{
//            txt_location.text = value
//
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking__rombongan)
    checkPermission()
        mAuth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(this)
    sessionManager.setTelfon("hayo")
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Hello Toolbar"

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mUserId = mAuth!!.currentUser!!.uid



        mFirestore!!.collection("Users").document(mUserId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val user_email = documentSnapshot.getString("name")
                val user_image = documentSnapshot.getString("image")
                val user_nama = documentSnapshot.getString("nama")
                nama_drawer.text = user_email
                email_drawer.text = user_nama
                sessionManager.setprofil(user_nama.toString())
                val placeholderOption =
                    RequestOptions()
                placeholderOption.placeholder(R.drawable.username)
                Glide.with(container.context).setDefaultRequestOptions(placeholderOption)
                    .load(user_image).into(gambardrawer)
                sessionManager.setFoto(user_image.toString())
            }


        //upload info
        referencebaru = FirebaseDatabase.getInstance().reference.child("Selecta").child("Users")
        /*referencebaru.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
            namaprofil = p0.child(auth.uid.toString()+"/nama").value.toString()
                gambarprofil = p0.child(auth.uid.toString()+"/gambar").value.toString()
                emailprofil = p0.child(auth.uid.toString()+"/email").value.toString()
                nama_drawer.text = namaprofil
                Picasso.get().load(gambarprofil)
                    .into(gambardrawer)
                email_drawer.text = emailprofil
            }
        })*/
        //========

        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
        container,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ){

        }

        drawerToggle.isDrawerIndicatorEnabled = true
        container.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener{
            when (it.itemId) {
                R.id.navigation_generate -> {
                    mainPresenter.changeFragment(supportFragmentManager,
                        GenerateCode(),R.id.nav_host_fragment)
                }
                R.id.nav_logout -> {

                    val tokenMapRemove: MutableMap<String, Any> =
                        HashMap()
                    tokenMapRemove["token_id"] = FieldValue.delete()

                    mFirestore!!.collection("Users").document(mUserId!!).update(tokenMapRemove)
                        .addOnSuccessListener {
                            mAuth!!.signOut()
                            sessionManager.setLogin(false)
                            sessionManager.setNama(false)
                            startActivity<Login>()
                            finish()

                        }

                }
            }
            container.closeDrawer(GravityCompat.START)
            true

        }
        mainPresenter = MainPresenter()
        mainPresenter.changeFragment(supportFragmentManager,
            DashboardFragment(),R.id.nav_host_fragment)
        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    @SuppressLint("InlinedApi")
    fun checkPermission(){

        val permissionAccessCoarseLocationApproved = ActivityCompat
            .checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved = ActivityCompat
                .checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            if (backgroundLocationPermissionApproved) {
               startService<ServiceLocation>()
                // App can access location both in the foreground and in the background.
                // Start your service that doesn't have a foreground service type
                // defined.
            } else {
                // App can only access location in the foreground. Display a dialog
                // warning the user that your app must have all-the-time access to
                startService<ServiceLocation>()
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var foreground = false
            var background = false
            for (i in permissions.indices) {
                if (permissions[i].equals(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        ignoreCase = true
                    )
                ) { //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        foreground = true
                        Toast.makeText(
                            getApplicationContext(),
                            "Foreground location permission allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                        continue
                    } else {
                        Toast.makeText(
                            getApplicationContext(),
                            "Location Permission denied",
                            Toast.LENGTH_SHORT
                        ).show()
                        break
                    }
                }
                if (permissions[i].equals(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        ignoreCase = true
                    )
                ) {
                    if (grantResults[i] >= 0) {
                        foreground = true
                        background = true
                        Toast.makeText(
                            getApplicationContext(),
                            "Background location location permission allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            getApplicationContext(),
                            "Background location location permission denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (foreground) {
                if (background) {
                    toast("Start Foreground and Background Location Updates")
                    startService(Intent(this, ServiceLocation::class.java))
                } else {
                    toast("Start foreground location updates")
                    startService(Intent(this, ServiceLocation::class.java))
                }
            }
        }

    }


//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//
//        //If the permission has been granted...//
//
//        if (requestCode == PERMISSIONS_REQUEST && grantResults.size == 1
//            && grantResults[0] == PERMISSION_GRANTED
//        ) {
//
//            //...then start the GPS tracking service//
//
//            startService(Intent(this, ServiceLocation::class.java))
//
//        } else {
//
//            //If the user denies the permission request, then display a toast with some more information//
//
//            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show()
//        }
//    }


//    private fun updateLocation() {
//        buildLocationRequest()
//        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=
//                PackageManager.PERMISSION_GRANTED)
//            return
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent())
//    }
//
//    private fun getPendingIntent(): PendingIntent? {
//            val intent = Intent(this@Tracking_Rombongan, ServiceTracking::class.java)
//             intent.action = ServiceTracking.ACTION_PROCESS_UPDATE
//            return PendingIntent.getBroadcast(this@Tracking_Rombongan,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
//    }
//
//    private fun buildLocationRequest() {
//        locationRequest = LocationRequest()
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        locationRequest.interval = 5000
//        locationRequest.fastestInterval = 3000
//        locationRequest.smallestDisplacement = 10f
//
//    }

}
