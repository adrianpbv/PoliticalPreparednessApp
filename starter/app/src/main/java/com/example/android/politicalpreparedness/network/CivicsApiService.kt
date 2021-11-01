package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.network.models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://www.googleapis.com/civicinfo/v2/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(ElectionAdapter()) // adapter for the Division field in the model
    .add(Rfc3339DateJsonAdapter()) // adapters for Java Date, parse the date string to a date object
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .client(CivicsHttpClient.getClient())
    .baseUrl(BASE_URL)
    .build()


/**
 *  Documentation for the Google Civics API Service can be found at https://developers.google.com/civic-information/docs/v2
 */

interface CivicsApiService {
    /**
     * List of available elections to query.
     */
    @GET("elections")
    suspend fun getElections(): ElectionResponse

    /**
     * Looks up information relevant to a voter based on the voter's registered address
     */
    @GET("voterinfo")
    suspend fun getVoterInfo(
        @Query("address") address: String,
        @Query("electionID") idElection: Int
    ): VoterInfoResponse

    @GET("voterinfo")
    suspend fun getVoterInfo(
        @Query("address") address: String,
        @Query("electionId") electionId: String,
        @Query("officialOnly") official: Boolean
    ): String

    /**
     * Looks up political geography and representative information
     */
    @GET("representatives")
    suspend fun getRepresentatives(
        @Query("address") address: String
    ): RepresentativeResponse

    @GET("representatives/ocdId")
    suspend fun getRepresentByDivision(
        @Query("ocdId") division: String // optional parameter
    ): String
}

object CivicsApi {
    val retrofitService: CivicsApiService by lazy {
        retrofit.create(CivicsApiService::class.java)
    }
}