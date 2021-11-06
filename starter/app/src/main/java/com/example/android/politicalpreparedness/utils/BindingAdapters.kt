package com.example.android.politicalpreparedness.utils

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.Election

/**
 * this binding adapter takes the user to browser app to search for information about an
 * election
 */
@BindingAdapter("linkVoteInfo")
fun voteInfoLink(view: View, uriAddress: String?) {
    uriAddress?.let {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriAddress))
        view.context.startActivity(intent)
    }
}

/**
 * Change the button state according to the saved [Election] state in the database
 */
@BindingAdapter("savedSate")
fun changeButtonState(buttonView: Button, isSaved: Boolean) {
    if (isSaved)
        buttonView.text = buttonView.context.getText(R.string.unfollow)
    else
        buttonView.text = buttonView.context.getText(R.string.follow)
}

/**
 * Inform the user if the election physical address is not available
 */
@BindingAdapter("addressText")
fun setAddressText(textView: TextView, address: Address?) {
    if (address == null) {
        textView.text = textView.context.getText(R.string.address_unavailable)
    } else
        textView.text = textView.context.getText(R.string.address)
}

/**
 * Binding adapter to hide the view if no content is provided
 */
@BindingAdapter("fadeView")
fun hideViewIfNoContent(view: View, any: Any?) {
    view.animate().cancel()
    if (any != null) {
        if (view.visibility == View.GONE)
            view.fadeIn()
    } else {
        if (view.visibility == View.VISIBLE)
            view.fadeOut()
    }
}

/**
 * Set the toolbar text according to the [Election]
 * If the Election is null or an error occurred the toolbar
 * should show an appropriate state by changing its text and color
 */
@BindingAdapter("toolbarText")
fun setToolbarText(toolbar: Toolbar, electionName: String?) {
    if (electionName != null) {
        toolbar.title = electionName
    } else {
        toolbar.title = toolbar.context.getText(R.string.election_toolbar_error)
        toolbar.setBackgroundResource(R.color.error_red)
    }
}

/**
 * Use this binding adapter to show and hide the views using boolean variables
 */
@BindingAdapter("android:fadeVisible")
fun setFadeVisible(view: View, visible: Boolean?) {
    if (view.tag == null) {
        view.tag = true
        view.visibility = if (visible == true) View.VISIBLE else View.GONE
    } else {
        view.animate().cancel()
        if (visible == true) {
            if (view.visibility == View.GONE)
                view.fadeIn()
        } else {
            if (view.visibility == View.VISIBLE)
                view.fadeOut()
        }
    }
    //Timber.i("***** SetFadeVisible %s *****", visible)
}

@BindingAdapter("hideView")
fun setIfVisible(view: View, visible: Boolean? = true) {
    visible?.let {
        if (visible) view.visibility = View.VISIBLE else view.visibility = View.GONE
    }
}


/**
 * [BindingAdapter]s for the [Election]s list.
 */
@BindingAdapter("app:electItems")
fun setElectionItems(listView: RecyclerView, items: List<Election>?) {
    items?.let {
        (listView.adapter as ElectionListAdapter).submitList(items)
    }
}


/**
 * [BindingAdapter] for setting up the representative's photo
 */
@BindingAdapter("setPhoto")
fun setRepresentativePhoto(imageView: ImageView, photoUrl: String?) {
    photoUrl?.let {
        val imgUri = photoUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imageView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imageView)
    }
}