package com.example.crm.worker

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import retrofit.RetrofitInstance

class LocationWorker(val context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BugInfo", "Dowork called.")
        return Result.success(
            workDataOf(
                "location" to 1
            )
        )
    }

    companion object {
        private const val LOCATION_DATA = "location"
    }
}