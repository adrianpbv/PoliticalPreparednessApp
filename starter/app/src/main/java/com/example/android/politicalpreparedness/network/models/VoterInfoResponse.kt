package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VoterInfoResponse (
    @Json(name = "election")val electionEntity: ElectionEntity,
    val pollingLocations: String? = null, // Future Use
    val contests: String? = null, //Future Use
    val state: List<State>? = null,
    val electionElectionOfficials: List<ElectionOfficial>? = null
)