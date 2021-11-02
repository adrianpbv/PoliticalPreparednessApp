package com.example.android.politicalpreparedness.network.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "election_table")
data class ElectionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "electionDay") val electionDay: Date,
    @Embedded(prefix = "division_") @Json(name = "ocdDivisionId") val division: Division
)

@Entity(tableName = "saved_election_table")
data class SavedElectionEntity(
    @PrimaryKey val idSavedElection: Int
)

@Parcelize
data class Election(
    val id: Int,
    val name: String,
    val electionDay: Date,
    val division: Division
): Parcelable{
    fun toEntityDatabase(): ElectionEntity =
        ElectionEntity(
                id = this.id,
                name = this.name,
            electionDay = this.electionDay,
            division = this.division
        )
}

fun ElectionEntity.toDomainModel(): Election = Election(
    id= this.id,
    name = this.name,
    electionDay = this.electionDay,
    division = this.division
)


/**
 * Extension function to map and get a list of Elections that will be used inside the app for
 * showing the Elections data on the UI
 */
fun List<ElectionEntity>.asDomainModel(): List<Election> {
    return map {
        Election(
            id = it.id,
            name = it.name,
            electionDay = it.electionDay,
            division = it.division
        )
    }
}