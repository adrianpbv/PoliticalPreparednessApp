package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionClickListener
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import org.koin.androidx.viewmodel.ext.android.viewModel


class ElectionsFragment : BaseFragment() {

    override val _viewModel: ElectionsViewModel by viewModel()
    private lateinit var dataBinding: FragmentElectionBinding

    private lateinit var listAdapterOne: ElectionListAdapter
    private lateinit var listAdapterTwo: ElectionListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)

        dataBinding.lifecycleOwner = this

        dataBinding.viewModel = _viewModel

        listAdapterOne = ElectionListAdapter(ElectionClickListener {
            _viewModel.navigateToDetailsElection(it)
        })
        listAdapterTwo = ElectionListAdapter(ElectionClickListener {
            _viewModel.navigateToDetailsElection(it)
        })

        dataBinding.upcomingElectionRecyclerView.adapter = listAdapterOne
        dataBinding.savedElectionRecyclerView.adapter = listAdapterTwo

        setupNavigation()
        return dataBinding.root
    }

    private fun setupNavigation() {
        _viewModel.navigateDetailElections.observe(viewLifecycleOwner, Observer {
            it?.let {
                _viewModel.navigationCommand.postValue(
                    NavigationCommand.To(
                        ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it)
                    )
                )
                _viewModel.navigationDetailElectionCompleted()
            }

        })
    }

}