@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.alfanshter.jatimpark

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.auth.Login
import com.alfanshter.jatimpark.ui.Calling.Calling
import com.alfanshter.jatimpark.ui.Tracking.TrackingFragment
import com.alfanshter.jatimpark.ui.dashboard.DashboardFragment
import com.alfanshter.jatimpark.ui.generate.GenerateCode
import com.alfanshter.jatimpark.ui.shareRombongan.ShareRombongan
import com.alfanshter.jatimpark.ui.shareRombongan.Sharerombongandua
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_tracking__rombongan.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.jetbrains.anko.startActivity
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
                            sessionManager.setIDStatusUser("0")
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
/*
    private fun startTrackerService() {
        startService(Intent(this, TrackerService::class.java))
        finish()
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if ((requestCode == PERMISSIONS_REQUEST && grantResults.size == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
//             startTrackerService()
        }
        else
        {
            finish()
        }


    }




}
