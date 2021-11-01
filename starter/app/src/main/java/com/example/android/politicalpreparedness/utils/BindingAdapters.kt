package com.example.android.politicalpreparedness.utils

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toolbar
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.network.models.Election


object BindingAdapters {

    /**
     * this binding adapter takes the user to browser app to search for information about an
     * election
     */
    @BindingAdapter("linkVoteInfo")
    @JvmStatic
    fun voteInfoLink(view: View, uriAddress: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriAddress))
        view.context.startActivity(intent)
    }

    /**
     * Change the button state according to the saved [Election] state in the database
     */
    @BindingAdapter("savedSate")
    @JvmStatic
    fun changeButtonState(buttonView: Button, isSaved: Boolean){
        if (isSaved)
            buttonView.text = buttonView.context.getText(R.string.unfollow)
        else
            buttonView.text = buttonView.context.getText(R.string.follow)
    }

    /**
     * Binding adapter to hide the view if no content is provided
     */
    @BindingAdapter("fadeView")
    @JvmStatic
    fun hideViewIfNoContent(view: View, any: Any?){
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
     * If the Election is null or an error ocurred the toolbar
     * should show an appropriate state by changing its text and color
     */
    @BindingAdapter("toolbarText")
    @JvmStatic
    fun setToolbarText(toolbar: Toolbar, electionName: String?){
        if (electionName != null){
            toolbar.title = electionName
        }else{
            toolbar.title = toolbar.context.getText(R.string.election_toolbar_error)
            toolbar.setBackgroundResource(R.color.error_red)
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
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
    }


    /**
     * [BindingAdapter]s for the [Election]s list.
     */
    @BindingAdapter("app:electItems")
    @JvmStatic
    fun setElectionItems(listView: RecyclerView, items: List<Election>?) {
        items?.let {
            (listView.adapter as ElectionListAdapter).submitList(items)
        }
    }


    /**
     * [BindingAdapter] for setting up the representative's photo
     */
    @BindingAdapter("setPhoto")
    fun setRepresentativePhoto(imageView: ImageView, photoUrl: String?){
        photoUrl?.let {
            val imgUri = photoUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imageView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_broken_image))
                .into(imageView)
        }
    }
}