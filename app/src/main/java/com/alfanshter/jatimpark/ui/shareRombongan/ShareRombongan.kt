@file:Suppress("DEPRECATION")

package com.alfanshter.jatimpark.ui.shareRombongan

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import cn.pedant.SweetAlert.SweetAlertDialog
import com.alfanshter.jatimpark.Model.ModelBaru
import com.alfanshter.jatimpark.Model.ModelSharing
import com.alfanshter.jatimpark.Model.Util.FcmPush
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.ui.Calling.WaitingCalling
import com.alfanshter.jatimpark.ui.dashboard.DashboardFragment
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoQuery
import com.goodiebag.pinview.Pinview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.share_rombongan_fragment.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.lang.Exception
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.internal.InlineMarker
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ShareRombongan : Fragment(),  AnkoLogger{

    private lateinit var viewModel: ShareRombonganViewModel
    lateinit var user: FirebaseUser
    lateinit var userID: String
    private lateinit var butonkode: ImageView
    private lateinit var generate :ImageView
    private lateinit var scan : ImageView
    lateinit var sessionManager: SessionManager
    private lateinit var referencejoin: DatabaseReference
    private lateinit var pin : Pinview
    lateinit var auth : FirebaseAuth
// variabel untuk send notif
    private var mUserId: String? = null
    private var mUserName: String? = null
    val myDate = Date()
    val format1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())
    val date :String = format1.format(myDate)
    val random = Random()
    val n:Int = 100000 + random.nextInt(900000)
    private val code = n.toString()
    var nama = ""
    lateinit var reference: DatabaseReference
    lateinit var referencebaru: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(context!!.applicationContext, getString(R.string.access_token))
        viewModel = ViewModelProviders.of(this).get(ShareRombonganViewModel::class.java)
        val root = inflater.inflate(R.layout.share_rombongan_fragment, container, false)
        //getter setter
        sessionManager = SessionManager(context)
        //inisialisasi button di fragment
        butonkode = root.find(R.id.sharingbuton)
        pin = root.find(R.id.pinsharing)
        generate = root.find(R.id.generate)
        scan = root.find(R.id.scan)
        //inisialisasi userid
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser!!.uid

        mUserId = activity!!.intent.getStringExtra("user_id")
        mUserName = activity!!.intent.getStringExtra("user_name")

        scan.setOnClickListener {
            val fr = fragmentManager?.beginTransaction()
            fr?.replace(R.id.nav_host_fragment,Scanner())
            fr?.commit()


        }
        generate.setOnClickListener {
            GenerateFungsi()
        }

        butonkode.setOnClickListener {

            referencejoin = FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
            var pinjoin = pin.value.toString()
            referencejoin.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        info("informasi : ${it.key}")
                        if (it.key.toString().equals(pinjoin)) {
                            sessionManager.setKunci(pin.value.toString())
                            sessionManager.setIDStatusUser("1")
                            val fr = fragmentManager?.beginTransaction()
                            fr?.replace(R.id.nav_host_fragment,sharerombongandua())
                            fr?.commit()
                        }
                    }
                }
            })

        }

        if (sessionManager.getIDStatusUser().equals("1"))
        {
            val fr = fragmentManager?.beginTransaction()
            fr?.replace(R.id.nav_host_fragment,sharerombongandua())
            fr?.commit()
        }


        return root
    }





    private fun GenerateFungsi() {

        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Apakah anda ingin mendapatkan kode ?")
            .setContentText("Tekat OK untuk melanjutkan")
            .setConfirmText("OK")
            .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation()
                try {
                    val text = code
                    user = auth.currentUser!!
                    userID = user.uid
                    reference = FirebaseDatabase.getInstance().reference.child("Selecta/Users")
                    referencebaru = FirebaseDatabase.getInstance().reference.child("Selecta")
                    val barcodeEndocer = BarcodeEncoder()
                    val bitmap = barcodeEndocer.encodeBitmap(text, BarcodeFormat.QR_CODE,400,400)
                    qrcode.setImageBitmap(bitmap)
                    pin.value = text
                    reference.child(userID).child("key").setValue(code)
                    reference.child(userID).child("date").setValue(date)
                    referencebaru.child("sharing").child(code).child(userID).setValue(ModelBaru("", -7.817527,    112.524507,"https://cdn0-production-images-kly.akamaized.net/RxUo9GGJA_1oDBlykD64OHEuVVg=/640x360/smart/filters:quality(75):strip_icc():format(jpeg)/kly-media-production/medias/2396228/original/055603500_1540900672-lion-3040797_1920.jpg"))

                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                toast("berhasil")
                            }

                            else
                            {
                                toast("gagal")
                                info("Gagal : ${task.exception?.message}")
                            }

                        }

                }catch (e : Exception){

                }     }
            .setCancelButton(
                "Kembali"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()


    }



}
