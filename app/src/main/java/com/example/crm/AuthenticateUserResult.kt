package com.example.crm

data class AuthenticateUserResult(
    val AttemptCount: Int,
    val DMAppForceInstall: String?,
    val DMAppVersion: String?,
    val EmailId: String?,
    val IMEINo: String?,
    val DMAppPath: String?,
    val IsAllowOutside: Boolean,
    val IsAuthenticated: Boolean,
    val IsGlobalUser: Boolean,
    val Level: Int?,
    val MRIGUID: String?,
    val MRIIsAllowOutside: Boolean,
    val MRINetworkId: String?,
    val NetworkId: String?,
    val Password: String?,
    val ReportingTo: String?,
    val UserGUID: String?,
    val UserId: String?,
    val UserName: String?
)
