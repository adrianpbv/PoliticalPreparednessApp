package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import com.example.android.politicalpreparedness.network.models.SavedElectionEntity

@Dao
interface ElectionDao {

    // Insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg election: ElectionEntity)

    // Select all election query
    @Query("SELECT * FROM election_table")
    fun getAllElections(): LiveData<List<ElectionEntity>>

    // Select single election query
    @Query("SELECT * FROM election_table WHERE id = :electionId")
    fun getElectionById(electionId: Int): LiveData<ElectionEntity?>

    // Delete query
    @Query("DELETE FROM election_table WHERE id = :electionId")
    suspend fun getDeleteElectionById(electionId: Int)

    // Clear query
    @Query("DELETE FROM election_table")
    suspend fun deleteElections()

    @Query("SELECT COUNT(id) FROM ELECTION_TABLE")
    suspend fun isElectionTableEmpty(): Int

    // Save an election by inserting its id into the savedElections table
    @Insert
    suspend fun insertSavedElection(savedElectionEntity: SavedElectionEntity)

    @Query("SELECT COUNT(*) FROM saved_election_table WHERE idSavedElection = :idElection")
    fun getSavedElectionById(idElection: Int): LiveData<Int>

    @Query("SELECT e.* FROM election_table e JOIN saved_election_table se ON e.id = se.idSavedElection")
    fun getSavedElections(): LiveData<List<ElectionEntity>>

    @Query("DELETE FROM election_table WHERE id not in (SELECT e.id FROM election_table e JOIN saved_election_table se ON e.id = se.idSavedElection )")
    suspend fun deleteUnsavedElections()

    @Query("DELETE FROM saved_election_table WHERE idSavedElection = :id")
    suspend fun unSavedElection(id: Int)

    @Query("SELECT COUNT(*) FROM saved_election_table")
    fun isSavedElectionTableEmpty(): LiveData<Int>
}