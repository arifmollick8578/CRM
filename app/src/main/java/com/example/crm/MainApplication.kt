package com.example.crm

import android.app.Application
import android.database.CursorWindow
import java.lang.reflect.Field

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}