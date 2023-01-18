package com.example.crm

import android.app.Application
import android.database.CursorWindow
import android.util.Log
import androidx.work.*
import com.example.crm.pending.PendingViewModel
import com.example.crm.permission.CheckPermission
import com.example.crm.permission.CheckPermission.Companion.requestLocationPermissions
//import com.example.crm.worker.LocationWorker
import java.lang.reflect.Field
import java.time.Duration


class MainApplication: Application() {

    private var locationWorker: WorkManager? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.setAccessible(true)
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
                e.printStackTrace()
        }

//        setupWorker()
    }

//    private fun setupWorker() {
////        if (!CheckPermission.checkLocationPermissions(this)) {
////            this.requestLocationPermissions()
////        }
//        Log.d("BugInfo", "Set up worker called.")
//
//        val locationRequest = PeriodicWorkRequest.Builder(
//            LocationWorker::class.java,
//            /*Duration.ofMinutes(2))*/Duration.ofSeconds(1))
//            .setConstraints(
//                Constraints.Builder().setRequiredNetworkType(
//                    NetworkType.CONNECTED
//                ).build()
//            )
//            .build()
//        locationWorker = WorkManager.getInstance(this)
////        locationWorker.enqueueUniquePeriodicWork(
////            "location",
////            ExistingPeriodicWorkPolicy.REPLACE,
////            locationRequest
////        )
//        locationWorker!!.enqueue(locationRequest)
//    }

}