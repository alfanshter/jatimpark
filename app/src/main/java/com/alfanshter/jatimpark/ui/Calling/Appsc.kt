package com.alfanshter.jatimpark.ui.Calling

import android.annotation.SuppressLint
import android.app.Application
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sinch.android.rtc.Sinch
import com.sinch.android.rtc.SinchClient
import com.sinch.android.rtc.calling.CallClient

@SuppressLint("Registered")
class Appsc : Application() {
    lateinit var auth : FirebaseAuth
    var userID = ""
    lateinit var db : FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
//        auth = FirebaseAuth.getInstance()
//        userID = auth.currentUser!!.uid
//        db = FirebaseFirestore.getInstance()
//        db.collection("Users").document(userID).get().addOnSuccessListener(object : OnSuccessListener<DocumentSnapshot>{
//            override fun onSuccess(p0: DocumentSnapshot?) {
//                var telepon = p0!!.getString("telepon")
//                USER_ID = ("" + "$telepon").replace("-", "")
//                sinchClient = Sinch.getSinchClientBuilder().context(this@Appsc)
//                    .applicationKey("e5178df1-dfff-474e-9194-1e775ed10632")
//                    .applicationSecret("hHHdDN9Sh0GmSbyR2PtxWQ==")
//                    .environmentHost("clientapi.sinch.com")
//                    .userId(USER_ID)
//                    .build()
//
//                sinchClient.setSupportActiveConnectionInBackground(true)
//                sinchClient.startListeningOnActiveConnection()
//                sinchClient.setSupportCalling(true)
//
//            }
//
//        })


    }

    companion object {
        lateinit var USER_ID: String
        lateinit var sinchClient: SinchClient
        lateinit var callClient: CallClient
    }
}
