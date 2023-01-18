package com.example.crm.preferences

interface IPreferenceHelper {
    fun setUserId(userId: String)
    fun getUserId(): String
    fun setImeiNo(imeiNo : String)
    fun getImeiNo() : String
    fun clearPrefs()
    fun setIsLogin(isLogin : Boolean)
    fun getIsLogin() : Boolean
    fun resetLogInData()
}
