package com.example.crm.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.crm.model.DraftListModel

@Dao
interface DraftDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(draft: DraftListModel)

    @Query("SELECT * from draft_table")
    fun getAllData(): LiveData<List<DraftListModel>>

    @Query("SELECT * from draft_table WHERE ProjectId=:ProjectId")
    fun getAllDataByProjectId(ProjectId: String): LiveData<List<DraftListModel>>

    @Query("SELECT * from draft_table")
    fun getResetAbleData(): List<DraftListModel>

    @Delete
    fun deleteListModel(draft: DraftListModel)

    @Query("UPDATE draft_table SET isPending = :isPending, TownshipName = :townshipName, Constructionslab = :constructionSlab, CompDate = :compDate, Lat = :latitude, Long = :longitude WHERE ProjectId = :ProjectId")
    fun updateData(
        isPending: Boolean,
        ProjectId: String,
        townshipName: String? = null,
        constructionSlab: String? = null,
        compDate: String?,
        latitude: Double?,
        longitude: Double?
    )


}