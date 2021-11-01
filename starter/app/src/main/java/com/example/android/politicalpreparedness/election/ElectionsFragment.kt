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
import timber.log.Timber


class ElectionsFragment : BaseFragment() {

    override val _viewModel by viewModel<ElectionsViewModel>()
    private lateinit var dataBinding: FragmentElectionBinding

    private lateinit var listAdapter: ElectionListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //TODO: Add ViewModel values and create ViewModel

        //TODO: Add binding values
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)

       // dataBinding.lifecycleOwner = this

        dataBinding.viewModel = _viewModel


        //TODO: Populate recycler adapters
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.lifecycleOwner = this

        setupListAdapter()
        setupNavigation()
    }

    //TODO: Refresh adapters when fragment loads

    private fun setupListAdapter() {
        val viewModel = dataBinding.viewModel

        if (viewModel != null) {
            listAdapter = ElectionListAdapter(ElectionClickListener {
                viewModel.navigateToDetailsElection(it)
            })
            dataBinding.upcomingElectionRecyclerView.adapter = listAdapter
            dataBinding.savedElectionRecyclerView.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }

    private fun setupNavigation() {
        _viewModel.navigateDetailElections.observe(viewLifecycleOwner, Observer {
            _viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it)
                )
            )
        })
    }

}