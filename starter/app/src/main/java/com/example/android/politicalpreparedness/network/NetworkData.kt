package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.network.models.Election

/**
 * Class to match the Asteroids List from the network
 */
data class NetworkData(val elections: ArrayList<Election>)

