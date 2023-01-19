package com.example.crm.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.crm.database.DraftDao
import com.example.crm.database.ImageDao
import com.example.crm.database.UserLocationDao
import com.example.crm.model.ProjectData
import com.example.crm.model.ImageDetails
import com.example.crm.model.UserLocation

class CrmRepo(
    private val draftDao: DraftDao,
    private val imageDao: ImageDao,
    private val locationDao: UserLocationDao
    ) {

    fun getAllProjectData(): LiveData<List<ProjectData>> {
        return draftDao.getAllData()
    }

    suspend fun deleteImageDetailsForProjectId(projectId: String) {
        imageDao.deleteImageDetailsByProjectId(projectId)
    }

    suspend fun deleteImageDetails(imageDetails: ImageDetails) {
        imageDao.deleteImageDetails(imageDetails)
    }

    suspend fun uploadImages(images: ImageDetails) {
        imageDao.uploadImage(images)
    }

    suspend fun getAllImages(): List<ImageDetails> {
        return imageDao.getAllImages()
    }

    suspend fun getAllImagesByProjectId(projectId: String): List<ImageDetails> {
        val images = imageDao.getAllImagesForProjectId(projectId)
        Log.d("BugInfo", "images size: ${images.size}")
        return images
    }

    suspend fun getAllImageDetailsByProjectId(projectId: String): List<ImageDetails> {
        return imageDao.getAllImageDetailsForProjectId(projectId)
    }

    suspend fun resetAllImageData() {
        imageDao.resetAll()
    }

    suspend fun getAllResetAbleData(): List<ProjectData> {
        return draftDao.getResetAbleData()
    }

    suspend fun deleteDraftListModel(projectData: ProjectData) {
        draftDao.deleteListModel(projectData)
    }

    suspend fun getAllDataByProjectId(projectId: String): LiveData<List<ProjectData>>? {
        return draftDao.getAllDataByProjectId(projectId)
    }

    suspend fun insertProjectData(projectData: ProjectData) {
        draftDao.insertDraft(projectData)
    }

    suspend fun updateDraftByProjectId(
        isPending: Boolean,
        projectId: String,
        townshipName: String? = null,
        constructionSlab: String? = null,
        compDate: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        draftDao.updateData(isPending, projectId, townshipName, constructionSlab, compDate, latitude, longitude)
    }

    suspend fun insertUserLocation(userLocation: UserLocation) {
        locationDao.insertLocation(userLocation)
    }

    suspend fun deleteUserLocation(userLocation: UserLocation) {
        locationDao.deleteLocation(userLocation)
    }

    suspend fun getAllLocationData(): List<UserLocation> {
        return locationDao.getAllLocation()
    }

    suspend fun getUserLocationById(id: String): UserLocation {
        return locationDao.getUserLocationById(id)
    }
}