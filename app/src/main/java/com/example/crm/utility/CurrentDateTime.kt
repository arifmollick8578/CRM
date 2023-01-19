package com.example.crm.utility

import java.text.SimpleDateFormat
import java.util.*

object CurrentDateTime {

    val currentTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    val currentDate: String
        get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
}