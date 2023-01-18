package com.example.crm.pending

import com.example.crm.CredentialRequest

data class PendingRequest(
    val credential: CredentialRequest,
    val imeiNo: String?,
    val userId: String?,
    val lastSyncTime: String?,
    val dataGroup: String?
)