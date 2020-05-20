@file:Suppress("UNREACHABLE_CODE")

package com.alfanshter.jatimpark.ui.Tracking

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alfanshter.jatimpark.R
import com.alfanshter.jatimpark.Session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.ImageSource
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.*


@Suppress(
    "CAST_NEVER_SUCCEEDS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "SENSELESS_COMPARISON", "DEPRECATION"
)
class TrackingFragment : Fragment(), OnMapReadyCallback, PermissionsListener,
    MapboxMap.OnMapClickListener {

    private lateinit var trackingViewModel: TrackingViewModel


    var reference: DatabaseReference? = null

    var user: FirebaseUser? = null
    var userID: String? = null
    var auth: FirebaseAuth? = null


    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var mapboxMap: MapboxMap
    private lateinit var startnavigasi: Button
    lateinit var locationComponent: LocationComponent

    //variabel menggambar rute
    private var currentRoute: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null

    //=====================
    private var navigationView: NavigationView? = null
    var origin = Point.fromLngLat(112.5265, -7.8196)


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



    private var locationEngine: LocationEngine? = null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    lateinit var butonnavigasi: ImageView
    lateinit var toilet: ImageView
    private var callback = MainActivityLocationCallback(this)

    var lastlatitude: String? = null

    //==============VARIABEL RECYCLERVIEW=================================
    private val coordinates: Array<LatLng> = arrayOf<LatLng>(
        LatLng(-7.819021, 112.525411),    //cinema
        LatLng(-7.818360, 112.525417),      // kolam dewasa
        LatLng(-7.818039, 112.525205),          // kolam anak
        LatLng(-7.818310, 112.524731),          // bianglala
        LatLng(-7.818811, 112.524904),          //Singa
        LatLng(-7.817937, 112.524660)      , //ayunan
        LatLng(-7.817663, 112.524512),       //coster
        LatLng(-7.816245, 112.524172)       //Pacuan kuda
    )

    private val namalayout: Array<String> = arrayOf(
        "Bioskop 4D",
        "Kolam Renang",
        "Kolam Renang Anak",
        "Bianglala",
        "Singa",
        "Ayunan",
        "Coaster",
        "Pacuan Kuda"
    )

    private val gambarrecycler = intArrayOf(
        R.drawable.bioskop,         //cinema
        R.drawable.kolamdewasa,          //kolam dewasa
        R.drawable.selectatiga,         //kolam anak
        R.drawable.bianglala,        //bianglala
        R.drawable.goasinga,           //singa
        R.drawable.ayunan,           //ayunan
        R.drawable.coster,
        R.drawable.pacuankuda
    )

    private val SYMBOL_ICON_ID = "SYMBOL_ICON_ID"
    private val SOURCE_ID = "SOURCE_ID"
    private val LAYER_ID = "LAYER_ID"
    private var featureCollection: FeatureCollection? = null
    lateinit var recyclerView: RecyclerView

    //====================================================================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        Mapbox.getInstance(context!!.applicationContext, getString(R.string.access_token))
        trackingViewModel = ViewModelProviders.of(this).get(TrackingViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val context: Context = inflater.context
        val mapView = view.findViewById(R.id.mapbox) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        toilet = view.find(R.id.toilet)
        butonnavigasi = view.find(R.id.navigasi)
        recyclerView = view.find(R.id.rv_on_top_of_map)

        toilet.setOnClickListener {
            val selectedLocationLatLng = LatLng(-7.817232, 112.524332)
            val newCameraPosition: CameraPosition = CameraPosition.Builder()
                .target(selectedLocationLatLng)
                .build()
            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
            toast("toilet")
        }
        butonnavigasi.setOnClickListener {
            var simulateRoute = true
            var options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
/*
                    .shouldSimulateRoute(simulateRoute)
*/
                .build()
            NavigationLauncher.startNavigation(view.context as Activity?, options)

        }
        butonnavigasi.isEnabled = true

        return view

    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(

            Style.Builder()
                .fromUri(Style.LIGHT) // Set up the image, source, and layer for the person icon,
// which is where all of the routes will start from
        ) {

            mapboxMap.addMarker(
                MarkerOptions().position(LatLng(-7.817540, 112.525251))
                    .title("musholla")

            )

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
            addDestinationIconSymbolLayer(it)
            initFeatureCollection()
            initMarkerIcons(it)
            initRecyclerView()
            mapboxMap.addOnMapClickListener(this)

        }

    }


    //==========RecyclerVIew===========

    private fun initFeatureCollection() {
        featureCollection = FeatureCollection.fromFeatures(arrayOf<Feature>())
        val featureList: MutableList<Feature> = ArrayList()
        if (featureCollection != null) {
            for (latLng in coordinates) {
                featureList.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            latLng.longitude,
                            latLng.latitude
                        )
                    )
                )
            }
            featureCollection = FeatureCollection.fromFeatures(featureList)
        }
    }

    private fun initRecyclerView() {
        val locationAdapter = TrackingFragment.LocationRecyclerViewAdapter(
            createRecyclerViewLocations(),
            mapboxMap
        )
        recyclerView.setLayoutManager(
            LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, true
            )
        )
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setAdapter(locationAdapter)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    private fun initMarkerIcons(@NonNull loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )
        loadedMapStyle.addSource(GeoJsonSource(SOURCE_ID, featureCollection))
        loadedMapStyle.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                iconImage(SYMBOL_ICON_ID),
                iconAllowOverlap(true),
                iconSize(0.2f)
            )
        )
    }

    private fun createRecyclerViewLocations(): List<SingleRecyclerViewLocation> {
        var locationList: ArrayList<SingleRecyclerViewLocation> = ArrayList()
        for (x in coordinates.indices) {
            val singleLocation = SingleRecyclerViewLocation()
            singleLocation.setName(namalayout[x])
            singleLocation.setGambar(gambarrecycler[x])
            singleLocation.setLocationCoordinates(coordinates[x])
            locationList.add(singleLocation)
        }
        return locationList
    }


    //==============================

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val preferences = context.getSharedPreferences("pref", 0)
    }

    override fun onStart() {
        super.onStart()
        mapbox.onStart()
    }


    override fun onResume() {
        super.onResume()
        mapbox.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapbox.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapbox.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapbox.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapbox.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()



        if (locationEngine != null) {
            locationEngine!!.removeLocationUpdates(callback)
        }

        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this)
        }
        mapbox.onDestroy()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        toast(R.string.user_location_permission_explanation)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (mapboxMap.style != null) {
                enableLocationComponent(mapboxMap.style!!)
            }
        } else {
            toast(R.string.user_location_permission_not_granted)
            activity?.finish()
        }
    }

    var alfan = false

    override fun onMapClick(point: LatLng): Boolean {
        var destinationPoint = Point.fromLngLat(point.longitude, point.latitude)
        origin = Point.fromLngLat(
            locationComponent.lastKnownLocation!!.longitude,
            locationComponent.lastKnownLocation!!.latitude
        )
        toast("hasilnya : ${locationComponent.lastKnownLocation!!.longitude}")
        //meload marker pada saat di click
        mapboxMap.style?.getSourceAs<GeoJsonSource>("destination-source-id")
            ?.setGeoJson(Feature.fromGeometry(destinationPoint))
        //===============================
        getRoute(origin, destinationPoint)
        alfan = true
        return true
    }


    private class LoadGeoJson internal constructor(activity: TrackingFragment) :
        AsyncTask<Void?, Void?, FeatureCollection?>() {
        private val weakReference: WeakReference<TrackingFragment> = WeakReference(
            activity
        )


        override fun onPostExecute(featureCollection: FeatureCollection?) {
            super.onPostExecute(featureCollection)
            val activity: TrackingFragment? =
                weakReference.get()
            if (activity != null && featureCollection != null) {
                activity.drawLines(featureCollection)
            }
        }

        companion object {
            fun convertStreamToString(`is`: InputStream?): String {
                val scanner = Scanner(`is`).useDelimiter("\\A")
                return if (scanner.hasNext()) scanner.next() else ""
            }
        }


        override fun doInBackground(vararg params: Void?): FeatureCollection? {
            try {
                val activity: TrackingFragment? =
                    weakReference.get()
                if (activity != null) {

                    val inputStream: InputStream? =
                        activity.context?.assets?.open("example.geojson")
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


    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    internal class MainActivityLocationCallback internal constructor(activity: TrackingFragment) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<TrackingFragment> = WeakReference(activity)
        lateinit var sessionManager: SessionManager

        @SuppressLint("SetTextI18n", "InflateParams")
        override fun onSuccess(result: LocationEngineResult?) {
            val activity: TrackingFragment? = activityWeakReference.get()


            if (activity != null) {
                val location: Location? = result?.lastLocation
                if (location == null) {
                    return

                }


                val sessionManager = SessionManager(activity.context)

                sessionManager.setlongitude(result.lastLocation!!.longitude.toString())
                sessionManager.setLatidude(result.lastLocation!!.latitude.toString())

                if (result.lastLocation != null) {
                    activity.mapboxMap.locationComponent.forceLocationUpdate(result.lastLocation)
                }

                activity.lastlatitude = "${result.lastLocation!!.longitude}"
            }
        }

        @SuppressLint("LogNotTimber")
        override fun onFailure(exception: java.lang.Exception) {
            Log.d("LocationChange", exception.localizedMessage)
            val activity: TrackingFragment? = activityWeakReference.get()
            if (activity != null) {
                activity.toast(exception.localizedMessage)

            }

        }
    }

    //fungsi gambar rute
    private fun getRoute(origin: Point, destination: Point) {
        var client = MapboxDirections.builder()


        NavigationRoute.builder(context?.applicationContext)
            .accessToken(getString(R.string.access_token))
            .origin(origin)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .destination(destination)
            .build()
            .getRoute(object : Callback<DirectionsResponse> {
                @SuppressLint("LogNotTimber")
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                    Log.e(TAG, "Error" + t.message)
                }

                @SuppressLint("LogNotTimber")
                override fun onResponse(
                    @NonNull
                    call: Call<DirectionsResponse>, @NonNull
                    response: Response<DirectionsResponse>
                ) {
//                    Log.d(TAG, " Response Codde :" + response.code())
                    if (response.body() == null) {
//                        Log.e(TAG, "Tidak ada Route, pastikan menggunakan token yang tepat")
                        return
                    } else if (response.body()!!.routes().size < 1) {
//                        Log.e(TAG, "Tidak ada Route")
                        return
                    }
                    //menggabar route di map
                    currentRoute = response.body()!!.routes().get(0)
                    if (navigationMapRoute != null) {
                        navigationMapRoute!!.removeRoute()
                    } else {
                        navigationMapRoute =
                            NavigationMapRoute(null, mapbox, mapboxMap, R.style.NavigationMapRoute)
                    }
                    navigationMapRoute!!.addRoute(currentRoute)
                }
            })


    }

    @SuppressLint("MissingPermission", "WrongConstant")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context?.applicationContext)) {
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                context?.applicationContext!!,
                loadedMapStyle
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            //locationComponent.lastKnownLocation!!.longitude
            initLocationEngine()


        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(context?.applicationContext as Activity?)
        }

    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(context?.applicationContext!!)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()

        locationEngine!!.requestLocationUpdates(request, callback, context!!.mainLooper)
        locationEngine!!.getLastLocation(callback)
/*
        locationEngine.removeLocationUpdates(request,callback,mainLooper)
*/
/*
        locationEngine.getLastLocation(callback)
*/
    }

    //penanda marker
    private fun addDestinationIconSymbolLayer(@NonNull loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            "destination-icon-id",
            BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default)
        )
        val geoJsonSource = GeoJsonSource("destination-source-id")
        loadedMapStyle.addSource(geoJsonSource)
        val destinationSymbolLayer =
            SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            iconImage("destination-icon-id"),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconSize(1f)
        )
        loadedMapStyle.addLayer(destinationSymbolLayer)
    }


    internal class LocationRecyclerViewAdapter(
        locationList: List<SingleRecyclerViewLocation>,
        mapBoxMap: MapboxMap
    ) :
        RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder>() {
        private val locationList: List<SingleRecyclerViewLocation>
        private val map: MapboxMap
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_on_top_of_map_card, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val singleRecyclerViewLocation: SingleRecyclerViewLocation = locationList[position]
            holder.gambar.setImageResource(singleRecyclerViewLocation.getGambar())
            holder.name.setText(singleRecyclerViewLocation.getName())

            holder.setClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int) {
                    val selectedLocationLatLng: LatLng =
                        locationList[position].getLocationCoordinates()!!
                    val newCameraPosition: CameraPosition = CameraPosition.Builder()
                        .target(selectedLocationLatLng)
                        .build()


                    map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                }
            })
        }

        override fun getItemCount(): Int {
            return locationList.size
        }

        internal class MyViewHolder(view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            var name: TextView
            var singleCard: CardView
            var gambar: ImageView
            private var clickListener: ItemClickListener? = null
            fun setClickListener(itemClickListener: ItemClickListener?) {
                clickListener = itemClickListener
            }

            override fun onClick(view: View?) {
                clickListener!!.onClick(view, layoutPosition)
            }

            init {
                name = view.findViewById(R.id.location_title_tv)
                singleCard = view.findViewById(R.id.single_location_cardview)
                gambar = view.findViewById(R.id.gambarrecycler)
                singleCard.setOnClickListener(this)
            }
        }

        init {
            this.locationList = locationList
            map = mapBoxMap
        }

        interface ItemClickListener {
            fun onClick(view: View?, position: Int)
        }
    }

}