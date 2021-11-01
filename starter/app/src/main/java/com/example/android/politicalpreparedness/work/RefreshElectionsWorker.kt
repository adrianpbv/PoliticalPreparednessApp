package com.example.android.politicalpreparedness.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.example.android.politicalpreparedness.repository.Repository
import retrofit2.HttpException

/**
 * Worker for updating the election database with new data from the server
 */
class RefreshElectionsWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshElectionsWorker"
    }

    /**
     * A coroutine-friendly method to do the periodic work.
     */
    override suspend fun doWork(): Result {
        val database = getInstance(applicationContext)
        val repository = Repository(database)
        return try {
            repository.deleteUnsavedElections()
            repository.refreshElections()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }
}