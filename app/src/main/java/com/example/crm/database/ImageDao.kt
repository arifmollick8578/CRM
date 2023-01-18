package com.example.crm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.crm.model.ImageDetails

@Dao
interface ImageDao {
    @Insert
    suspend fun uploadImage(image: ImageDetails)

    @Query("SELECT * from image_table")
    suspend fun getAllImages(): List<ImageDetails>

    @Query("SELECT * from image_table WHERE projectId = :projectId")
    suspend fun getAllImagesForProjectId(projectId: String): List<ImageDetails>

    @Query("SELECT * from image_table WHERE projectId = :projectId")
    suspend fun getAllImageDetailsForProjectId(projectId: String): List<ImageDetails>

    @Query("DELETE from image_table WHERE projectId = :projectId")
    suspend fun deleteImageDetailsByProjectId(projectId: String)

    @Delete
    suspend fun deleteImageDetails(imageDetails: ImageDetails)

    @Query("DELETE from image_table")
    suspend fun resetAll()
}