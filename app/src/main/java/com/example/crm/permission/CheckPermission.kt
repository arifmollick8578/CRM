package com.example.crm.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.crm.FormActivity

class CheckPermission {

    companion object {
        fun checkLocationPermissions(context: Context): Boolean {
            val permissionState = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return permissionState == PackageManager.PERMISSION_GRANTED
        }

        fun Activity.requestLocationPermissions() {
            val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (shouldProvideRationale) {
                Log.i("TAG", "Displaying permission rationale to provide additional context.")
            }
            else {
                Log.i("TAG", "Requesting permission")
                startLocationPermissionRequest()
            }
        }

        private fun Activity.startLocationPermissionRequest() {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }

        fun Activity.showDialogToAccessGps() {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes",  DialogInterface.OnClickListener { dialogInterface, i ->
                    startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
                .setNegativeButton("No",  DialogInterface.OnClickListener { dialogInterface, i ->
                    Log.d("BugInfo", "GPS permission is denied.")
                    dialogInterface.cancel()
                })
            val alert: AlertDialog = builder.create();
            alert.show()
        }

        const val LOCATION_PERMISSION_CODE = 2
    }
}