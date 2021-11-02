package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.utils.Event
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(application: Application) : BaseViewModel(application) {

    private val database = ElectionDatabase.getInstance(application)
    private val repository = Repository(database)


    private val _navigateDetailElections = MutableLiveData<Int?>()
    val navigateDetailElections: LiveData<Int?> = _navigateDetailElections

    //LiveData for upcoming elections
    private val upcomingElectionsDbase = repository.getElections()//MutableLiveData<Result<List<Election>>>()

    val upcomingElections: LiveData<List<Election>> = Transformations.map(upcomingElectionsDbase) {
        if (it is Result.Success) {
            Timber.i("Upcoming Elections LiveData %d", it.data.size)
            it.data
        } else
            emptyList()
    }

    //LiveData for saved elections
    private val savedElectionsDbase = repository.getSavedElections()//MutableLiveData<Result<List<Election>>>()

    val savedElections: LiveData<List<Election>> = Transformations.map(savedElectionsDbase){
        if (it is Result.Success){
            it.data
        }else
            emptyList()
    }

    // State variables for the different states of the information
    private val _connectionError = MutableLiveData<Boolean>()
    val connectionError: LiveData<Boolean> = _connectionError

    private val _loadingData = MutableLiveData<Boolean>()
    val loadingData: LiveData<Boolean> = _loadingData

    val errorFromDataBase = Transformations.map(upcomingElectionsDbase) {
        if(it is Result.Error) {
            showErrorMessage.value = R.string.loading_data_error
            true
        }else false
    }

    val emptySavedElectionList = Transformations.map(savedElectionsDbase){
        it is Result.Success && it.data.isEmpty()
    }

    init {
        _loadingData.value = true
        _connectionError.value = false
        viewModelScope.launch {
            try {
                // refresh the database with new upcoming election
                repository.refreshElections()
                //_loadingData.value = false
            } catch (error: UnknownHostException) {
                // catching the network error in the viewModel to show an informative error message
                Timber.e(error)
                _connectionError.value = repository.isElectionTableEmpty() == 0
                showErrorMessage.value = R.string.networkError
            } catch (error: Exception) {
                Timber.e(error)
                _connectionError.value = repository.isElectionTableEmpty() == 0
                showErrorMessage.value = R.string.networkError
            }
            _loadingData.value = false // The loading state has ended so the app is in another state
        }
    }

    fun navigateToDetailsElection(idElection: Int) {
        _navigateDetailElections.value = idElection
    }

    fun navigationDetailElectionCompleted(){
        _navigateDetailElections.value = null
    }

}