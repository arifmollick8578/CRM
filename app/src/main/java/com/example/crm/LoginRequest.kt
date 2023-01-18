package com.example.crm

data class LoginRequest(
    val credential: CredentialRequest,
    val IMEINo: String?,
    val userId: String,
    val Password: String
)
