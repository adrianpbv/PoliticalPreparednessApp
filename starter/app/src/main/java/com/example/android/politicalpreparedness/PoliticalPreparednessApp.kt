package com.example.android.politicalpreparedness

import android.app.Application
import androidx.work.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.repository.IRepository
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.representative.RepresentativeViewModel
import com.example.android.politicalpreparedness.work.RefreshElectionsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
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

        // Koin implementation
        val koinModule = module {

            // Declare singletons
            single { ElectionDatabase.getInstance(this@PoliticalPreparednessApp) } // DB
            single { Repository(get()) as IRepository} // An instance from the repository

            // Declaring the viewModels
            viewModel{
                ElectionsViewModel(get(), get())
                RepresentativeViewModel(get(), get())
            }
        }

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@PoliticalPreparednessApp)
            modules(koinModule)
        }
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