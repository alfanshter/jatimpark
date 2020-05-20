@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.alfanshter.jatimpark

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.alfanshter.jatimpark.Activity.InfoPembuat
import com.alfanshter.jatimpark.Model.ModelUsers
import com.alfanshter.jatimpark.ServiceLocation.MyReceiver
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Utils.Utils
import com.alfanshter.jatimpark.auth.Login
import com.alfanshter.jatimpark.ui.Calling.Calling
import com.alfanshter.jatimpark.ui.Tracking.TrackingFragment
import com.alfanshter.jatimpark.ui.dashboard.DashboardFragment
import com.alfanshter.jatimpark.ui.generate.GenerateCode
import com.alfanshter.jatimpark.ui.setting.Setting_Fragment
import com.alfanshter.jatimpark.ui.shareRombongan.ShareRombongan
import com.alfanshter.jatimpark.ui.shareRombongan.Sharerombongandua
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.UsersFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tracking__rombongan.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.find
import java.util.*

class Tracking_Rombongan : AppCompatActivity(),AnkoLogger {


    //lokasi
    companion object{
        private val TAG: String? = Tracking_Rombongan::class.simpleName
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private var root: View? = null
        private val UPDATE_INTERVAL: Long = 15000 // Every 60 seconds.
        private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 30 seconds
        private val MAX_WAIT_TIME: Long = UPDATE_INTERVAL * 5 // Every 5 minutes.

    }
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    // UI Widgets.
    private var mRequestUpdatesButton: Button? = null
    private var mRemoveUpdatesButton: Button? = null
    //=========
    val PERMISSIONS_REQUEST = 1

    private var mAuth: FirebaseAuth? = null

    private lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var referencebaru: DatabaseReference
    lateinit var userID: String
    private lateinit var mainPresenter: MainPresenter
    private lateinit var sessionManager: SessionManager
    private var mFirestore: FirebaseFirestore? = null
    private var mUserId: String? = null

    var namaprofil = ""
    var emailprofil = ""
    var gambarprofil = ""
        var nilai = false
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction().detach(Sharerombongandua())
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking__rombongan)

        if (!checkPermissions()) {
            requestPermissions()
        }
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()


        mAuth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(this)
    sessionManager.setTelfon("hayo")
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Selecta"
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            container,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ){

        }
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        info { "tesbaru ${auth.currentUser!!.displayName}" }
        mUserId = auth.currentUser!!.uid
        //upload info
        referencebaru = FirebaseDatabase.getInstance().reference.child("Selecta").child("Users").child(mUserId.toString())
        referencebaru.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                    var ambildata = p0.getValue(ModelUsers::class.java)
                    namaprofil = ambildata!!.nama.toString()
                    emailprofil = ambildata.email.toString()
                    gambarprofil = ambildata.image.toString()
                val navigationview = findViewById<NavigationView>(R.id.navigationView)
                val headerview = navigationview.getHeaderView(0)

                val placeholderOption =
                    RequestOptions()
                val navname = headerview.findViewById<TextView>(R.id.nama_drawer)
                val navemail = headerview.findViewById<TextView>(R.id.email_drawer)

                val navgambar = headerview.findViewById<ImageView>(R.id.gambardrawer)
                    navname.text = namaprofil
                Glide.with(container.context).setDefaultRequestOptions(placeholderOption).load(gambarprofil).into(navgambar)
                navemail.text = emailprofil
                sessionManager.setFoto(ambildata.image.toString())
                sessionManager.setprofil(ambildata.nama.toString())
            }
        })
        //========



        drawerToggle.isDrawerIndicatorEnabled = true
        container.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener{
            when (it.itemId) {
                R.id.nav_logout -> {
                    Utils.setRequestingLocationUpdates(this, false)
                    mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())

                    val tokenMapRemove: MutableMap<String, Any> =
                        HashMap()
                    tokenMapRemove["token_id"] = FieldValue.delete()

                    mFirestore!!.collection("Users").document(mUserId!!).update(tokenMapRemove)
                        .addOnSuccessListener {
/*
                            if (sessionManager.getIDStatusUser().equals("1"))
                            {
                                Utils.setRequestingLocationUpdates(this, false)
                                mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())

                            }
*/
                            sessionManager.setIDStatusUser("0")

                            mAuth!!.signOut()
                            sessionManager.setLogin(false)
                            sessionManager.setNama(false)
                            startActivity<Login>()
                            finish()

                        }

                }

                R.id.settings -> {
                    mainPresenter.changeFragment(supportFragmentManager,
                        Setting_Fragment(),R.id.nav_host_fragment)

                }

                R.id.info_drawable -> {
                   startActivity<InfoPembuat>()

                }




            }
            container.closeDrawer(GravityCompat.START)
            true

        }
        mainPresenter = MainPresenter()
        mainPresenter.changeFragment(supportFragmentManager,
            DashboardFragment(),R.id.nav_host_fragment)
        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (sessionManager.getviewuser().equals("1"))
        {
            mainPresenter.changeFragment(supportFragmentManager,
                UsersFragment(),R.id.nav_host_fragment)
        }
        sessionManager.setviewuser("")
    }
    private fun requestPermissions() {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val backgroundLocationPermissionApproved =
            (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val shouldProvideRationale =
            permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                Tracking_Rombongan.TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                find(R.id.sharerombonganduaactivity),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) { // Request permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
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
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
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
        val intent = Intent(this, MyReceiver::class.java)
        intent.action = MyReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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




}
