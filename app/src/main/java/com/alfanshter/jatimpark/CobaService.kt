package com.alfanshter.jatimpark

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_coba_service.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.jar.Manifest

class CobaService : AppCompatActivity(),SharedPreferences.OnSharedPreferenceChangeListener {

    private var mService:ServiceLocation?=null
    private var mBound = false
    private val mServiceConnection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
                mService = null
                mBound = false
            }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as ServiceLocation.LocalBinder
                    mService = binder.service
            mBound = true
             }

    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onBackgroundLocationRetrieve(event:BackgroundLocation)
    {
        if (event.location!=null)
            Toast.makeText(this,Commond.getLocationText(event.location),Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coba_service)
        Dexter.withActivity(this)
            .withPermissions(Arrays.asList(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                android.Manifest.permission.FOREGROUND_SERVICE))
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    request_location_updates_button.setOnClickListener {
                        mService!!.requestLocationUpdates()
                    }
                    remove_location_updates_button!!.setOnClickListener {
                        mService!!.removeLocationUpdates()
                    }
                    setButtonState(Commond.requestingLocationUpdates(this@CobaService))
                    bindService(Intent(this@CobaService,ServiceLocation::class.java),mServiceConnection,Context.BIND_AUTO_CREATE)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                }

            }).check()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().register(this)
        super.onStop()

    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(Commond.KEY_REQUEST_LOCATION_UPDATES))
            setButtonState(sharedPreferences!!.getBoolean(Commond.KEY_REQUEST_LOCATION_UPDATES,false))
    }

    private fun setButtonState(boolean: Boolean) {
        if (boolean)
        {
            remove_location_updates_button.isEnabled = true
            request_location_updates_button.isEnabled = false
        }
        else{
            remove_location_updates_button.isEnabled = false
            request_location_updates_button.isEnabled = true
        }
    }
}
