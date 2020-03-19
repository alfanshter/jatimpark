package com.alfanshter.jatimpark

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import java.text.DateFormat
import java.util.*

object Commond {

    val KEY_REQUEST_LOCATION_UPDATES = "requesting_location_updates"
    fun getLocationText(location: Location?):String{
        return if (location ==null)
            "Unknow location"
        else
            ""+location.latitude + "/" + location.longitude
    }

    fun getLocationTitle(context: Context): String? {
        return String.format("Location Update :  ${            DateFormat.getDateInstance().format(Date())}")
    }

    fun setRequestingLocationUpdates(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_REQUEST_LOCATION_UPDATES,value)
            .apply()
    }

    fun requestingLocationUpdates(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_REQUEST_LOCATION_UPDATES,false)

    }
}