package com.example.crm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class UserLocation(
    @PrimaryKey
    val trackTime: String,
    val appId: String?,
    val userId: String?,
    val latitude: Double?,
    val longitude: Double?,
)
