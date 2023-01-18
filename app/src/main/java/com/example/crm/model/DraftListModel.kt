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
data class DraftListModel(
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
    @PrimaryKey
    val ProjectId: String,
    val ProjectName: String?,
    val ProjectSubType: String?,
    val ProjectType: String,
    val Region: String?,
    val TownshipName: String?,
    val TransId: Int?,
    val Remark: String?,
    val isPending: Boolean

) : Parcelable
