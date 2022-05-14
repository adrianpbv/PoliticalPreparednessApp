package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.AdministrationBody
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.State
import com.example.android.politicalpreparedness.repository.IRepository
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    app: Application,
    private val repository: IRepository,
    private val electionId: Int
) : BaseViewModel(app) {

    val isSaved: LiveData<Boolean> =
        Transformations.map(repository.isTheElectionSaved(electionId)) {
            it != 0 // if it is not null, means that it's marked as saved (in the savedTable)
        }

    val election: LiveData<Election?> =
        Transformations.switchMap(repository.getElectionById(electionId)) {
            getElectionFromDataBase(it)
        }


    private val voteInfo = Transformations.switchMap(election) {
        it?.let {
            // If an error from the database happened so the election will be null and this is not executed
            // Only the icon error from the dataBase is shown.
            val address = it.division.state.plus(", " + it.division.country)
            viewModelScope.launch {
                repository.getVoterInfo(
                    address,
                    electionId
                ) // LiveData voteInfo is aware of changes
            }
        }
        repository.observeVoteInfo()
    }

    val state: LiveData<State?> = Transformations.map(voteInfo) {
        if (it is Result.Success) {
            it.data.state?.get(0)
        } else
            null
    }

    val electionAdministrationBody: LiveData<AdministrationBody> = Transformations.map(state) {
        it?.electionAdministrationBody
    }

    val physicalAddress: LiveData<Address?> =
        Transformations.map(electionAdministrationBody) {
            it?.physicalAddress
        }

    val networkError: LiveData<Boolean> = Transformations.map(voteInfo) {
        if (it is Result.Error) {
            showErrorMessage.value = R.string.loading_data_error
        }
        it is Result.Error
    }

    private val _dataBaseError = MutableLiveData<Boolean>(false)
    val dataBaseError: LiveData<Boolean>
        get() = _dataBaseError

    val isLoadingDataFromNetwork = Transformations.map(voteInfo) {
        it is Result.Loading
    }

    private val _isLoadingDataFromDBase = MutableLiveData<Boolean>(true)
    val isLoadingDataFromDBase: LiveData<Boolean>
        get() = _isLoadingDataFromDBase

    /**
     * Get the election by its id and determine the state of the query
     */
    fun getElectionFromDataBase(electionDB: Result<Election?>): LiveData<Election?> {
        _isLoadingDataFromDBase.value = true // Show a loading progress while the dataBase operation
        // is running and ends up in one state(Error or Success)
        val electionResult = MutableLiveData<Election?>()
        if (electionDB is Result.Success) {
            _dataBaseError.value = false
            electionResult.value = electionDB.data
        } else {
            showErrorMessage.value = R.string.loading_data_error
            _dataBaseError.value = true
            electionResult.value = null
        }
        _isLoadingDataFromDBase.value = false
        return electionResult
    }

    /**
     * Manage the state of the election selected by the user, set it as a followed election if
     * it was unfollowed previously, otherwise the opposite
     */
    fun handleSavedElections() {
        if (isSaved.value == true) {
            unsaveElection()
        } else
            saveElection()
    }

    /**
     * Save the current election into the db
     */
    fun saveElection() {
        viewModelScope.launch {
            repository.setElectionAsSaved(electionId)
        }
    }

    /**
     * Delete the current election from the db
     */
    fun unsaveElection() {
        viewModelScope.launch {
            repository.deleteSavedElection(electionId)
        }
    }
}