package com.alfanshter.jatimpark.auth

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alfanshter.jatimpark.FirebaseHelper.FirebaseHelper
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE = 1
    private var mStorage: StorageReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mFirestore: FirebaseFirestore? = null
    lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null
    private var myUrl = ""
    lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        imageUri = null
        progressDialog = ProgressDialog(this)
        mStorage = FirebaseStorage.getInstance().reference.child("images")
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()


        auth = FirebaseAuth.getInstance()
        signup.setOnClickListener {
            daftar()
        }

        register_image_btn.setOnClickListener {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),PICK_IMAGE
            )
        }
    }


    private fun daftar() {
        if (imageUri != null) {
            progressDialog.setTitle("Tunggu...")
            progressDialog.show()
            registerProgressBar.visibility = View.VISIBLE
            val email = email.text.toString().trim()
            val username = user.text.toString().trim()
            val password = pass.text.toString().trim()

            val longitude  =  "112.926894"
            val latitude  = "-7.705582"

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user_id = mAuth!!.currentUser!!.uid
                            val user_profile = mStorage!!.child("$user_id.jpg") //filerife
                            var uploadTask : StorageTask<*>
                            uploadTask = user_profile.putFile(imageUri!!)
                            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                                if (!task.isSuccessful){
                                    task.exception?.let {
                                        throw  it
                                        registerProgressBar.visibility = View.INVISIBLE
                                    }
                                }
                                return@Continuation user_profile.downloadUrl
                            }).addOnCompleteListener(OnCompleteListener { task ->
                                if (uploadTask.isSuccessful) {
                                    databaseReference = FirebaseDatabase.getInstance().getReference("Selecta").child("Users").child(user_id)

                                    val downloadUrl = task.result
                                    myUrl = downloadUrl.toString()

                                    val token_id =
                                        FirebaseInstanceId.getInstance().token
                                    val userMap: MutableMap<String, Any?> =
                                        HashMap()
                                    userMap["name"] = email
                                    userMap["image"] = myUrl
                                    userMap["token_id"] = token_id
                                    userMap["password"] = password
                                    userMap["nama"] = username
                                    userMap["longitude"] = longitude
                                    userMap["latitude"] = latitude
                                    userMap["email"] = email
                                    userMap["iduser"] = user_id
                                    databaseReference.setValue(userMap)
                                    mFirestore!!.collection("Users").document(user_id)
                                        .set(userMap).addOnSuccessListener {
                                            registerProgressBar.visibility = View.INVISIBLE
                                            sendToMain()
                                        }.addOnFailureListener { e ->
                                            progressDialog.dismiss()
                                            Toast.makeText(
                                                this@register,
                                                "Error : " + e.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                            registerProgressBar.visibility = View.INVISIBLE
                                        }
                                } else {
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this@register,
                                        "Error : " + uploadTask.exception!!.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    registerProgressBar.visibility = View.INVISIBLE
                                }
                            })


                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@register,
                                "Error : " + task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            registerProgressBar.visibility = View.INVISIBLE
                        }
                    }

            } else {
                toast("isi semua from")
            }
        }

        else{
            toast("Upload foto terlebih dahulu")
        }

    }

    private fun sendToMain() {
        startActivity<Tracking_Rombongan>()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE)
        {
            imageUri = data!!.data
            register_image_btn!!.setImageURI(imageUri)

        }
    }

}
