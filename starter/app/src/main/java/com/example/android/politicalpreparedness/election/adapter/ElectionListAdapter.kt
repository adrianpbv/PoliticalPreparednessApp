package com.example.android.politicalpreparedness.election.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.databinding.ElectionItemBinding
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionEntity

class ElectionListAdapter(private val clickListener: ElectionClickListener):
    ListAdapter<Election, ElectionViewHolder>(ElectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        return ElectionViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, item)
    }

    //TODO: Add companion object to inflate ViewHolder (from)
}
class ElectionViewHolder private constructor(val binding: ElectionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(clickListener: ElectionClickListener, item: Election) {
        binding.clickListener = clickListener
        binding.election = item
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ElectionViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ElectionItemBinding.inflate(layoutInflater, parent, false)

            return ElectionViewHolder(binding)
        }
    }
}
/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class ElectionDiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem == newItem
    }
}

/**
 * A clickListener to react when the user taps on an item.
 */
class ElectionClickListener(val clickListener: (electionId: Int) -> Unit) {
    fun onClick(election: Int) = clickListener(election)
}