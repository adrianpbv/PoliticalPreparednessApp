package com.example.android.politicalpreparedness.repository

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.utils.Result

interface IRepository {
    /**
     * Refresh the elections in the dataBase with data from the network
     */
    suspend fun refreshElections()

    /**
     * Get LiveData with all the Elections from the database
     */
    fun getElections(): LiveData<Result<List<Election>>>

    /**
     * Get an election by its Id
     */
    fun getElectionById(electionId: Int): LiveData<Result<Election?>>

    suspend fun electionTableEmpty(): Int

    /**
     * Function that determines whether there are elections in the database or not
     */
    suspend fun isElectionTableEmpty(): Int

    /**
     * Get LiveData to know if the savedElectionTable is empty
     */
    fun isSavedElectionTableEmpty(): LiveData<Boolean>

    /**
     *  Function to delete all the unsaved elections from the database
     */
    suspend fun deleteUnsavedElections()

    /**
     * Function to delete an specific election by its id
     */
    suspend fun deleteElectionById(id: Int)

    /**
     * Get the elections saved by the user
     */
    fun getSavedElections(): LiveData<Result<List<Election>>>

    /**
     * Save an election through inserting its id into the saved_table
     */
    suspend fun setElectionAsSaved(id: Int)

    /**
     * Unsaved an elections by deleting its Id in the savedElection table
     */
    suspend fun deleteSavedElection(id: Int)

    /**
     * Get LiveData to whether the election is saved or not
     */
    fun isTheElectionSaved(id: Int): LiveData<Int>

    /**
     * Sync the vote information with observers
     */
    fun observeVoteInfo(): LiveData<Result<VoterInfoResponse>>

    /**
     * Looks up information relevant to a voter based on the voter's registered address
     */
    suspend fun getVoterInfo(address: String, electionId: Int)
    fun observeRepresentatives(): LiveData<Result<RepresentativeResponse>>

    /**
     * Get the representatives by address
     */
    suspend fun getRepresentative(address: String)

    suspend fun getRep(address: String): Result<RepresentativeResponse>
}