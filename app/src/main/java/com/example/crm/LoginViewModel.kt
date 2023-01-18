package com.example.crm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private var loginLiveDashBoard = MutableLiveData<LoginResponse>()

    private var _shouldLogOut = MutableLiveData<Boolean>(false)
    val shouldLogOut: LiveData<Boolean> = _shouldLogOut
    fun postLogin(loginRequest: LoginRequest) {
        RetrofitInstance.api.postLogin(loginRequest)
            .enqueue(object  : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.body()!=null){
                        loginLiveDashBoard.value = response.body()
                        Log.d("TAG",response.body().toString())
                    }
                    else{
                        return
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.d("TAG",t.message.toString())
                }
            })
    }
    fun observeLoginResponseData() : LiveData<LoginResponse> {
        return loginLiveDashBoard
    }

    fun logOutAccount() {
        _shouldLogOut.postValue(true)
    }
}