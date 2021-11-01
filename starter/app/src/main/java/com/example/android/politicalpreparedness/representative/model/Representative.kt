package com.example.android.politicalpreparedness.representative.model

import com.example.android.politicalpreparedness.network.models.Office
import com.example.android.politicalpreparedness.network.models.Official
import java.util.*

data class Representative (
        var id: String = UUID.randomUUID().toString(),
        val official: Official,
        val office: Office
)