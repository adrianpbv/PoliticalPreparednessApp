package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.IRepository
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException

class ElectionsViewModel(
    val app: Application,
    private val repository: IRepository
) : BaseViewModel(app) {

    private val _navigateDetailElections = MutableLiveData<Int?>()
    val navigateDetailElections: LiveData<Int?> = _navigateDetailElections

    //LiveData for upcoming elections
    val upcomingElectionsDB = repository.getElections()
        .map {
            if (it is Result.Success)
                it.data
            else {
                return@map emptyList()
            }
        }.asLiveData()

    val errorFromDataBase = repository.getElections()
        .map {
            if (it is Result.Error) {
                showErrorMessage.value = R.string.loading_data_error
                true
            } else false
        }.asLiveData()


    //LiveData for saved elections
    private val savedElectionsDbase =
        repository.getSavedElections()//MutableLiveData<Result<List<Election>>>()

    val savedElections: LiveData<List<Election>> = Transformations.map(savedElectionsDbase) {
        if (it is Result.Success) {
            it.data
        } else
            emptyList()
    }

    // State variables for the different states of the information
    private val _connectionError = MutableLiveData<Boolean>()
    val connectionError: LiveData<Boolean> = _connectionError

    private val _loadingData = MutableLiveData<Boolean>()
    val loadingData: LiveData<Boolean> = _loadingData




    val emptySavedElectionTable: LiveData<Boolean> = repository.isSavedElectionTableEmpty()

    init {
        _loadingData.value = true
        _connectionError.value = false
        viewModelScope.launch {
            try {
                // refresh the database with new upcoming election
                repository.refreshElections()


                } catch (error: UnknownHostException) {
                // catching the network error in the viewModel to show an informative error message
                Timber.e(error)
                _connectionError.value = repository.isElectionTableEmpty() == 0
                showErrorMessage.value = R.string.network_error
            } catch (error: Exception) {
                Timber.e(error)
                _connectionError.value = repository.isElectionTableEmpty() == 0
                showErrorMessage.value = R.string.network_error
            }
        }
        _loadingData.value = false // The loading state has ended so the app is in another state
    }

    fun navigateToDetailsElection(idElection: Int) {
        _navigateDetailElections.value = idElection
    }

    fun navigationDetailElectionCompleted() {
        _navigateDetailElections.value = null
    }

}