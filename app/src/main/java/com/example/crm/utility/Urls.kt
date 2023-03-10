package com.example.crm.utility

import android.content.Context
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager

object Urls {

    fun getWebViewMapLink(context: Context, latitude: Double, longitude: Double): String {
        val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(context) }
        return "http://mriext.propequity.in/View/SurveyorMap.aspx?lat=$latitude&long=$longitude&uid=${preferenceHelper.getUserId()}&output=embed"
    }

    fun getChangePasswordUrl(): String {
        return "http://newlaunch.propequity.in/surveysystem"
    }

    fun getMapRedirectUrl(latitude: Double, longitude: Double, title: String): String {
        return "http://maps.google.com/maps?q=loc:$latitude,$longitude ($title)"
    }
}