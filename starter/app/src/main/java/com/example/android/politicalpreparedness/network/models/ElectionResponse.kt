package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ElectionResponse(
        val kind: String,
        @Json(name = "elections")val electionEntities: List<ElectionEntity>
)

fun ElectionResponse.asDataBaseEntity(): Array<ElectionEntity> {
        return electionEntities.toTypedArray()
}