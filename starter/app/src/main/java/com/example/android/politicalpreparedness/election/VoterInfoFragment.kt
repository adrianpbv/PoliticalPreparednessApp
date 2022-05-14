package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.udacity.project4.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf


class VoterInfoFragment : BaseFragment() {
    private lateinit var voterInfoBinding: FragmentVoterInfoBinding
    private val args: VoterInfoFragmentArgs by navArgs()
    override val _viewModel: VoterInfoViewModel by viewModel{ parametersOf(args.argElectionId)}


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        voterInfoBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_voter_info,
            container,
            false
        )

        voterInfoBinding.lifecycleOwner = this

        voterInfoBinding.viewModel = _viewModel

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
         */
        voterInfoBinding.electionInfoUrl.setOnClickListener {
            val uriAddress = _viewModel.electionAdministrationBody.value?.electionInfoUrl
            if (!uriAddress.isNullOrEmpty()) {
                openAnExternalLink(uriAddress)
            } else {
                _viewModel.showErrorMessage.value = R.string.link_error
            }
        }

        voterInfoBinding.ballotInfoUrl.setOnClickListener {
            val uriAddress = _viewModel.electionAdministrationBody.value?.ballotInfoUrl
            if (!uriAddress.isNullOrEmpty()) {
                openAnExternalLink(uriAddress)
            } else {
                _viewModel.showErrorMessage.value = R.string.link_error
            }
        }
        return voterInfoBinding.root
    }

    private fun openAnExternalLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}