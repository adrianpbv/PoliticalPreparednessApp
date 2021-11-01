package com.example.android.politicalpreparedness.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.android.politicalpreparedness.utils.Result
import com.example.android.politicalpreparedness.utils.Result.Success
import com.example.android.politicalpreparedness.utils.asSavedElectionEntity
import kotlinx.coroutines.delay
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException


/**
 * Repository that communicates with the NetworkApi and the database
 */
class Repository(private val database: ElectionDatabase) {

    private val observeReprensentative = MutableLiveData<Result<RepresentativeResponse>>()
    private val observeVoteInfo = MutableLiveData<Result<VoterInfoResponse>>()

    /**
     * Refresh the elections in the dataBase with data from the network
     */
    suspend fun refreshElections() {
        withContext(Dispatchers.IO){
            val elections = CivicsApi.retrofitService.getElections()
            database.electionDao.insertAll(*elections.asDataBaseEntity())
        }
    }

    /**
     * Get LiveData with all the Elections from the database
     */
    fun getElections(): LiveData<Result<List<Election>>>{
        try{
            return Transformations.map(database.electionDao.getAllElections()) {
                Success(it.asDomainModel()) // data is a list of Elections
            }
        }catch(exception: Exception){
            Timber.e(exception)
            val elections = MutableLiveData<Result<List<Election>>>()
            elections.value = Result.Error(exception)
            return elections
        }
    }

    /**
     * Get an election by its Id
     */
    suspend fun getElectionById(electionId: Int): Election?{
        withContext(Dispatchers.IO){
            return@withContext database.electionDao.getElectionById(electionId)?.toDomainModel()
        }
        return null
    }

    /**
     * Function that determines whether there are elections in the database or not
     */
    suspend fun isElectionTableEmpty(): Int = database.electionDao.isElectionTableEmpty()


    /**
     *  Function to delete all the unsaved elections from the database
     */
    suspend fun deleteUnsavedElections() {
        withContext(Dispatchers.IO) {
            database.electionDao.deleteUnsavedElections()
        }
    }

    /**
     * Function to delete an specific election by its id
     */
    suspend fun deleteElectionById(id: Int) {
        withContext(Dispatchers.IO) {
            database.electionDao.getDeleteElectionById(id)
        }
    }

    /**
     * Get the elections saved by the user
     */
    fun getSavedElections(): LiveData<Result<List<Election>>> {
        try {
            return Transformations.map(database.electionDao.getSavedElections()){
                Success(it.asDomainModel())
            }
        }catch(exception: Exception){
            Timber.e(exception)
            val elections = MutableLiveData<Result<List<Election>>>()
            elections.value = Result.Error(exception)
            return elections
        }
    }

    /**
     * Save an election through inserting its id into the saved_table
     */
    suspend fun setElectionAsSaved(id: Int){
        withContext(Dispatchers.IO){
            database.electionDao.insertSavedElection(id.asSavedElectionEntity())
        }
    }

    /**
     * Unsaved an elections by deleting its Id in the savedElection table
     */
    suspend fun deleteSavedElection(id: Int){
        withContext(Dispatchers.IO){
            database.electionDao.unSavedElection(id)
        }
    }

    /**
     * Get LiveData to whether the election is saved or not
     */
    fun isTheElectionSaved(id: Int): LiveData<Int> =
        database.electionDao.getSavedElectionById(id)

    fun observeRepresentatives(): LiveData<Result<RepresentativeResponse>> =
        observeReprensentative

    /**
     * Get the representatives by address
     */
    suspend fun getRepresentative(address: String){
        observeReprensentative.value = Result.Loading
        delay(2000)
        withContext(Dispatchers.IO){
            try {
                observeReprensentative.postValue(Success(CivicsApi.retrofitService.getRepresentatives(address)))
            }catch (errorHttp: HttpException){
                observeReprensentative.postValue(Result.Error(errorHttp))
                Timber.e(errorHttp)
            }catch (errorHost: UnknownHostException){
                observeReprensentative.postValue(Result.Error(errorHost))
                Timber.e(errorHost)
            }catch (exc: Exception){
                observeReprensentative.postValue(Result.Error(exc))
                Timber.e(exc)
            }
        }
    }

    /**
     * Sync the vote information with observers
     */
    fun observeVoteInfo(): LiveData<Result<VoterInfoResponse>>{
        return observeVoteInfo
    }

    /**
     * Looks up information relevant to a voter based on the voter's registered address
     */
    suspend fun getVoterInfo(address: String, electionId: Int){
        // Update the LiveData while the information is being downloaded from the network.
        observeVoteInfo.value = Result.Loading
        delay(2000)
        withContext(Dispatchers.IO){
            try {
                observeVoteInfo.value = Success(CivicsApi.retrofitService.getVoterInfo(address, electionId))
            }catch (errorHttp: HttpException){
                observeVoteInfo.value = Result.Error(errorHttp)
                Timber.e(errorHttp)
            }catch (errorHost: UnknownHostException){
                observeVoteInfo.value = Result.Error(errorHost)
                Timber.e(errorHost)
            }catch (error: Exception){
                observeVoteInfo.value = Result.Error(error)
                Timber.e(error)
            }
        }
    }
}