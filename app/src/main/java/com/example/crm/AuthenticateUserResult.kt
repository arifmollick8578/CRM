package com.example.crm

data class AuthenticateUserResult(
    val attemptCount: Int,
    val dMAppForceInstall: String?,
    val dMAppVersion: String?,
    val emailId: String?,
    val iMEINo: String?,
    val dMAppPath: String?,
    val isAllowOutside: Boolean,
    val isAuthenticated: Boolean,
    val isGlobalUser: Boolean,
    val level: Int?,
    val mRIGUID: String?,
    val mRIIsAllowOutside: Boolean,
    val mRINetworkId: String?,
    val networkId: String?,
    val password: String?,
    val reportingTo: String?,
    val userGUID: String?,
    val userId: String?,
    val userName: String?
)
