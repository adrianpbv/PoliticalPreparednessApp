package com.example.android.politicalpreparedness.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Division(
        val id: String,
        val country: String,
        val state: String,
        val district: String
) : Parcelable {
        override fun toString(): String {
                return "Division: id: $id, Country: $country, State: $state, District: $district"
        }

        fun getAddress(): String {
                val state = if (this.state != "") "$state ," else ""
                val district = if(this.district != "") "$district ," else ""

                return state + district + country
        }
}