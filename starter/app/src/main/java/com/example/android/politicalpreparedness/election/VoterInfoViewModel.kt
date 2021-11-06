package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.AdministrationBody
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.State
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class VoterInfoViewModel(
    app: Application,
    private val electionId: Int
) : BaseViewModel(app) {

    private val database = ElectionDatabase.getInstance(app)
    private val repository = Repository(database)

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

    fun getElectionInfUrl(): String? {
        val voteInformation = voteInfo.value
        if (voteInformation is Result.Success) {
            return voteInformation.data.state!![0].electionAdministrationBody.electionInfoUrl
        }
        return null
    }

    fun getBallotInfoUrl(): String? {
        val voteInformation = voteInfo.value
        if (voteInformation is Result.Success) {
            return voteInformation.data.state!![0].electionAdministrationBody.ballotInfoUrl
        }
        return null
    }

    fun handleSavedElections() {
        if (isSaved.value == true) {
            unsaveElection()
        } else
            saveElection()
    }

    //TODO: Add var and methods to save and remove elections to local database
    fun saveElection() {
        viewModelScope.launch {
            repository.setElectionAsSaved(electionId)
        }
    }

    fun unsaveElection() {
        viewModelScope.launch {
            repository.deleteSavedElection(electionId)
        }
    }

    fun checkPopulatedData() {
        Timber.i("Election name: %s", election.value?.name)
        Timber.i("Election Id: %s", election.value?.id)
        Timber.i(" ****** Testing the State ******")
        Timber.i("ElectionInfoUrl name: %s", electionAdministrationBody.value?.electionInfoUrl)
        Timber.i(
            "Election State name: %s",
            electionAdministrationBody.value?.physicalAddress?.state
        )
        Timber.i("AVAILABLE ADDRESS -->> %s", physicalAddress.value == null)
    }
}

class VoterInfoViewModelFactory(
    private val application: Application,
    private val electionId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VoterInfoViewModel(application, electionId) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}