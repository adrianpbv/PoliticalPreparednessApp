package com.example.android.politicalpreparedness.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.*
import com.example.android.politicalpreparedness.utils.Result
import com.example.android.politicalpreparedness.utils.Result.Success
import com.example.android.politicalpreparedness.utils.asSavedElectionEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException


/**
 * Repository that communicates with the NetworkApi and the database
 */
class Repository(
    private val database: ElectionDatabase,
    private val repoDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IRepository {

    private val observeReprensentative = MutableLiveData<Result<RepresentativeResponse>>()
    private val observeVoteInfo = MutableLiveData<Result<VoterInfoResponse>>()

    private val _isElectionFlowEmpty = MutableStateFlow(false)
    val isElectionFlowEmpty: StateFlow<Boolean> = _isElectionFlowEmpty

    /**
     * Refresh the [Election]s in the dataBase with data from the network
     */
    override suspend fun refreshElections() {
        withContext(Dispatchers.IO) {
            val elections = CivicsApi.retrofitService.getElections()
            database.electionDao.insertAll(*elections.asDataBaseEntity())
        }
    }

    /**
     * Get LiveData with all the [Election]s from the database
     */
    override fun getElections(): Flow<Result<List<Election>>> {
        return try {
            database.electionDao.getAllElections()
                .map { Success(it.asDomainModel())}
                .flowOn(Dispatchers.IO)
        }catch (exc : Exception){
            Timber.e(exc)
            flowOf(Result.Error(exc))
        }
    }

    /*
    * Get a Flow to share between several collector,
    * [WhileSubscribed] sharing policy is used to cancel the upstream
    *  flow when there are no collectors
     */
    val electionSharedFlow = database.electionDao.getAllElections().shareIn(
        scope = CoroutineScope(repoDispatcher),
        SharingStarted.WhileSubscribed(5000)
    )

    /**
     * Get an [Election] by its Id
     */
    override fun getElectionById(electionId: Int): LiveData<Result<Election?>> {
        return try {
            Transformations.map(database.electionDao.getElectionById(electionId)) {
                Success(it?.toDomainModel())
            }
        } catch (exc: Exception) {
            Timber.e(exc)
            val temp = MutableLiveData<Result<Election?>>()
            temp.value = Result.Error(exc)
            temp
        }
    }

    /**
     * Get the number of rows in the [Election] table, is it has 0 values it is empty
     */
    override suspend fun electionTableEmpty(): Int = database.electionDao.isElectionTableEmpty()


    /**
     * Function that determines whether there are elections in the database or not
     */
    override suspend fun isElectionTableEmpty(): Int {
        var isTableEmpty = 0
        withContext(Dispatchers.IO) {
            isTableEmpty = async {
                electionTableEmpty()
            }.await() // await for the result
        }
        return isTableEmpty
    }

    /**
     * Get LiveData to know if the savedElectionTable is empty
     */
    override fun isSavedElectionTableEmpty(): LiveData<Boolean> =
        Transformations.map(database.electionDao.isSavedElectionTableEmpty()) {
            Timber.i("isSavedElectionTableEmpty Count--> %d", it)
            it == 0
        }

    /**
     *  Function to delete all the unsaved [Election]s from the database
     */
    override suspend fun deleteUnsavedElections() {
        withContext(Dispatchers.IO) {
            database.electionDao.deleteUnsavedElections()
        }
    }

    /**
     * Function to delete an specific [Election] by its id
     */
    override suspend fun deleteElectionById(id: Int) {
        withContext(Dispatchers.IO) {
            database.electionDao.getDeleteElectionById(id)
        }
    }

    /**
     * Get the [Election]s saved by the user
     */
    override fun getSavedElections(): LiveData<Result<List<Election>>> {
        try {
            return Transformations.map(database.electionDao.getSavedElections()) {
                Success(it.asDomainModel())
            }
        } catch (exception: Exception) {
            Timber.e(exception)
            val elections = MutableLiveData<Result<List<Election>>>()
            elections.value = Result.Error(exception)
            return elections
        }
    }

    /**
     * Save an election by inserting its id into the saved_table
     */
    override suspend fun setElectionAsSaved(id: Int) {
        withContext(Dispatchers.IO) {
            database.electionDao.insertSavedElection(id.asSavedElectionEntity())
        }
    }

    /**
     * Unsaved an elections by deleting its Id in the savedElection table
     */
    override suspend fun deleteSavedElection(id: Int) {
        withContext(Dispatchers.IO) {
            database.electionDao.unSavedElection(id)
        }
    }

    /**
     * Get LiveData to whether the election is saved or not
     */
    override fun isTheElectionSaved(id: Int): LiveData<Int> =
        database.electionDao.getSavedElectionById(id)

    /**
     * Sync the vote information with observers
     */
    override fun observeVoteInfo(): LiveData<Result<VoterInfoResponse>> {
        return observeVoteInfo
    }

    /**
     * Looks up information relevant to a voter based on the voter's registered address
     */
    override suspend fun getVoterInfo(address: String, electionId: Int) {
        // Update the LiveData while the information is being downloaded from the network.
        observeVoteInfo.value = Result.Loading
        withContext(Dispatchers.IO) {
            delay(1000)
            try {
                val response = CivicsApi.retrofitService.getVoterInfo(address, electionId)
                observeVoteInfo.postValue(
                    Success(response)
                )
                Timber.e(
                    "Repository Response electionUrl: %s",
                    response.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
                )
                Timber.e(
                    "Repository Response State: %s",
                    response.state?.get(0)?.electionAdministrationBody?.physicalAddress?.state
                )
            } catch (errorHttp: HttpException) {
                observeVoteInfo.postValue(Result.Error(errorHttp))
                Timber.e(errorHttp)
                Timber.e("Http error: %s", errorHttp.message())
            } catch (errorHost: UnknownHostException) {
                observeVoteInfo.postValue(Result.Error(errorHost))
                Timber.e(errorHost, "Error message: %s", errorHost.message)
            } catch (error: Exception) {
                observeVoteInfo.postValue(Result.Error(error))
                Timber.e(error, "Error message: %s", error.message)
            }
        }
    }

    override fun observeRepresentatives(): LiveData<Result<RepresentativeResponse>> =
        observeReprensentative

    /**
     * Get the [Representative] by address
     */
    override suspend fun getRepresentative(address: String) {
        observeReprensentative.value = Result.Loading
        withContext(Dispatchers.IO) {
            delay(1000)
            try {
                observeReprensentative.postValue(
                    Success(
                        CivicsApi.retrofitService.getRepresentatives(
                            address
                        )
                    )
                )
            } catch (errorHttp: HttpException) {
                observeReprensentative.postValue(Result.Error(errorHttp))
                Timber.e(errorHttp)
            } catch (errorHost: UnknownHostException) {
                observeReprensentative.postValue(Result.Error(errorHost))
                Timber.e(errorHost)
            } catch (exc: Exception) {
                observeReprensentative.postValue(Result.Error(exc))
                Timber.e(exc)
            }
        }
    }


}