package com.alfanshter.jatimpark.ui.setting


import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.alfanshter.jatimpark.Model.ModelUsers
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_anak_setting_.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast


/**
 * A simple [Fragment] subclass.
 */
class AnakSetting_Fragment : Fragment(), AnkoLogger {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var mFirestore: FirebaseFirestore? = null
    private lateinit var sessionManager: SessionManager
    lateinit var reference: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog
    lateinit var btnNama: Button
    lateinit var authenticate: Button
    lateinit var edtnama: EditText
    lateinit var nama: EditText
    lateinit var update: Button
    lateinit var edit_text_password: EditText
    lateinit var edit_text_new_pass: EditText
    lateinit var edit_text_pass: EditText
    lateinit var layoutUpdateEmail: LinearLayout
    lateinit var layoutPassword: LinearLayout
    lateinit var layoutPass: LinearLayout
    lateinit var layoutUpdatePass: LinearLayout
    lateinit var button_update: Button
    lateinit var button_authenticate: Button
    lateinit var foto: CircleImageView
    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null
    private var mStorage: StorageReference? = null
    var userid = ""
    private var myUrl = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_anak_setting_, container, false)
        mStorage = FirebaseStorage.getInstance().reference.child("images")
        auth = FirebaseAuth.getInstance()
        userid = auth.currentUser!!.uid
        var alfan = auth.currentUser!!.photoUrl
        info { "foto : $alfan" }
        sessionManager = SessionManager(context)
        progressDialog = ProgressDialog(context)
        btnNama = root.find(R.id.btn_nama)
        edtnama = root.find(R.id.namabaru)
        nama = root.find(R.id.nama)
        foto = root.find(R.id.foto)
        edit_text_password = root.find(R.id.edit_text_password)
        layoutPassword = root.find<LinearLayout>(R.id.layoutPassword)
        layoutUpdateEmail = root.find(R.id.layoutUpdateEmail)
        button_update = root.find(R.id.button_update)
        button_authenticate = root.find(R.id.button_authenticate)
        layoutPass = root.find(R.id.layoutPass)
        layoutUpdatePass = root.find(R.id.layoutUpdatePass)
        authenticate = root.find(R.id.authenticate)
        edit_text_pass = root.find(R.id.edit_text_pass)
        edit_text_new_pass = root.find(R.id.edit_text_new_pass)
        update = root.find(R.id.update)

        imageUri = null
        reference =
            FirebaseDatabase.getInstance().reference.child("Selecta")
        mFirestore = FirebaseFirestore.getInstance()
        if (sessionManager.getSetting().equals("0")) {
            root.find<ConstraintLayout>(R.id.akun).visibility = View.VISIBLE
            root.find<ConstraintLayout>(R.id.email).visibility = View.INVISIBLE
            root.find<ConstraintLayout>(R.id.changepassword).visibility = View.INVISIBLE
            reference.child("Users").child(userid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        var data = p0.getValue(ModelUsers::class.java)
                        nama.setText(data!!.nama.toString())
                    }

                })
            btnNama.setOnClickListener {
                gantinama()
            }

            foto.setOnClickListener {
                setfoto()
            }


        } else if (sessionManager.getSetting().equals("1")) {
            root.find<ConstraintLayout>(R.id.changepassword).visibility = View.INVISIBLE
            root.find<ConstraintLayout>(R.id.akun).visibility = View.INVISIBLE
            root.find<ConstraintLayout>(R.id.email).visibility = View.VISIBLE

            layoutPassword.visibility = View.VISIBLE
            layoutUpdateEmail.visibility = View.GONE

            button_authenticate.setOnClickListener {
                progressDialog.setTitle("Sedang di Proses ...")
                progressDialog.show()
                updateemail()
            }

            button_update.setOnClickListener {
                updateemailsetelahpassword()
            }


        } else if (sessionManager.getSetting().equals("2")) {
            root.find<ConstraintLayout>(R.id.akun).visibility = View.INVISIBLE
            root.find<ConstraintLayout>(R.id.email).visibility = View.INVISIBLE
            root.find<ConstraintLayout>(R.id.changepassword).visibility = View.VISIBLE
            layoutPass.visibility = View.VISIBLE
            layoutUpdatePass.visibility = View.GONE

            authenticate.setOnClickListener {
                checkpassword()
            }

            update.setOnClickListener {
                updatepassword()

            }

        }



        return root
    }

    fun gantinama() {
        val nama = edtnama.text.toString().trim()
        val user_profile = mStorage!!.child("$userid.jpg") //filerife

        if (imageUri != null) {

            if (nama.isNotEmpty()) {
                progressDialog.setTitle("Update Nama ....")
                progressDialog.show()
                var uploadTask: StorageTask<*>
                uploadTask = user_profile.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw  it

                        }
                    }
                    return@Continuation user_profile.downloadUrl
                }).addOnCompleteListener {
                    if (uploadTask.isSuccessful) {
                        val downloadUrl = it.result
                        myUrl = downloadUrl.toString()
                        val update: MutableMap<String, Any?> = HashMap()
                        update["image"] = myUrl
                        update["nama"] = nama

                        reference.child("Users").child(userid).updateChildren(update)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    progressDialog.dismiss()
                                } else {
                                    progressDialog.dismiss()
                                    toast("silahkan coba lagi")
                                }
                            }


                        if (sessionManager.getIDStatusUser().equals("1")) {
                            val sharing: MutableMap<String, Any?> = HashMap()
                            sharing["name"] = nama
                            sharing["image"] = myUrl
                            reference.child("sharing").child(sessionManager.getKunci().toString())
                                .child(userid).updateChildren(sharing)

                        }
                    }

                }


            } else {
                toast("harus diisi perubahan nama")
            }

        } else if (imageUri == null) {

            if (nama.isNotEmpty()) {
                progressDialog.setTitle("Update Nama ....")
                progressDialog.show()

                val update: MutableMap<String, Any?> = HashMap()
                update["nama"] = nama
                reference.child("Users").child(userid).updateChildren(update)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressDialog.dismiss()
                        } else {
                            progressDialog.dismiss()
                            toast("silahkan coba lagi")
                        }
                    }

                if (sessionManager.getIDStatusUser().equals("1")) {
                    val sharing: MutableMap<String, Any?> = HashMap()
                    sharing["name"] = nama
                    reference.child("sharing").child(sessionManager.getKunci().toString())
                        .child(userid).updateChildren(sharing)

                }

            } else {
                toast("harus diisi perubahan nama")
            }
        }


    }

    fun setfoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"), PICK_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            imageUri = data!!.data
            foto.setImageURI(imageUri)

        }
    }

    fun updateemail() {
        val password = edit_text_password.text.toString().trim()
        if (password.isEmpty()) {
            edit_text_password.error = "Password required"
            edit_text_password.requestFocus()
            return
        }
        currentUser?.let { user ->
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            progressDialog.setTitle("Tunggu>>>")
            progressDialog.show()
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    when {
                        task.isSuccessful -> {
                            layoutPassword.visibility = View.GONE
                            layoutUpdateEmail.visibility = View.VISIBLE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            edit_text_password.error = "Invalid Password"
                            edit_text_password.requestFocus()
                        }
                        else -> context?.toast(task.exception?.message!!)
                    }
                }

        }


    }

    fun updateemailsetelahpassword() {
        button_update.setOnClickListener { view ->
            val email = edit_text_email.text.toString().trim()

            if (email.isEmpty()) {
                edit_text_email.error = "Email tidak boleh kosong"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edit_text_email.error = "Email tidka sesuai"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }
            progressDialog.setTitle("Tunggu")
            progressDialog.show()
            currentUser?.let { user ->
                user.updateEmail(email)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val usermap: MutableMap<String, Any?> = HashMap()
                            usermap["email"] = email
                            reference.child("Users").child(userid).updateChildren(usermap)
                            toast("berhasil")
                        } else {
                            context?.toast(task.exception?.message!!)
                        }
                    }

            }
        }
    }

    fun checkpassword() {
        val password = edit_text_pass.text.toString().trim()

        if (password.isEmpty()) {
            edit_text_pass.error = "Password harus diisi"
            edit_text_pass.requestFocus()
            return
        }
        currentUser?.let { user ->
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            progressDialog.setTitle("Tunggu ....")
            progressDialog.show()
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    when {
                        task.isSuccessful -> {
                            layoutPass.visibility = View.GONE
                            layoutUpdatePass.visibility = View.VISIBLE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            edit_text_pass.error = "Password Salah"
                            edit_text_pass.requestFocus()
                        }
                        else -> context?.toast(task.exception?.message!!)
                    }
                }
        }

    }

    fun updatepassword() {
        val password = edit_text_new_pass.text.toString().trim()
        if (password.isEmpty() || password.length < 6) {
            edit_text_new_pass.error = "password tidak boleh kurang dari 6"
            edit_text_new_pass.requestFocus()
            return
        }
        if (password != edit_text_new_pass.text.toString().trim()) {
            edit_text_new_pass.error = "password salah"
            edit_text_new_pass.requestFocus()
            return
        }

        currentUser?.let { user ->
            progressDialog.setTitle("Tunggu ...")
            progressDialog.show()
            user.updatePassword(password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val usermap: MutableMap<String, Any?> = HashMap()
                        usermap["password"] = password
                        reference.child("Users").child(userid).updateChildren(usermap)
                        //upload ke firestore
                        mFirestore!!.collection("Users").document(userid).update(usermap)
                        progressDialog.dismiss()
                        toast("berhasil")

                    } else {
                        context?.toast(task.exception?.message!!)
                    }
                }
        }

    }
}
