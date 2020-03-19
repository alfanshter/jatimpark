package com.alfanshter.jatimpark.ui.Calling

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.SinchService
import com.alfanshter.jatimpark.SinchStatus
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.alfanshter.jatimpark.ui.shareRombongan.sharerombongandua
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.android.synthetic.main.activity_waiting_calling.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.toast
import java.util.HashMap

class WaitingCalling : AppCompatActivity() {

    private var mUserId: String? = null
    private var mUserName: String? = null
    private var mCurrentId: String? = null
    private var mFirestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_calling)
           mFirestore = FirebaseFirestore.getInstance()
        mCurrentId = FirebaseAuth.getInstance().uid
        val notificationMessage: MutableMap<String, Any> =
            HashMap()
        notificationMessage["message"] = "Ada Panggilan Darurat"
        notificationMessage["from"] = mCurrentId!!
        panggilanProgress.visibility = View.VISIBLE
        mFirestore!!.collection("Admin/kVOopdPBHGhutxoo1965c4BvMTp1/Notifications")
            .add(notificationMessage).addOnSuccessListener {
                panggilanProgress.visibility = View.INVISIBLE
                toast("Request panggilan berhasil ")
                Appsc.sinchClient.isStarted
                startService<SinchService>()
            }.addOnFailureListener { e ->
                toast("Request Gagal silahkan coba lagi ")
                panggilanProgress.visibility = View.INVISIBLE
                    onBackPressed()
            }
    }


}

