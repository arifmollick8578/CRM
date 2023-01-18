package com.example.crm.pending

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.crm.database.CrmDatabase
import com.example.crm.model.DraftListModel
import com.example.crm.model.ImageDetails
import com.example.crm.model.UserLocation
import com.example.crm.repository.CrmRepo
import com.example.crm.utility.CurrentDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PendingViewModel(application: Application) : AndroidViewModel(application) {
    private var crmRepo: CrmRepo
    private var peningListMutableLiveData = MutableLiveData<PendingResponse>()
    val getAllData: LiveData<List<DraftListModel>>

    private var _byteArraysForProjectId = MutableLiveData<List<ImageDetails>>()
    val byteArrayForProjectId: LiveData<List<ImageDetails>> = _byteArraysForProjectId

    private var _allLocationData = MutableLiveData<List<UserLocation>>()
    val allLocationData: LiveData<List<UserLocation>> = _allLocationData

    private var _currentLocation = MutableLiveData<Pair<Double, Double>>()
    val currentLocation: LiveData<Pair<Double, Double>> = _currentLocation

    private var _currentLocation1: UserLocation? = null
    val currentLocation1: UserLocation = _currentLocation1 ?: UserLocation(CurrentDateTime.currentTime, "", "", 0.0, 0.0)

    private var _imageDetails = MutableLiveData<List<ImageDetails>>()
    val postImageDetails: LiveData<List<ImageDetails>> = _imageDetails
    private var _postingStatus = MutableLiveData<Pair<Int, Int>>()
    val postingStatus: LiveData<Pair<Int, Int>> = _postingStatus

    private val getAllDataListByProjectIdInternal: MutableLiveData<List<DraftListModel>> =
        MutableLiveData()
    val getAllDataByProjectIdMutable: LiveData<List<DraftListModel>> = getAllDataListByProjectIdInternal

    init {
        val draftDao = CrmDatabase.getDatabase(application).userDao()
        val imageDao = CrmDatabase.getDatabase(application).imageDao()
        val locationDao = CrmDatabase.getDatabase(application).locationDao()
        crmRepo = CrmRepo(draftDao, imageDao, locationDao)
        getAllData = crmRepo.getAllData

        Log.d("BugInfo", "ViewModelInit called.")
        getAllImages()
    }

    fun getAllImages(): List<ImageDetails>? {
        Log.d("BugInfo", "getAllImages called.")
        var images: List<ImageDetails>? = null
        viewModelScope.launch(Dispatchers.IO) {
            images = crmRepo.getAllImages()
            Log.d("BugInfo", "ImageDetails size: ${images?.size}")
            images?.forEach {
                Log.d("BugInfo", "projectId: ${it.projectId}")
            }
        }
        return images
    }

    fun deleteImageDetailsByProjectId(projectId: String) {
        viewModelScope.launch {
        crmRepo.deleteImageDetailsForProjectId(projectId)
        }
    }

    fun deleteImageDetails(imageDetails: ImageDetails) {
        viewModelScope.launch {
            crmRepo.deleteImageDetails(imageDetails)
        }
    }

    fun uploadImage(images: ImageDetails) {
        viewModelScope.launch {
            crmRepo.uploadImages(images)
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
                Log.d("BugInfo", "imageDetails size: ${imageDetails.size}")
                _postingStatus.postValue(
                    Pair(0, _imageDetails.value?.size!!)
                )
            }
        }
    }

    fun resetApplication() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("BugInfo", "Image reset started.")
            crmRepo.resetAllImageData()
            Log.d("BugInfo", "Image reset finished.")
        }
        viewModelScope.launch(Dispatchers.IO) {
            val alldata = crmRepo.getAllResetAbleData()
            alldata.forEach {
                Log.d("BugInfo", "deleting: projectId: ${it.ProjectId}.. of ${alldata.size}")
                crmRepo.updateDraftByProjectId(
                    isPending = true,
                    projectId = it.ProjectId,
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
        Log.d("BugInfo", "user: ${userLocation.latitude}, time: ${userLocation.trackTime}")
        updateAllLocationDataToLiveData()
        _currentLocation.postValue(Pair(userLocation.latitude!!, userLocation.longitude!!))

        Log.d("BugInfo", "_val: ${_currentLocation.value}.. val: ${currentLocation.value}")
        _currentLocation1 = userLocation
    }

//    fun deleteListModel(draftListModel: DraftListModel) {
//        viewModelScope.launch {
//            crmRepo.deleteDraftListModel(draftListModel)
//        }
//    }

    fun getAllDataByProjectList(projectId:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = crmRepo.getAllDataByProjectid(projectId)
            getAllDataListByProjectIdInternal.postValue(result?.value)
        }
    }

    fun insertDraft(draftListModel: DraftListModel) {
        viewModelScope.launch(Dispatchers.IO) {
            crmRepo.insertDraft(draftListModel)
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
    ){
        viewModelScope.launch(Dispatchers.IO){
            crmRepo.updateDraftByProjectId(isPending,projectId, townshipName, constructionSlab, compDate,
            latitude, longitude)
        }
    }

    fun getPendingList(pendingRequest: PendingRequest) {
        RetrofitInstance.api.getpendingList(pendingRequest)
            .enqueue(object : Callback<PendingResponse> {
                override fun onResponse(
                    call: Call<PendingResponse>,
                    response: Response<PendingResponse>
                ) {
                    if (response.body() != null) {
                        peningListMutableLiveData.value = response.body()
                        Log.d("TAG", response.body().toString())
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<PendingResponse>, t: Throwable) {
                    Log.d("TAG", t.message.toString())
                }
            })
    }

    fun observePendingResponseData(): LiveData<PendingResponse> {
        return peningListMutableLiveData
    }

    fun startPostingLocationData() {
        if (allLocationData.value?.isEmpty() == true || allLocationData.value?.size == 0) {
            Log.d("BugInfo", "Pending location size 0,")
            return
        }
        Log.d("BugInfo", "Start posting. size: ${allLocationData.value?.size}")
        allLocationData.value?.forEach {
            Log.d("BugInfo", "Uploading time: ${it.trackTime}, lat: ${it.latitude}.. locationSize: ${allLocationData.value?.size}")
            RetrofitInstance.api.updateUserLocationData(it)
            deleteUserLocation(it)
        }

        Log.d("BugInfo", "Work finished.")
        _allLocationData.postValue(emptyList())
    }

}

