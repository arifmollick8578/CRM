package retrofit

import com.example.crm.LoginRequest
import com.example.crm.LoginResponse
import com.example.crm.model.*
import com.example.crm.pending.PendingRequest
import com.example.crm.pending.PendingResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiDetails {
    @POST("datamap.svc/rest/AuthenticateUser")
    fun postLogin(@Body loginRequest : LoginRequest) : Call<LoginResponse>

    @POST("datamap.svc/rest/GetProjectForSurvey")
    fun getpendingList(@Body pendingRequest : PendingRequest) : Call<PendingResponse>

    @POST("datamap.svc/rest/UpdateSurveyImageDetail")
    fun updateImageDetails(@Body imageDetails: ImageRequestDetails): Call<UpdateSurveyImageDetailResult>

    @POST("datamap.svc/rest/")
    fun updateUserLocationData(@Body location: UserLocation): Call<UserLocation>

    // first version with image details directly
//    @POST("datamap.svc/rest/UpdateSurveyImageDetail")
//    fun updateImageDetails(imageDetails: List<ImageDetails>): Call<List<ImageDetails>>

//    @Multipart
//    @POST("datamap.svc/rest/UpdateSurveyImageDetail")
//    fun updateImageDetails(@Part imageDetails: ImageRequestDetails, @Part image: MultipartBody.Part): Call<ImageDetails>
//
//    @POST("datamap.svc/rest/UpdateSurveyImageDetail")
//    fun updatePendingListDetails(@Body listModel: DraftListModel): Call<DraftListModel>
}