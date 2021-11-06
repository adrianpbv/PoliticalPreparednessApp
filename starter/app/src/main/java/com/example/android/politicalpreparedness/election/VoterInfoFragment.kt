package com.example.android.politicalpreparedness.election

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.udacity.project4.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class VoterInfoFragment : BaseFragment() {
    private lateinit var voterInfoBinding: FragmentVoterInfoBinding
    private val args: VoterInfoFragmentArgs by navArgs()

    override lateinit var _viewModel: VoterInfoViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //TODO: Add ViewModel values and create ViewModel
        //TODO: Add binding values
        voterInfoBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_voter_info,
            container,
            false
        )
        //TODO: Populate voter info -- hide views without provided data.
        voterInfoBinding.lifecycleOwner = this

        val viewModelFactory = VoterInfoViewModelFactory(requireActivity().application, args.argElectionId)
        _viewModel = ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)
        voterInfoBinding.viewModel = _viewModel

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */
        voterInfoBinding.electionInfoUrl.setOnClickListener {
            val uriAddress = _viewModel.electionAdministrationBody.value?.electionInfoUrl
            if (!uriAddress.isNullOrEmpty()) {
                openAnExternalLink(uriAddress)
            }else{
                _viewModel.showErrorMessage.value = R.string.link_error
            }
            _viewModel.checkPopulatedData()
        }

        voterInfoBinding.ballotInfoUrl.setOnClickListener {
            val uriAddress = _viewModel.electionAdministrationBody.value?.ballotInfoUrl
            if (!uriAddress.isNullOrEmpty()) {
                openAnExternalLink(uriAddress)
            }else{
                _viewModel.showErrorMessage.value = R.string.link_error
            }
            Timber.i(" ****** Debugging ****** %s",_viewModel.election.value?.getFormattedDate())
        }

        //TODO: Handle loading of URLs

        //TODO: Handle save button UI state
        //TODO: cont'd Handle save button clicks
        return voterInfoBinding.root
    }

    private fun openAnExternalLink(url: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    //TODO: Create method to load URL intents

}