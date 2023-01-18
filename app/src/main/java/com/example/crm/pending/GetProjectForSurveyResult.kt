package com.example.crm.pending

data class GetProjectForSurveyResult(
    val Address: String?,
    val AssignDate: String?,
    val AssignDateText: String?,
    val Builder: String?,
    val City: String?,
    val Colony: String?,
    val CompDate: String?,
    val Constructionslab: String?,
    val Lat: Double?,
    val LaunchDate: String?,
    val LaunchSqft: String?,
    val LaunchUnit: String?,
    val Long: Double?,
    val ProjectId: String,
    val ProjectName: String?,
    val ProjectSubType: String?,
    val ProjectType: String,
    val Region: String?,
    val TownshipName: String?,
    val TransId: Int?
)
