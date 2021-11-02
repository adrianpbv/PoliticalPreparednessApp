package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.utils.Result
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.AdministrationBody
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.network.models.toDomainModel
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    app: Application,
    private val repository: Repository,
    private val electionId: Int) : BaseViewModel(app) {

    private val voteInfo = repository.observeVoteInfo()
    val electionAdministrationBody: LiveData<AdministrationBody?> = Transformations.switchMap(voteInfo){
        getAdministrationBody(it)
    }
    val dataElection: LiveData<Election?> = Transformations.switchMap(voteInfo){
        getElection(it)
    }
    val isSaved : LiveData<Boolean> = Transformations.map(repository.isTheElectionSaved(electionId)){
        it != 0 // if it is not null, means that it's marked as saved (in the savedTable)
    }

    private val _isDataLoadingError = MutableLiveData<Boolean>(false)
    val isDataLoadingError: LiveData<Boolean>
            get() = _isDataLoadingError

    val isLoadingData = Transformations.map(voteInfo){
        it is Result.Loading
    }
    private lateinit var election: Election

    init {
        getElectionFromDataBase()
        getVoteInformation()
    }

    fun getAdministrationBody(voteInformation: Result<VoterInfoResponse>): LiveData<AdministrationBody?>{
        val result = MutableLiveData<AdministrationBody?>()

        if (voteInformation is Result.Success){
            _isDataLoadingError.value = false
            result.value = voteInformation.data.state?.get(0)?.electionAdministrationBody
        }else{
            result.value = null
            _isDataLoadingError.value = true
            showSnackBarInt.value = R.string.loading_data_error
        }
        return result
    }

    fun getElection(voteInformation: Result<VoterInfoResponse>): LiveData<Election?>{
        val result = MutableLiveData<Election?>()

        if (voteInformation is Result.Success){
            _isDataLoadingError.value = false
            result.value = voteInformation.data.electionEntity.toDomainModel()
        }else{
            result.value = null
            _isDataLoadingError.value = true
            showSnackBarInt.value = R.string.loading_data_error
        }
        return result
    }

    fun getElectionFromDataBase(){
        viewModelScope.launch {
            election = repository.getElectionById(electionId)!!
        }
    }

    fun getVoteInformation(){
        if (::election.isInitialized){
            val address = election.division.state.plus(" ," +  election.division.country)
            viewModelScope.launch {
                repository.getVoterInfo(address, electionId) // LiveData voteInfo is aware of changes
            }
        }else{
            _isDataLoadingError.value = true
        }
    }

    fun getElectionInfUrl(): String?{
        val voteInformation = voteInfo.value
        if (voteInformation is Result.Success){
            return voteInformation.data.state!![0].electionAdministrationBody.electionInfoUrl
        }
        return null
    }

    fun getBallotInfoUrl(): String?{
        val voteInformation = voteInfo.value
        if (voteInformation is Result.Success){
            return voteInformation.data.state!![0].electionAdministrationBody.ballotInfoUrl
        }
        return null
    }

    fun handleSavedElections(){
        if(isSaved.value == true){
            unsaveElection()
        }else
            saveElection()
    }

    //TODO: Add var and methods to save and remove elections to local database
    fun saveElection(){
        viewModelScope.launch {
            repository.setElectionAsSaved(electionId)
        }
    }

    fun unsaveElection(){
        viewModelScope.launch {
            repository.deleteSavedElection(electionId)
        }
    }
}