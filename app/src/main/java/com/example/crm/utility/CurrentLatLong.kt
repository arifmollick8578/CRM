package com.example.crm.utility

import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

// Working not using WIP
class CurrentLatLong(activity: Activity): LocationListener {
    private val locationManager: LocationManager
    private var currentLocation: Location? = null
//    private lateinit var gpsTracker: GPSTracker
    init {
        locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
    }
}