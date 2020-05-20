package com.alfanshter.jatimpark.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.jatimpark.Model.BannerPromo
import com.alfanshter.jatimpark.Model.ModelUsers
import com.alfanshter.jatimpark.Model.UsersInfo
import com.alfanshter.jatimpark.R
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.glide.slider.library.SliderLayout
import com.glide.slider.library.animations.DescriptionAnimation
import com.glide.slider.library.slidertypes.BaseSliderView
import com.glide.slider.library.slidertypes.TextSliderView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity


@Suppress("UNREACHABLE_CODE")
class DashboardFragment : Fragment(), AnkoLogger {
    private lateinit var recyclerView: RecyclerView
    private var groupAdapter = GroupAdapter<ViewHolder>()
    lateinit var databaseReference: DatabaseReference
    lateinit var refinfo: DatabaseReference
    lateinit var show_progress: ProgressBar
    lateinit var mSlider: SliderLayout
    var image_list: HashMap<String, String>? = null
    lateinit var reference: DatabaseReference
    lateinit var root: View
    var bannersatu = ""
    var promos: List<BannerPromo> = listOf()
    var status = ""

    companion object{
        lateinit var textSliderView : TextSliderView

    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val time = System.currentTimeMillis()
        info { "alfanbaru$time" }

        val musa = FirebaseAuth.getInstance()
        val telfon = musa.currentUser!!.phoneNumber
        info { "telfon : $telfon" }
        databaseReference = FirebaseDatabase.getInstance().reference.child("Selecta").child("Home")
        recyclerView = root.find(R.id.recyclerinfo)

        textSliderView = TextSliderView(context!!.applicationContext)

        val LayoutManager = LinearLayoutManager(context!!.applicationContext)
        LayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = LayoutManager
        refinfo = FirebaseDatabase.getInstance().reference.child("Selecta").child("info")

        val option =
            FirebaseRecyclerOptions.Builder<UsersInfo>().setQuery(refinfo, UsersInfo::class.java)
                .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<UsersInfo, MyViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val itemView = LayoutInflater.from(context?.applicationContext)
                        .inflate(R.layout.list_item, parent, false)
                    return MyViewHolder(itemView)
                }

                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    position: Int,
                    model: UsersInfo
                ) {
                    val refid = getRef(position).key.toString()
                    refinfo.child(refid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            holder.mtitle.setText(model.nama)
                            Picasso.get().load(model.gambar).fit().centerCrop().into(holder.mimage)
                            holder.itemView.setOnClickListener {
                                startActivity<information>(
                                    "Firebase_Image" to model.gambar,
                                    "Firebase_title" to model.nama,
                                    "Firebase_isi" to model.deskripsi
                                )


                            }
                        }

                    })
                }
            }
        setupslider()

        recyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
        return root

    }

    private fun setupslider() {
        mSlider = root.findViewById(R.id.slider)
        image_list = HashMap()
        reference = FirebaseDatabase.getInstance().reference.child("Selecta").child("info")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (data in p0.children) {
                    var banner = data.getValue(ModelUsers::class.java)
                    image_list!![banner!!.nama.toString() + "_" + banner.id.toString()] =
                        banner.gambar.toString()
                }
                val requestOptions = RequestOptions()
                requestOptions.centerCrop()
                for (key in image_list!!.keys) {
                    var keysplit = key.split("_")
                    val nama = keysplit[0]
                    val id = keysplit[1]
             textSliderView
                        .description(nama)
                        .image(image_list!!.get(key))
                        .setRequestOption(requestOptions)
                        .setProgressBarVisible(true)
                        .setOnSliderClickListener(object : BaseSliderView.OnSliderClickListener {
                            override fun onSliderClick(slider: BaseSliderView?) {
                                val intent = Intent(context!!.applicationContext,information::class.java)
                                intent.putExtras(textSliderView.bundle)
                                startActivity(intent)
                            }

                        })

                    //add extra bundle
                    textSliderView.bundle(Bundle())
                    textSliderView.bundle.putString("id",id)
                    mSlider.addSlider(textSliderView)


                    //remove banner
                    reference.removeEventListener(this)


                }
            }

        })

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground)
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
        mSlider.setCustomAnimation(DescriptionAnimation())
        mSlider.setDuration(4000)


    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView.findViewById(R.id.txvTitle)
        var mimage: ImageView = itemView.findViewById(R.id.gambar)
    }

    override fun onStop() {
        super.onStop()
        mSlider.startAutoCycle()

    }

}