package com.example.crm.utility

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CurrentDateTime {

    val currentTime: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString()

    val currentDate: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}