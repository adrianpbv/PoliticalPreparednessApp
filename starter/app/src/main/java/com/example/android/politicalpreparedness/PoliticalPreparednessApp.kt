package com.example.android.politicalpreparedness

import android.app.Application
import androidx.work.*
import com.example.android.politicalpreparedness.work.RefreshElectionsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PoliticalPreparednessApp : Application() {
    val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()
    }

    /**
     * Set up the WorkManager work with its constraints and frequency
     */
    private fun setupRecurringWork() {
        // Set under what conditions the work will run
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .setRequiresDeviceIdle(true)
            .build()

        // Schedule periodic work
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshElectionsWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RefreshElectionsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}