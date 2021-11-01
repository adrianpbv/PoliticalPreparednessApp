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


    private val _navigateDetailElections = MutableLiveData<Int>()
    val navigateDetailElections: LiveData<Int> = _navigateDetailElections

    //TODO: Create live data val for upcoming elections
//    private val _upcomingElections = MutableLiveData<List<Election>>()
    lateinit var upcomingElectionsDbase: LiveData<Result<List<Election>>>

    val upcomingElections: LiveData<List<Election>> = Transformations.map(upcomingElectionsDbase) {
        if (it is Result.Success)
            it.data
        else
            emptyList()
    }

    lateinit var savedElectionsDbase: LiveData<Result<List<Election>>>

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
        it is Result.Error
    }

    val emptySavedElectionList = Transformations.map(upcomingElectionsDbase){
        it is Result.Success && it.data.isEmpty()
    }


    //TODO: Create live data val for saved elections
    //private val _savedElections = MutableLiveData<List<Election>>()

    // get() = _savedElections

    init {
        _connectionError.value = false
        viewModelScope.launch {
            try {
                // refresh the database with new upcoming election
                repository.refreshElections()
                getElectionsFromDataBase()
            } catch (e: UnknownHostException) {
                // catching the network error in the viewModel to show an informative error message
                _connectionError.value = repository.isElectionTableEmpty() == 0
                showErrorMessage.value = R.string.networkError
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun getElectionsFromDataBase() {
        _loadingData.value = true
        viewModelScope.launch {
            delay(2000)
            upcomingElectionsDbase = repository.getElections()
            savedElectionsDbase = repository.getSavedElections()
            _loadingData.value = false
        }
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info
    fun navigateToDetailsElection(idElection: Int) {
        _navigateDetailElections.value = idElection
    }

}