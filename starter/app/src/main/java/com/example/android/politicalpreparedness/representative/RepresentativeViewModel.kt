package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.repository.IRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.utils.Result
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class RepresentativeViewModel(
    app: Application,
    private val repository: IRepository
) : BaseViewModel(app) {

    private val address = MutableLiveData<Address>()

    private val _loadingData = MutableLiveData(false)
    val loadingData: LiveData<Boolean>
        get() = _loadingData

    private val _networkError = MutableLiveData(false)
    val networkError: LiveData<Boolean>
        get() = _networkError

    val representative = Transformations.switchMap(address) {
        viewModelScope.launch {
            // get the representative from the network
            repository.getRepresentative(it.toString())
        }
        // observe and update the data
        Transformations.map(repository.observeRepresentatives()) {
            getRepresentativeList(it)
        }
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

    /**
     * Function to fetch representatives from the API given a provided address
     */
    private fun getRepresentativeList(response: Result<RepresentativeResponse>)
            : List<Representative> {
        return when (response) {
            is Result.Loading -> {
                Timber.i("****** Result Loading *****")
                _loadingData.value = true
                _networkError.value = false
                emptyList()
            }
            is Result.Error -> {
                _loadingData.value = false
                _networkError.value = true
                showErrorMessage.value = R.string.loading_data_error
                //val error = response.value as Result.Error
                //Timber.e(error.exception)
                emptyList()
            }
            is Result.Success -> {
                _loadingData.value = false
                _networkError.value = false
                //val representative = response.value as Result.Success
                Timber.i(
                    "****** Result Successful ****** -> %s",
                    response.data.officials.get(0).name
                )
                response.data.offices.flatMap {
                    // flatMap allows to return all the representative lists in a single list
                    it.getRepresentatives(response.data.officials)
                }
            }
            else -> {
                _loadingData.value = false
                _networkError.value = false
                showErrorMessage.value = R.string.loading_data_error
                Timber.e("Null values")
                emptyList()
            }
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
     * Function that takes the address provided by the user
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
