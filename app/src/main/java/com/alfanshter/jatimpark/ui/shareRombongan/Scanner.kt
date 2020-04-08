package com.alfanshter.jatimpark.ui.shareRombongan


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.google.firebase.database.*
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.support.v4.toast

/**
 * A simple [Fragment] subclass.
 */
class Scanner : Fragment(),ZXingScannerView.ResultHandler {

    private var zxingScannerView: ZXingScannerView? = null
    private val TAG = Scanner::class.qualifiedName
    private lateinit var referencejoin: DatabaseReference
    lateinit var sessionManager: SessionManager

    companion object {

        fun newInstance() {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(context)
        zxingScannerView = ZXingScannerView(context)
        zxingScannerView!!.setAspectTolerance(0.5f)
        zxingScannerView?.setResultHandler(this)
        return zxingScannerView
    }

    override fun handleResult(rawResult: Result?) {
        zxingScannerView?.startCamera()
        if (zxingScannerView!=null)
        {
            var pinjoin = rawResult.toString()
            Toast.makeText(context, "Item $rawResult Scanned", Toast.LENGTH_SHORT).show()
            referencejoin = FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
            referencejoin.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        if (it.key.toString().equals(pinjoin)) {
                            sessionManager.setKunci(pinjoin)
                            sessionManager.setIDStatusUser("1")
                            val fr = fragmentManager?.beginTransaction()
                            fr?.replace(R.id.nav_host_fragment,Sharerombongandua())
                            fr?.commit()
                        }
                        else{
                            val fr = fragmentManager?.beginTransaction()
                            fr?.replace(R.id.nav_host_fragment,ShareRombongan())
                            fr?.commit()
                            toast("silahkan ulangi lagi kode tidak cocok")

                        }
                    }
                }
            })


        }

    }


    override fun onPause() {
        super.onPause()
        zxingScannerView?.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        zxingScannerView?.setResultHandler(this)
        zxingScannerView?.startCamera()
    }

}
