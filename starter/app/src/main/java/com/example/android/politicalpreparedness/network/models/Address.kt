package com.example.android.politicalpreparedness.network.models

data class Address (
        val line1: String,
        val line2: String? = "",
        val city: String,
        val state: String,
        val zip: String,
        val locationName: String? = null
) {
    override fun toString(): String {
        var output = line1.plus("\n")
        if (!line2.isNullOrEmpty()) output = output.plus(line2).plus("\n")
        output = output.plus("$city, $state $zip")
        return output
    }

    val isEmpty: Boolean
        get() = line1.isEmpty() || city.isEmpty() || state.isEmpty() || zip.isEmpty()

}