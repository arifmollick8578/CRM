package com.example.crm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.crm.model.UserLocation

@Dao
interface UserLocationDao {

    @Insert
    suspend fun insertLocation(userLocation: UserLocation)

    @Delete
    suspend fun deleteLocation(userLocation: UserLocation)

    @Query("SELECT * from location_table")
    suspend fun getAllLocation(): List<UserLocation>

    @Query("SELECT * from location_table WHERE trackTime = :time")
    suspend fun getUserLocationById(time: String): UserLocation
}