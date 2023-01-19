package com.example.crm

data class LoginRequest(
    val credential: CredentialRequest,
    val imeiNo: String?,
    val userId: String,
    val password: String
)
