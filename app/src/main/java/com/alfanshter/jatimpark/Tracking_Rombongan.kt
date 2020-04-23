@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.alfanshter.jatimpark

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.alfanshter.jatimpark.Activity.InfoPembuat
import com.alfanshter.jatimpark.Model.ModelUsers
import com.alfanshter.jatimpark.Session.SessionManager
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tracking__rombongan.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.jetbrains.anko.startActivity
import java.util.*

class Tracking_Rombongan : AppCompatActivity() {
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

        mAuth = FirebaseAuth.getInstance()

        sessionManager = SessionManager(this)
    sessionManager.setTelfon("hayo")
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Selecta"

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
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

                val placeholderOption =
                    RequestOptions()
                nama_drawer.text = namaprofil
                Glide.with(container.context).setDefaultRequestOptions(placeholderOption).load(gambarprofil).into(gambardrawer)
                email_drawer.text = emailprofil
                sessionManager.setFoto(ambildata.image.toString())
                sessionManager.setprofil(ambildata.nama.toString())
            }
        })
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if ((requestCode == PERMISSIONS_REQUEST && grantResults.size == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
        }
        else
        {
            finish()
        }


    }




}
