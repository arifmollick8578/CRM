package com.example.crm.utility

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager

fun Context.getDeviceId(): String {
    val deviceId: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    } else {
        val mTelephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (mTelephony.deviceId != null) {
            mTelephony.deviceId
        } else {
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
    }
    return deviceId
}