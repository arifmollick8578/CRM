package com.example.crm.pending

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.example.crm.database.CrmDatabase
import com.example.crm.model.ProjectData
import com.example.crm.model.ImageDetails
import com.example.crm.model.UserLocation
import com.example.crm.repository.CrmRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PendingViewModel(application: Application): AndroidViewModel(application) {
    private var crmRepo: CrmRepo
    private var _pendingListLiveData = MutableLiveData<ProjectListResponse>()
    val pendingListLiveData: LiveData<ProjectListResponse> = _pendingListLiveData

    val projectDataList: LiveData<List<ProjectData>>

    private var _byteArraysForProjectId = MutableLiveData<List<ImageDetails>>()
    val byteArrayForProjectId: LiveData<List<ImageDetails>> = _byteArraysForProjectId

    private var _allLocationData = MutableLiveData<List<UserLocation>>()
    val allLocationData: LiveData<List<UserLocation>> = _allLocationData

    private var _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> = _currentLocation

    private var _imageDetails = MutableLiveData<List<ImageDetails>>()
    val postImageDetails: LiveData<List<ImageDetails>> = _imageDetails
    private var _postingStatus = MutableLiveData<Pair<Int, Int>>()
    val postingStatus: LiveData<Pair<Int, Int>> = _postingStatus

    private val _allProjectDataListByProjectId: MutableLiveData<List<ProjectData>> =
        MutableLiveData()
    val allProjectDataListByProjectId: LiveData<List<ProjectData>> = _allProjectDataListByProjectId

    init {
        val draftDao = CrmDatabase.getDatabase(application).userDao()
        val imageDao = CrmDatabase.getDatabase(application).imageDao()
        val locationDao = CrmDatabase.getDatabase(application).locationDao()
        crmRepo = CrmRepo(draftDao, imageDao, locationDao)
        projectDataList = crmRepo.getAllProjectData()
        getAllImages()
    }

    private fun getAllImages(): List<ImageDetails>? {
        var images: List<ImageDetails>? = null
        viewModelScope.launch(Dispatchers.IO) {
            images = crmRepo.getAllImages()
        }
        return images
    }

    fun deleteImageDetails(imageDetails: ImageDetails) {
        viewModelScope.launch {
            crmRepo.deleteImageDetails(imageDetails)
        }
    }

    fun uploadImage(image: ImageDetails) {
        viewModelScope.launch {
            crmRepo.uploadImages(image)
        }
    }

    fun updatesAllImagesByProjectId(projectId: String) {
        viewModelScope.launch {
            val images = crmRepo.getAllImagesByProjectId(projectId)
           _byteArraysForProjectId.postValue(images)
        }
    }

    fun postPendingDetails(projectId: String) {
        viewModelScope.launch {
            val imageDetails = crmRepo.getAllImageDetailsByProjectId(projectId)
            _imageDetails.postValue(imageDetails)
            if (_imageDetails.value?.isNotEmpty() == true) {
                Log.d("BugInfo", "Pending images are uploading with size: ${imageDetails.size}")
                _postingStatus.postValue(
                    Pair(0, _imageDetails.value?.size!!)
                )
            }
        }
    }

    fun resetApplication() {
        viewModelScope.launch(Dispatchers.IO) {
            crmRepo.resetAllImageData()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val allData = crmRepo.getAllResetAbleData()
            allData.forEach {
                Log.d("BugInfo", "deleting: projectId: ${it.projectId}.. of ${allData.size}")
                // setting default value and resetting project data
                crmRepo.updateDraftByProjectId(
                    isPending = true,
                    projectId = it.projectId,
                    townshipName = null,
                    constructionSlab = null,
                    compDate = null,
                    latitude = null,
                    longitude = null,
                )
            }
        }
    }

    fun updatePosting() {
        val existingValue = postingStatus.value?.first
        if (existingValue != null) {
            Log.d("BugInfo", "updating image size: $existingValue")
            if (existingValue == postingStatus.value?.second) {
                _postingStatus.postValue(
                    Pair(0, 0)
                )
                return
            }
            _postingStatus.postValue(
                Pair(existingValue + 1, postingStatus.value?.second!!)
            )
        }
    }

    private fun updateAllLocationDataToLiveData() {
        viewModelScope.launch {
            _allLocationData.postValue(crmRepo.getAllLocationData())
        }
    }

    fun getUserLocationById(id: String): UserLocation? {
        var userLocation: UserLocation? = null
        viewModelScope.launch {
            userLocation = crmRepo.getUserLocationById(id)
        }
        return userLocation
    }

    fun deleteUserLocation(userLocation: UserLocation) {
        viewModelScope.launch {
            crmRepo.deleteUserLocation(userLocation)
        }
    }

    fun insertUserLocation(userLocation: UserLocation) {
        viewModelScope.launch {
            crmRepo.insertUserLocation(userLocation)
        }
        updateAllLocationDataToLiveData()
    }

    fun updateCurrentLocation(location: Location) {
        _currentLocation.postValue(location)
    }

    fun getAllDataByProjectList(projectId:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = crmRepo.getAllDataByProjectId(projectId)
            _allProjectDataListByProjectId.postValue(result?.value)
        }
    }

    fun insertProjectData(projectData: ProjectData) {
        viewModelScope.launch(Dispatchers.IO) {
            crmRepo.insertProjectData(projectData)
        }
    }

    fun updateDraft(
        isPending : Boolean,
        projectId : String,
        townshipName: String? = null,
        constructionSlab: String? = null,
        compDate: String? = null,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch(Dispatchers.IO){
            crmRepo.updateDraftByProjectId(isPending,projectId, townshipName, constructionSlab, compDate,
            latitude, longitude)
        }
    }

    fun getPendingList(pendingRequest: PendingRequest) {
        RetrofitInstance.api.getpendingList(pendingRequest)
            .enqueue(object : Callback<ProjectListResponse> {
                override fun onResponse(
                    call: Call<ProjectListResponse>,
                    response: Response<ProjectListResponse>
                ) {
                    if (response.body() != null) {
                        _pendingListLiveData.value = response.body()
                        Log.d("TAG", response.body().toString())
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<ProjectListResponse>, t: Throwable) {
                    Log.d("TAG", t.message.toString())
                }
            })
    }

    fun startPostingLocationData() {
        if (allLocationData.value?.isEmpty() == true || allLocationData.value?.size == 0) {
            Log.d("TAG", "Pending location size 0.")
            return
        }
        Log.d("TAG", "Start posting. size: ${allLocationData.value?.size}")
        allLocationData.value?.forEach {
            Log.d("TAG", "Uploading location with time: ${it.trackTime}, latitude: ${it.latitude} and longitude ${it.longitude}")
            RetrofitInstance.api.updateUserLocationData(it)
            deleteUserLocation(it)
        }

        Log.d("BugInfo", "Uploading location data finished.")
        _allLocationData.postValue(emptyList())
    }

}

