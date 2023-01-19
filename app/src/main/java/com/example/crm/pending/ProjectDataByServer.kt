package com.example.crm.pending

data class ProjectDataByServer(
    val address: String?,
    val assignDate: String?,
    val assignDateText: String?,
    val builder: String?,
    val city: String?,
    val colony: String?,
    val compDate: String?,
    val constructionSlab: String?,
    val latitude: Double?,
    val launchDate: String?,
    val launchSqFt: String?,
    val launchUnit: String?,
    val longitude: Double?,
    val projectId: String,
    val projectName: String?,
    val projectSubType: String?,
    val projectType: String,
    val region: String?,
    val townshipName: String?,
    val transactionId: Int?
)
