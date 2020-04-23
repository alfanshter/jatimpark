package com.alfanshter.jatimpark.ui.setting


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.ui.shareRombongan.Sharerombongandua
import org.jetbrains.anko.support.v4.toast

/**
 * A simple [Fragment] subclass.
 */
class Setting_Fragment : Fragment() {

    var listView: ListView? = null
    var array = arrayOf("Edit Profil","Email","Password")
private lateinit var sessionManager: SessionManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_setting_, container, false)
        val adapter = ArrayAdapter(context!!.applicationContext,
            R.layout.listview_item, array)
        val listView:ListView = root.findViewById(R.id.listview)

        sessionManager = SessionManager(context)
        sessionManager.setSetting("")
        listView.setAdapter(adapter)
        listView.onItemClickListener = object  : AdapterView.OnItemClickListener{
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val itemValue = listView.getItemAtPosition(position) as String
                toast(id.toString())
                sessionManager.setSetting(id.toString())
                val fr = fragmentManager?.beginTransaction()
                fr?.replace(R.id.nav_host_fragment, AnakSetting_Fragment())
                fr?.commit()
            }

        }
        return  root
    }


}
