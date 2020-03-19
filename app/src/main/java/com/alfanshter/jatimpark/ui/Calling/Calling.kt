package com.alfanshter.jatimpark.ui.Calling


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.alfanshter.jatimpark.Global
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.SinchService
import com.alfanshter.jatimpark.SinchStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast



/**
 * A simple [Fragment] subclass.
 */
class Calling : Fragment(),AnkoLogger {

    lateinit var panggilan : Button
    private var mFirestore: FirebaseFirestore? = null
    private var mUserId: String? = null
    private var mUserName: String? = null
    private var mCurrentId: String? = null
    private lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_calling, container, false)
        panggilan = root.find(R.id.panggil)
        sessionManager = SessionManager(context)

        panggilan.setOnClickListener(View.OnClickListener {
            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Melakukan panggilan darurat")
                .setContentText("Tekat OK untuk melanjutkan")
                .setConfirmText("OK")
                .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation()
                        startActivity<WaitingCalling>()
                    }
                .setCancelButton(
                    "Kembali"
                ) { sDialog -> sDialog.dismissWithAnimation() }
                .show()
        })
        return root

        }






}



