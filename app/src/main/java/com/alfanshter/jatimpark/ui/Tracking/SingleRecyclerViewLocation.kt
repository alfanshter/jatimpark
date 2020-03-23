package com.alfanshter.jatimpark.ui.Tracking

import android.widget.ImageView
import com.mapbox.mapboxsdk.geometry.LatLng

class SingleRecyclerViewLocation {
    private var name: String? = null
    private var bedInfo: String? = null
    private var locationCoordinates: LatLng? = null
    private var gambar : Int = 0

    fun getGambar(): Int {
        return gambar
    }

    fun setGambar(gambar: Int) {
        this.gambar = gambar
    }




    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getBedInfo(): String? {
        return bedInfo
    }

    fun setBedInfo(bedInfo: String?) {
        this.bedInfo = bedInfo
    }

    fun getLocationCoordinates(): LatLng? {
        return locationCoordinates
    }

    fun setLocationCoordinates(locationCoordinates: LatLng?) {
        this.locationCoordinates = locationCoordinates
    }

}