package com.example.crm.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "image_table")
data class ImageDetails(
    @PrimaryKey(autoGenerate = true)
    val imageId: Int = 0,
    val transactionId: Int?,
    val projectId: String,
    var imageName: String?,
    var imageString: String?,
    var imageType: String?,
    var latitude: Double?,
    var longitudes: Double?,
    val imageTime: String?,
    val userId: String?,
    val imageDate: String?,
    val createdBy: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val images: ByteArray?
) : Parcelable

data class ImageRequestDetails(
    val imageId: Int?,
    val imageName: String?,
    val imageType: String?,
    val transactionId: Int?,
    val projectId: String?,
    val imageDate: String?,
    val userId: String?,
    val latitude: Double?,
    val longitudes: Double?,
    val image: String
)

data class UpdateSurveyImageDetailResult(
    val result: Boolean?
)
