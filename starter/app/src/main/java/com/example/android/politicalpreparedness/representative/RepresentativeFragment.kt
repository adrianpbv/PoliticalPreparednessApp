package com.example.android.politicalpreparedness.representative

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.viewModels
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.base.BaseFragment
import timber.log.Timber
import java.util.*

class RepresentativeFragment : BaseFragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override val _viewModel by viewModels<RepresentativeViewModel>() {
        RepresentativeViewModelFactory(requireContext().applicationContext as Application)
    }
    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var listAdapter: RepresentativeListAdapter

    private val resultLauncherLocationService = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            checkDeviceLocationAndGetLocation()
        } else {
            Snackbar.make(
                binding.representativeMotionLayout,
                R.string.location_required_error,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                checkDeviceLocationAndGetLocation()
            }.show()
        }
    }

    private var resultLauncherForegroundLocation = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkDeviceLocationAndGetLocation()
        } else showSnackBarAppSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRepresentativeBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        listAdapter = RepresentativeListAdapter()
        binding.representativeRecyclerView.adapter = listAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this

        //Button listeners for field and location search
        binding.buttonSearch.setOnClickListener {
            val state = resources.getStringArray(R.array.states)[_viewModel.stateInt.value!!]
            _viewModel.inputAddress(state)
            hideKeyboard()
        }

        binding.buttonLocation.setOnClickListener {
            getLocation()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            resultLauncherForegroundLocation.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            false
        }
    }

    private fun isPermissionGranted(): Boolean =
        checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun getLocation() {
        if (checkLocationPermissions()) {
            checkDeviceLocationAndGetLocation()
        }
    }


    /**
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within the app.
     */
    @SuppressLint("MissingPermission")
    fun checkDeviceLocationAndGetLocation() {
        // check if the device's location is on, otherwise show a dialog to activate it.

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // Using LocationServices to get the Settings Client and create
        // a val called locationSettingsResponseTask to check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        // finding out if the location settings are not satisfied
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show a dialog to activate the location service
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    resultLauncherLocationService.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.e("Error getting location settings resolution: %s", sendEx.message)
                }
            }
        }

        // If the locationSettingsResponseTask does complete, check that it is successful,
        // if so you will want to add the geofence.
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.i("Successful %s", locationSettingsResponseTask.result.toString())
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        _viewModel.searchRepresentative(geoCodeLocation(location))
                    }
                    .addOnFailureListener { exception ->
                        _viewModel.showErrorMessage.postValue(R.string.get_current_location_error)
                        Timber.e(exception)
                        _viewModel.showErrorMessage.value = R.string.get_current_location_error
                    }
            }
        }
    }

    /**
     * Helper function to change the lat/long location to a human readable street address
     */
    private fun geoCodeLocation(location: Location?): Address? {
        if (location != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(
                        address.thoroughfare,
                        address.subThoroughfare,
                        address.locality,
                        address.adminArea,
                        address.postalCode
                    )
                }
                .first()
        } else {
            _viewModel.showErrorMessage.value = R.string.get_current_location_error
            return null
        }
    }

    /**
     * Suggest to the user to grant the location permission to use this functionality within the app
     */
    private fun showSnackBarAppSettings() {
        if (isAdded) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

}