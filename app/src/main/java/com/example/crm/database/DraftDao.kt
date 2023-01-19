package com.example.crm.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.crm.model.ProjectData

@Dao
interface DraftDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(draft: ProjectData)

    @Query("SELECT * from draft_table")
    fun getAllData(): LiveData<List<ProjectData>>

    @Query("SELECT * from draft_table WHERE projectId=:projectId")
    fun getAllDataByProjectId(projectId: String): LiveData<List<ProjectData>>

    @Query("SELECT * from draft_table")
    fun getResetAbleData(): List<ProjectData>

    @Delete
    fun deleteListModel(draft: ProjectData)

    @Query("UPDATE draft_table SET isPending = :isPending, townshipName = :townshipName, constructionSlab = :constructionSlab, compDate = :compDate, latitude = :latitude, longitude = :longitude WHERE projectId = :projectId")
    fun updateData(
        isPending: Boolean,
        projectId: String,
        townshipName: String? = null,
        constructionSlab: String? = null,
        compDate: String?,
        latitude: Double?,
        longitude: Double?
    )


}