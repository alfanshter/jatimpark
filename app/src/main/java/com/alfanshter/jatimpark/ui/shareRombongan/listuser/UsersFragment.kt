package com.alfanshter.jatimpark.ui.shareRombongan.listuser


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.Adapter.UsersRecyclerAdapter
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.Model.Users
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class UsersFragment : Fragment(),AnkoLogger {
    lateinit var mUsersListView: RecyclerView

    lateinit var usersList: MutableList<Users>
    private var usersRecyclerAdapter: UsersRecyclerAdapter? = null

    private var mFirestore: FirebaseFirestore? = null
    private lateinit var sessionManager: SessionManager

    var nama = ""
    var image =""
    var status = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_users, container, false)
        sessionManager = SessionManager(context)

        ambildataa()

        mUsersListView = root.findViewById(R.id.users_list_view)
        mFirestore = FirebaseFirestore.getInstance()


        usersList = ArrayList()
        usersRecyclerAdapter = UsersRecyclerAdapter(container!!.context,
            usersList as ArrayList<Users>
        )

        mUsersListView.setHasFixedSize(true)
        mUsersListView.layoutManager = LinearLayoutManager(container.context)
        mUsersListView.adapter = usersRecyclerAdapter

        return root
    }
  var namadata =""
    var imagedata =""
    var tokeniddata =""
    fun ambildataa()
    {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(uid).get()
            .addOnSuccessListener(object : OnSuccessListener<DocumentSnapshot>{
                override fun onSuccess(p0: DocumentSnapshot?) {
                    if (p0!!.exists()){
                        var nama = p0.getString("nama")
                        var image = p0.getString("image")
                        var tokenid = p0.getString("token_id")
                        namadata = nama.toString()
                        imagedata = image.toString()
                        tokeniddata = tokenid.toString()
                        info { "hasil : ${namadata}" }
                        upload()
                    }
                }

            })


        status = true
    }

    fun upload()
    {
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val userMap: MutableMap<String, Any?> =
            HashMap()
        userMap["nama"] = namadata
        userMap["image"]= imagedata
        userMap["token_id"] = tokeniddata
        info { "ngetes ${nama}"  }
        db.collection("Sharing").document(sessionManager.getKunci().toString()).collection("share").document(userID).set(userMap)

    }

    override fun onStart() {
        super.onStart()

        usersList.clear()
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        mFirestore!!.collection("Sharing").document(sessionManager.getKunci().toString()).collection("share").addSnapshotListener(
            activity!!,
            object : EventListener<QuerySnapshot> {

                override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    for (doc in documentSnapshots!!.documentChanges) {
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val user_id = doc.document.id
                            val users: Users = doc.document.toObject(Users::class.java).withId(user_id)
                            usersList.add(users)
                            usersRecyclerAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            })
    }


}
