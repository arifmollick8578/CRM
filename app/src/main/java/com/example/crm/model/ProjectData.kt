package com.example.crm.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.crm.utility.FileTypeConverter
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "draft_table")
@TypeConverters(FileTypeConverter::class)
data class ProjectData(
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
    @PrimaryKey
    val projectId: String,
    val projectName: String?,
    val projectSubType: String?,
    val projectType: String,
    val region: String?,
    val townshipName: String?,
    val transactionId: Int?,
    val remark: String?,
    val isPending: Boolean
) : Parcelable
