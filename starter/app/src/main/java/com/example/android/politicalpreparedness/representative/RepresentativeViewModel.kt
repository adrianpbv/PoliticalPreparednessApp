package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.repository.Repository
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.launch

class RepresentativeViewModel(
    app: Application,
    private val repository: Repository
) : BaseViewModel(app) {

    //TODO: Establish live data for representatives and address

    private val address = MutableLiveData<Address>()
    private val representativeResponse = Transformations.switchMap(address) {
        viewModelScope.launch {
            // get the representative from the network
            repository.getRepresentative(it.toString())
        }
        // observe and update the data
        repository.observeRepresentatives()
    }


    val representative: LiveData<List<Representative>> =
        Transformations.map(representativeResponse) {
            if (it != null) {
                getRepresentativeList(it)
            } else emptyList()
        }
    val emptyRepresentativeList = Transformations.map(representativeResponse) {
        it is Result.Success && !it.isSuccess// data is null so the list is empty
    }
    val networkError = Transformations.map(representativeResponse) {
        it is Result.Error
    }
    val loadingData = Transformations.map(representativeResponse) {
        it is Result.Loading
    }

    // Two-way dataBinding, exposing MutableLiveData
    val lineOne = MutableLiveData<String>()
    val lineTwo = MutableLiveData<String>()
    val city = MutableLiveData<String>()
    val stateString = MutableLiveData<String>()
    val stateInt = MutableLiveData<Int>()
    val zip = MutableLiveData<String>()

    init {
        stateInt.value = 0
    }
    //TODO: Create function to fetch representatives from API from a provided address

    private fun getRepresentativeList(response: Result<RepresentativeResponse>): List<Representative> {
        return if (response is Result.Success) {
            response.data.offices.flatMap {
                it.getRepresentatives(response.data.officials)
            }
        } else {
            showErrorMessage.postValue(R.string.loading_representatives_error)
            emptyList()
        }
    }

    /**
     * Update the [Address] LiveData to look for its [Representative]s
     */
    fun searchRepresentative(address: Address?) {
        address?.let {
            this.address.value = it
            setAddressFieldValues()
        }
    }

    /**
     *
     */
    fun inputAddress(selectedState: String) {
        val new_line1 = lineOne.value
        val new_line2 = lineTwo.value
        val new_city = city.value
        val new_state = selectedState
        val new_zip = zip.value

        if (new_line1 == null || new_city == null || new_zip == null) {
            showErrorMessage.value = R.string.missed_data
            return
        }

        val newAddress = Address(
            line1 = new_line1, line2 = new_line2,
            city = new_city, state = new_state, zip = new_zip
        )

        if (newAddress.isEmpty) {
            showErrorMessage.value = R.string.missed_data
            return
        }
        address.value = newAddress
    }

    /**
     * Update the UI with the current location
     */
    private fun setAddressFieldValues() {
        val currentLocation = address.value!!
        lineOne.value = currentLocation.line1
        lineTwo.value = currentLocation.line2!!
        city.value = currentLocation.city
        stateString.value = currentLocation.state
        zip.value = currentLocation.zip
    }

}
