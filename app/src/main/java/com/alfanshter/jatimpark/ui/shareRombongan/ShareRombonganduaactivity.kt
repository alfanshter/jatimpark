package com.alfanshter.jatimpark.ui.shareRombongan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.jatimpark.Model.ModelSharing

import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.ServiceLocation.MyReceiver
import com.alfanshter.jatimpark.Session.SessionManager
import com.alfanshter.jatimpark.Tracking_Rombongan
import com.alfanshter.jatimpark.Utils.Utils
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.Adapter.UsersRecyclerAdapter
import com.alfanshter.jatimpark.ui.shareRombongan.listuser.Model.Users
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.BuildConfig
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.ImageSource
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_share_rombonganduaactivity.*
import kotlinx.android.synthetic.main.fragment_sharerombongandua.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlinx.android.synthetic.main.fragment_sharerombongandua.btn_qrcode as btn_qrcode1

class ShareRombonganduaactivity : AppCompatActivity(), AnkoLogger, OnMapReadyCallback,
    PermissionsListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var trace: Trace
    lateinit var databaseReference: DatabaseReference
    lateinit var mapboxMap: MapboxMap
    private var callback = MainActivityLocationCallbackBaru(this)
    private lateinit var customview: View
    private var markerViewManager: MarkerViewManager? = null
    var lokasi = LatLng(00.00, 00.00)
    private lateinit var marker: MarkerView
    private var setMarker: Boolean = false
    private lateinit var locationComponent: LocationComponent
    private var locationEngine: LocationEngine? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private var statusjoin: Boolean = false
    lateinit var sessionManager: SessionManager
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    @SuppressLint("UseSparseArrays")
    var markerMap: HashMap<Int, MarkerView> = HashMap<Int, MarkerView>()
    private lateinit var auth: FirebaseAuth
    lateinit var userID: String
    lateinit var user: FirebaseUser
    lateinit var referensehapus: DatabaseReference
    var statusupdate = 0

    private val numStartupTasks = CountDownLatch(2)
    private val ID_IMAGE_SOURCE = "animated_image_source"
    private val ID_IMAGE_SOURCEdua = "animated_image_source2"

    private val ID_IMAGE_LAYER = "animated_image_layer"
    private val ID_IMAGE_LAYERdua = "animated_image_layer2"

    //variabel restoran
    private val ID_SOURCE_Restoran = "source_restoran"
    private val ID_IMAGE_Restoran = "layer_restoran"

    //variabel kolamdewasa
    private val ID_SOURCE_PoolDewasa = "source_pool"
    private val ID_LAYER_PoolDewasa = "layer_pool"

    //variabel kamarganti
    private val ID_SOURCE_KamarGanti = "source_kmrganti"
    private val ID_LAYER_KamarGanti = "layer_kmrganti"

    //variabel restoranasri
    private val ID_SOURCE_RestoranAsri = "source_restoranasri"
    private val ID_LAYER_RestoranAsri = "layer_restoranasri"

    //variabel cienam
    private val ID_SOURCE_Cinema = "source_cinema"
    private val ID_LAYER_Cinema = "layer_cinema"
    //variabel RestoranSederhana
    private val ID_SOURCE_RestoranSederhana = "source_RestoranSederhana"
    private val ID_LAYER_RestoranSederhana = "layer_RestoranSederhana"
    //variabel Bianglala
    private val ID_SOURCE_Bianglala = "source_Bianglala"
    private val ID_LAYER_Bianglala = "layer_Bianglala"

    //variabel Singa
    private val ID_SOURCE_Singa = "source_Singa"
    private val ID_LAYER_Singa = "layer_Singa"

    //variabel Ayunan
    private val ID_SOURCE_Ayunan = "source_Ayunan"
    private val ID_LAYER_Ayunan = "layer_Ayunan"

    //variabel Coster
    private val ID_SOURCE_Coster = "source_Coster"
    private val ID_LAYER_Coster = "layer_Coster"

    //variabel ToiletHutan
    private val ID_SOURCE_ToiletHutan = "source_ToiletHutan"
    private val ID_LAYER_ToiletHutan = "layer_ToiletHutan"
    //variabel PacuanKuda
    private val ID_SOURCE_PacuanKuda = "source_PacuanKuda"
    private val ID_LAYER_PacuanKuda = "layer_PacuanKuda"
    //variabel Bungasatu
    private val ID_SOURCE_Bungasatu = "source_Bungasatu"
    private val ID_LAYER_Bungasatu = "layer_Bungasatu"
    //variabel Bungadua
    private val ID_SOURCE_Bungadua = "source_Bungadua"
    private val ID_LAYER_Bungadua = "layer_BungaBungadua"
    //variabel Bungatiga
    private val ID_SOURCE_Bungatiga = "source_Bungatiga"
    private val ID_LAYER_Bungatiga = "layer_Bungatiga"
    //variabel Bungaempat
    private val ID_SOURCE_Bungaempat = "source_Bungaempat"
    private val ID_LAYER_Bungaempat = "layer_Bungaempat"
    //variabel Bungalima
    private val ID_SOURCE_Bungalima = "source_Bungalima"
    private val ID_LAYER_Bungalima = "layer_Bungalima"
    //variabel Bungaenam
    private val ID_SOURCE_Bungaenam = "source_Bungaenam"
    private val ID_LAYER_Bungaenam = "layer_Bungaenam"
    //variabel Bungatujuh
    private val ID_SOURCE_Bungatujuh = "source_Bungatujuh"
    private val ID_LAYER_Bungatujuh = "layer_Bungatujuh"
    //variabel Bungadelapan
    private val ID_SOURCE_Bungadelapan = "source_Bungadelapan"
    private val ID_LAYER_Bungadelapan = "layer_Bungadelapan"
    //variabel Bungasembilan
    private val ID_SOURCE_Bungasembilan = "source_Bungasembilan"
    private val ID_LAYER_Bungasembilan = "layer_Bungasembilan"
    //variabel ToiletUjung
    private val ID_SOURCE_ToiletUjung = "source_ToiletUjung"
    private val ID_LAYER_ToiletUjung = "layer_ToiletUjung"
    //variabel PasebanSriAgung
    private val ID_SOURCE_PasebanSriAgung = "source_PasebanSriAgung"
    private val ID_LAYER_PasebanSriAgung = "layer_PasebanSriAgung"

    companion object {

        fun convertStreamToString(`is`: InputStream?): String {
            val scanner = Scanner(`is`).useDelimiter("\\A")
            return if (scanner.hasNext()) scanner.next() else ""
        }

        private val TAG: String? = ShareRombonganduaactivity::class.simpleName
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private var root: View? = null
        private val UPDATE_INTERVAL: Long = 15000 // Every 60 seconds.
        private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 30 seconds
        private val MAX_WAIT_TIME: Long = UPDATE_INTERVAL * 5 // Every 5 minutes.

        lateinit var nilaikode: String
        private const val STARTUP_TRACE_NAME = "trace_ambilgambar"
        private const val REQUESTS_COUNTER_NAME = "requests sent"
        private const val FILE_SIZE_COUNTER_NAME = "file size"
    }
    private var mLocationRequest: LocationRequest? = null

    var dialog_bidding: Dialog? = null


    //punya Lihat USer
    lateinit var usersList: MutableList<Users>
    private var usersRecyclerAdapter: UsersRecyclerAdapter? = null

    private var mFirestore: FirebaseFirestore? = null
    var nama = ""
    var image =""
    var status = false

    //======================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_share_rombonganduaactivity)

        rombongandualayout.visibility = View.VISIBLE
        layoutuserr.visibility = View.INVISIBLE
        //show  pop up
        dialog_bidding = Dialog(this)
        dialog_bidding!!.setContentView(R.layout.popupqrcode)
        //============

        if (!checkPermissions()) {
            requestPermissions()
        }
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        Utils.setRequestingLocationUpdates(this, true)
        mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())

        mapviewshare.onCreate(savedInstanceState)
        mapviewshare.getMapAsync(this)
        sessionManager = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userID = user.uid
        val db = FirebaseFirestore.getInstance()

        getdataview()
        usersya()
        home.setOnClickListener {
            startActivity<Tracking_Rombongan>()
        }


        viewuserbaru.setOnClickListener {
            rombongandualayout.visibility = View.INVISIBLE
            layoutuserr.visibility = View.VISIBLE
        }

        back.setOnClickListener {
            rombongandualayout.visibility = View.VISIBLE
            layoutuserr.visibility = View.INVISIBLE

        }
        keluar.setOnClickListener {
            statusupdate = 1
            Utils.setRequestingLocationUpdates(this, false)
            mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())

            sessionManager.setIDStatusUser("")
            statusjoin = false
            referensehapus =
                FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
            referensehapus.child(sessionManager.getKunci().toString()).child(userID).removeValue()
            db.collection("Sharing").document(sessionManager.getKunci().toString())
                .collection("share").document(userID)
                .delete()
            startActivity<Tracking_Rombongan>()

        }

        btn_qrcode.setOnClickListener {
            showpopup()
        }



    }
    private fun requestPermissions() {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val backgroundLocationPermissionApproved =
            (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        val shouldProvideRationale =
            permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
               ShareRombonganduaactivity.TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                find(R.id.sharerombonganduaactivity),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) { // Request permission
                    ActivityCompat.requestPermissions(
                       this, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
    fun removeLocationUpdates(view: View?) {
        Log.i(TAG, "Removing location updates")
        Utils.setRequestingLocationUpdates(this, false)
        mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.LIGHT) {
            // Set the latitude and longitude values for the image's four corners
            val quad = LatLngQuad(
                LatLng(-7.817532, 112.525955),
                LatLng(-7.817291, 112.525754),
                LatLng(-7.817399, 112.525500),
                LatLng(-7.817588, 112.525624)
            )

            val kolamanak = LatLngQuad(
                LatLng(-7.81818, 112.52519),
                LatLng(-7.81812, 112.52536),
                LatLng(-7.81786, 112.52517),
                LatLng(-7.81790, 112.52506)
            )

            val restoran = LatLngQuad(
                LatLng(-7.818378, 112.525175),
                LatLng(-7.818515, 112.525235),
                LatLng(-7.818564, 112.525129),
                LatLng(-7.818416, 112.525073)


            )

            val pooldewasa = LatLngQuad(
                LatLng(-7.818183, 112.525362),
                LatLng(-7.818494, 112.525534),
                LatLng(-7.818558, 112.525436),
                LatLng(-7.818241, 112.525266)

            )

            val kamarganti = LatLngQuad(
                LatLng(-7.818159, 112.525160),
                LatLng(-7.818094, 112.525275),
                LatLng(-7.818147, 112.525306),
                LatLng(-7.818211, 112.525189)

            )
            val restoranasri = LatLngQuad(
                LatLng(-7.818006, 112.525548),
                LatLng(-7.818185, 112.525663),
                LatLng(-7.818254, 112.525527),
                LatLng(-7.818090, 112.525432)

            )

            val cinema = LatLngQuad(
                LatLng(-7.819043, 112.525304),
                LatLng(-7.818888, 112.525524),
                LatLng(-7.818992, 112.525580),
                LatLng(-7.819135, 112.525359)

            )

            val restoransederhana = LatLngQuad(
                LatLng(-7.817520, 112.525263),
                LatLng(-7.817764, 112.525396),
                LatLng(-7.817842, 112.525246),
                LatLng(-7.817607, 112.525123)

            )

            val Bianglala = LatLngQuad(
                LatLng(-7.818230, 112.524732),
                LatLng(-7.818357, 112.524753),
                LatLng(-7.818404, 112.524676),
                LatLng(-7.818273, 112.524629)

            )

            val Singa = LatLngQuad(
                LatLng(-7.818772, 112.524917),
                LatLng(-7.818836, 112.524949),
                LatLng(-7.818866, 112.524883),
                LatLng(-7.818797, 112.524845)
            )

            val Ayunan = LatLngQuad(
                LatLng(-7.817832, 112.524725),
                LatLng(-7.817961, 112.524777),
                LatLng(-7.818020, 112.524619),
                LatLng(-7.817892, 112.524589)
            )

            val Coster = LatLngQuad(
                LatLng(-7.817547, 112.524675),
                LatLng(-7.817671, 112.524731),
                LatLng(-7.817746, 112.524545),
                LatLng(-7.817620, 112.524473)
            )

            val ToiletHutan = LatLngQuad(
                LatLng(-7.817184, 112.524339),
                LatLng(-7.817287, 112.524373),
                LatLng(-7.817322, 112.524276),
                LatLng(-7.817206, 112.524219)
            )
            val PacuanKuda = LatLngQuad(
                LatLng(-7.815977, 112.523985),
                LatLng(-7.816294, 112.524155),
                LatLng(-7.816380, 112.523915),
                LatLng(-7.816043, 112.523751)
            )
            val Bungasatu = LatLngQuad(
                LatLng(-7.817321, 112.524935),
                LatLng(-7.817537, 112.525014),
                LatLng(-7.817591, 112.524870),
                LatLng(-7.817367, 112.524818)
            )
            val Bungadua = LatLngQuad(
                LatLng(-7.817109, 112.524853),
                LatLng(-7.817284, 112.524924),
                LatLng(-7.817335, 112.524810),
                LatLng(-7.817154, 112.524755)
            )

            val Bungatiga = LatLngQuad(
                LatLng(-7.816913, 112.524767),
                LatLng(-7.817091, 112.524845),
                LatLng(-7.817122, 112.524749),
                LatLng(-7.816954, 112.524691)
            )
            val Bungaempat = LatLngQuad(
                LatLng(-7.817408, 112.525089),
                LatLng(-7.817526, 112.525132),
                LatLng(-7.817550, 112.525072),
                LatLng(-7.817433, 112.525032)
            )
            val Bungalima = LatLngQuad(
                LatLng(-7.817290, 112.525036),
                LatLng(-7.817392, 112.525077),
                LatLng(-7.817412, 112.525022),
                LatLng(-7.817307, 112.524981)
            )
            val Bungaenam = LatLngQuad(
                LatLng(-7.817153, 112.524978),
                LatLng(-7.817263, 112.525026),
                LatLng(-7.817283, 112.524973),
                LatLng(-7.817172, 112.524932)
            )
            val Bungatujuh = LatLngQuad(
                LatLng(-7.817055, 112.524940),
                LatLng(-7.817140, 112.524967),
                LatLng(-7.817158, 112.524925),
                LatLng(-7.817074, 112.524893)
            )
            val Bungadelapan = LatLngQuad(
                LatLng(-7.816686, 112.524832),
                LatLng(-7.816981, 112.524948),
                LatLng(-7.817018, 112.524873),
                LatLng(-7.816732, 112.524753)
            )

            val Bungasembilan = LatLngQuad(
                LatLng(-7.816603, 112.524613),
                LatLng(-7.816878, 112.524725),
                LatLng(-7.816912, 112.524631),
                LatLng(-7.816652, 112.524523)
            )
            val ToiletUjung = LatLngQuad(
                LatLng(-7.816183, 112.524787),
                LatLng(-7.816293, 112.524833),
                LatLng(-7.816346, 112.524722),
                LatLng(-7.816267, 112.524671)
            )
            val PasebanSriAgung = LatLngQuad(
                LatLng(-7.815906, 112.524621),
                LatLng(-7.816025, 112.524661),
                LatLng(-7.816094, 112.524501),
                LatLng(-7.815977, 112.524455)
            )





            it.addSource(ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.selecta))
            it.addSource(ImageSource(ID_IMAGE_SOURCEdua, kolamanak, R.drawable.pool))
            it.addLayer(RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE))
            it.addLayer(RasterLayer(ID_IMAGE_LAYERdua, ID_IMAGE_SOURCEdua))

            //gambar restoran
            it.addSource(ImageSource(ID_SOURCE_Restoran, restoran, R.drawable.restaurant))
            it.addLayer(RasterLayer(ID_IMAGE_Restoran, ID_SOURCE_Restoran))
            //gambar kolamdewasa
            it.addSource(ImageSource(ID_SOURCE_PoolDewasa, pooldewasa, R.drawable.pooldewasa))
            it.addLayer(RasterLayer(ID_LAYER_PoolDewasa, ID_SOURCE_PoolDewasa))
            //gambar kamarganti
            it.addSource(ImageSource(ID_SOURCE_KamarGanti, kamarganti, R.drawable.kamarganti))
            it.addLayer(RasterLayer(ID_LAYER_KamarGanti, ID_SOURCE_KamarGanti))
            //gambar Restoranasri
            it.addSource(ImageSource(ID_SOURCE_RestoranAsri, restoranasri, R.drawable.restoranasri))
            it.addLayer(RasterLayer(ID_LAYER_RestoranAsri, ID_SOURCE_RestoranAsri))
            //gambar cinema
            it.addSource(ImageSource(ID_SOURCE_Cinema, cinema, R.drawable.cinema))
            it.addLayer(RasterLayer(ID_LAYER_Cinema, ID_SOURCE_Cinema))
            //gambar RestoranSederhana
            it.addSource(ImageSource(ID_SOURCE_RestoranSederhana, restoransederhana, R.drawable.restoranasri))
            it.addLayer(RasterLayer(ID_LAYER_RestoranSederhana, ID_SOURCE_RestoranSederhana))
            //gambar Bianglala
            it.addSource(ImageSource(ID_SOURCE_Bianglala, Bianglala, R.drawable.bianglala))
            it.addLayer(RasterLayer(ID_LAYER_Bianglala, ID_SOURCE_Bianglala))
            //gambar Singa
            it.addSource(ImageSource(ID_SOURCE_Singa, Singa, R.drawable.lion))
            it.addLayer(RasterLayer(ID_LAYER_Singa, ID_SOURCE_Singa))
            //gambar Ayunan
            it.addSource(ImageSource(ID_SOURCE_Ayunan, Ayunan, R.drawable.ayunan))
            it.addLayer(RasterLayer(ID_LAYER_Ayunan, ID_SOURCE_Ayunan))
            //gambar Coster
            it.addSource(ImageSource(ID_SOURCE_Coster, Coster, R.drawable.costerpng))
            it.addLayer(RasterLayer(ID_LAYER_Coster, ID_SOURCE_Coster))
            //gambar ToiletHutan
            it.addSource(ImageSource(ID_SOURCE_ToiletHutan, ToiletHutan, R.drawable.kamarganti))
            it.addLayer(RasterLayer(ID_LAYER_ToiletHutan, ID_SOURCE_ToiletHutan))
            //gambar PacuanKuda
            it.addSource(ImageSource(ID_SOURCE_PacuanKuda, PacuanKuda, R.drawable.horse))
            it.addLayer(RasterLayer(ID_LAYER_PacuanKuda, ID_SOURCE_PacuanKuda))
            //gambar Bungasatu
            it.addSource(ImageSource(ID_SOURCE_Bungasatu, Bungasatu, R.drawable.bungasatu))
            it.addLayer(RasterLayer(ID_LAYER_Bungasatu, ID_SOURCE_Bungasatu))
            //gambar Bungadua
            it.addSource(ImageSource(ID_SOURCE_Bungadua, Bungadua, R.drawable.bungadua))
            it.addLayer(RasterLayer(ID_LAYER_Bungadua, ID_SOURCE_Bungadua))
            //gambar Bungatiga
            it.addSource(ImageSource(ID_SOURCE_Bungatiga, Bungatiga, R.drawable.bungatiga))
            it.addLayer(RasterLayer(ID_LAYER_Bungatiga, ID_SOURCE_Bungatiga))
            //gambar Bungaempat
            it.addSource(ImageSource(ID_SOURCE_Bungaempat, Bungaempat, R.drawable.bungaempat))
            it.addLayer(RasterLayer(ID_LAYER_Bungaempat, ID_SOURCE_Bungaempat))
            //gambar Bungalima
            it.addSource(ImageSource(ID_SOURCE_Bungalima, Bungalima, R.drawable.bungalima))
            it.addLayer(RasterLayer(ID_LAYER_Bungalima, ID_SOURCE_Bungalima))
            //gambar Bungaenam
            it.addSource(ImageSource(ID_SOURCE_Bungaenam, Bungaenam, R.drawable.bungaenam))
            it.addLayer(RasterLayer(ID_LAYER_Bungaenam, ID_SOURCE_Bungaenam))
            //gambar Bungatujuh
            it.addSource(ImageSource(ID_SOURCE_Bungatujuh, Bungatujuh, R.drawable.bungatujuh))
            it.addLayer(RasterLayer(ID_LAYER_Bungatujuh, ID_SOURCE_Bungatujuh))
            //gambar Bungadelapan
            it.addSource(ImageSource(ID_SOURCE_Bungadelapan, Bungadelapan, R.drawable.bungadelapan))
            it.addLayer(RasterLayer(ID_LAYER_Bungadelapan, ID_SOURCE_Bungadelapan))
            //gambar Bungasembilan
            it.addSource(ImageSource(ID_SOURCE_Bungasembilan, Bungasembilan, R.drawable.bungasembilan))
            it.addLayer(RasterLayer(ID_LAYER_Bungasembilan, ID_SOURCE_Bungasembilan))
            //gambar ToiletUjung
            it.addSource(ImageSource(ID_SOURCE_ToiletUjung, ToiletUjung, R.drawable.kamarganti))
            it.addLayer(RasterLayer(ID_LAYER_ToiletUjung, ID_SOURCE_ToiletUjung))
            //gambar PasebanSriAgung
            it.addSource(ImageSource(ID_SOURCE_PasebanSriAgung, PasebanSriAgung, R.drawable.hall))
            it.addLayer(RasterLayer(ID_LAYER_PasebanSriAgung, ID_SOURCE_PasebanSriAgung))
            LoadGeoJson(this).execute()

            enableLocationComponent(it)
            markerViewManager = MarkerViewManager(mapviewshare, mapboxMap)
            setMarker = true
            if (setMarker == true) {
                if (statusupdate==0)
                {
                    trace = FirebasePerformance.getInstance().newTrace(STARTUP_TRACE_NAME)
                    Log.d(TAG, "Starting trace")
                    trace.start()
                    ambildata()
                    trace.incrementMetric(REQUESTS_COUNTER_NAME, 1)
                    Thread(Runnable {
                        try {
                            numStartupTasks.await()
                        } catch (e: InterruptedException) {
                            Log.e(TAG, "Unable to wait for startup task completion.")
                        } finally {
                            Log.d(TAG, "Stopping trace")
                            trace.stop()
                            runOnUiThread {
                                toast("Trace complete")
                            }
                        }
                    }).start()

                }

            }
        }


    }

    private class LoadGeoJson internal constructor(activity: ShareRombonganduaactivity) :
        AsyncTask<Void?, Void?, FeatureCollection?>() {
        private val weakReference: WeakReference<ShareRombonganduaactivity> = WeakReference(
            activity
        )


        override fun onPostExecute(featureCollection: FeatureCollection?) {
            super.onPostExecute(featureCollection)
            val activity: ShareRombonganduaactivity? =
                weakReference.get()
            if (activity != null && featureCollection != null) {
                activity.drawLines(featureCollection)
            }
        }

        override fun doInBackground(vararg params: Void?): FeatureCollection? {
            try {
                val activity: ShareRombonganduaactivity? =
                    weakReference.get()
                if (activity != null) {

                    val inputStream: InputStream? =
                        activity.application.assets?.open("example.geojson")
                    return FeatureCollection.fromJson(
                        convertStreamToString(
                            inputStream
                        )
                    )
                }
            } catch (exception: Exception) {
                Timber.e("Exception Loading GeoJSON: %s", exception.toString())
            }
            return null
        }

    }

    private fun drawLines(featureCollection: FeatureCollection) {
        mapboxMap.getStyle { style: Style ->
            if (featureCollection.features() != null) {
                if (featureCollection.features()!!.size > 0) {
                    style.addSource(GeoJsonSource("line-source", featureCollection))
                    // The layer properties for our line. This is where we make the line dotted, set the
                    // color, etc.
                    style.addLayer(
                        LineLayer("linelayer", "line-source")
                            .withProperties(
                                PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                                PropertyFactory.lineOpacity(
                                    .5f
                                ),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(
                                    Color.parseColor("#34b0d0")
                                )
                            )
                    )
                }
            }
        }
    }

    fun ambildata() {
        // [START perf_manual_network_trace]

        databaseReference =
            FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                .child(sessionManager.getKunci().toString())
        info { "hasil ${sessionManager.getKunci().toString()}"  }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("InflateParams", "CheckResult")
            override fun onDataChange(p0: DataSnapshot) {
                for (values in markerMap.values) {
                    markerViewManager?.removeMarker(values)
                }
                //   markerViewManager?.removeMarker(marker)
                var counter: Int = 0
                for (i in p0.children) {
                    var user: ModelSharing? =
                        i.getValue(ModelSharing::class.java)
                    var datalongitude = user!!.longitude
                    var datalatitude = user.latidude
                    var nama = user.name
                    var foto = user.image

                    customview = LayoutInflater.from(this@ShareRombonganduaactivity)
                        .inflate(R.layout.marker, null)
                    customview.layoutParams =
                        FrameLayout.LayoutParams(
                            ConstraintSet.WRAP_CONTENT, ConstraintSet.WRAP_CONTENT
                        )

                    val titleTextView: TextView = customview.findViewById(R.id.marker_window_title)
                    val gambarView: ImageView = customview.findViewById(R.id.gambarview)

                    lokasi = LatLng(datalatitude, datalongitude)
/*
                    Picasso.get().load(foto).resize(50, 50).into(gambarView)
*/


                    Glide.with(this@ShareRombonganduaactivity).load(foto).override(50,50).listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            numStartupTasks.countDown() // Signal end of image load task.
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            numStartupTasks.countDown() // Signal end of image load task.
                            return false
                        }

                    }).into(gambarView)

                    marker = MarkerView(lokasi, customview)
                    titleTextView.text = nama
                    markerViewManager?.addMarker(marker)
                    markerMap.put(counter, marker)
                    counter++
                }


            }

        })

    }

    fun manualNetworkTrace() {
        val data = "badgerbadgerbadgerbadgerMUSHROOM!".toByteArray()

        // [START perf_manual_network_trace]
        val metric = FirebasePerformance.getInstance().newHttpMetric("https://www.google.com",
            FirebasePerformance.HttpMethod.GET)
        val url = URL("https://www.google.com")
        metric.start()
        val conn = url.openConnection() as HttpURLConnection
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        try {
            val outputStream = DataOutputStream(conn.outputStream)
            outputStream.write(data)
        } catch (ignored: IOException) {
        }

        metric.setRequestPayloadSize(data.size.toLong())
        metric.setHttpResponseCode(conn.responseCode)
        printStreamContent(conn.inputStream)

        conn.disconnect()
        metric.stop()
        // [END perf_manual_network_trace]
    }

    private fun printStreamContent(inputStream: InputStream) {

    }

    override fun onStart() {
        super.onStart()

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        mapviewshare.onStart()

        usersList.clear()
        val auth = FirebaseAuth.getInstance()
        val userID = auth.currentUser!!.uid
        mFirestore!!.collection("Sharing").document(sessionManager.getKunci().toString()).collection("share").addSnapshotListener(
            this,
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

    override fun onResume() {
        super.onResume()
        mapviewshare.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapviewshare.onPause()
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
        mapviewshare.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapviewshare.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapviewshare.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (markerViewManager != null) {
            markerViewManager!!.onDestroy()
        }
        mapviewshare.onDestroy()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        toast(R.string.user_location_permission_explanation)

    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (mapboxMap.style != null) {
                enableLocationComponent(mapboxMap.style!!)
            }
        } else {
            toast(R.string.user_location_permission_not_granted)
            finish()
        }


    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                this!!,
                loadedMapStyle
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            initLocationEngine()


        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this as Activity?)
        }
    }

    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine!!.requestLocationUpdates(request, callback, this.mainLooper)
        locationEngine!!.getLastLocation(callback)
    }




    internal class MainActivityLocationCallbackBaru internal constructor(activity: ShareRombonganduaactivity) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<ShareRombonganduaactivity> =
            WeakReference(activity)
        lateinit var databaseReference: DatabaseReference

        override fun onSuccess(result: LocationEngineResult?) {
            val activity: ShareRombonganduaactivity? = activityWeakReference.get()


            if (activity != null) {
                val location: Location? = result?.lastLocation
                if (location == null) {
                    return

                }

                    databaseReference =
                        FirebaseDatabase.getInstance().reference.child("Selecta").child("sharing")
                    /* databaseReference.child(activity.sessionManager.getKunci().toString())
                         .child("${activity.userID}/latidude")
                         .setValue(result.lastLocation!!.latitude)
                     databaseReference.child(activity.sessionManager.getKunci().toString())
                         .child("${activity.userID}/longitude")
                         .setValue(result.lastLocation!!.longitude)*/
              /*      databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/name")
                        .setValue(activity.sessionManager.getprofil())
                    databaseReference.child(activity.sessionManager.getKunci().toString())
                        .child("${activity.userID}/image")
                        .setValue(activity.sessionManager.getFoto())
*/
            }

        }


        @SuppressLint("LogNotTimber")
        override fun onFailure(exception: Exception) {
            Log.d("LocationChange", exception.localizedMessage)
            val activity: ShareRombonganduaactivity? = activityWeakReference.get()
            if (activity != null) {
                activity.toast(exception.localizedMessage)

            }

        }

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest!!.interval = UPDATE_INTERVAL

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest!!.maxWaitTime = MAX_WAIT_TIME
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission was granted.
                requestLocationUpdates()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                    find(R.id.sharerombonganduaactivity),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) { // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    fun requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(this, true)
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            e.printStackTrace()
        }
    }

    private fun getPendingIntent(): PendingIntent {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val intent = Intent(this, MyReceiver::class.java)
        intent.action = MyReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {


    }

    fun showpopup(){
        val textClose: TextView
        val qrcode: ImageView
        val id: TextView

        qrcode = dialog_bidding!!.findViewById(R.id.img_qrcode)
        textClose = dialog_bidding!!.findViewById(R.id.txtclose)
        id = dialog_bidding!!.findViewById(R.id.txt_popup)

        val gambar = sessionManager.getKunci()
        val barcodeEndocer = BarcodeEncoder()
        val bitmap = barcodeEndocer.encodeBitmap(gambar, BarcodeFormat.QR_CODE,400,400)
        qrcode.setImageBitmap(bitmap)
        id.text = sessionManager.getKunci()


        textClose.setOnClickListener { dialog_bidding!!.dismiss() }
        dialog_bidding!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog_bidding!!.show()
    }

    var namadata =""
    var imagedata =""
    var tokeniddata =""

    fun usersya()
    {
        mFirestore = FirebaseFirestore.getInstance()
        usersList = ArrayList()
        usersRecyclerAdapter = UsersRecyclerAdapter(this,
            usersList
        )

        viewuserrecycler.setHasFixedSize(true)
        viewuserrecycler.layoutManager = LinearLayoutManager(this)
        viewuserrecycler.adapter = usersRecyclerAdapter

    }
    
    fun getdataview()
    {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(uid).get()
            .addOnSuccessListener(object : OnSuccessListener<DocumentSnapshot> {
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


}
