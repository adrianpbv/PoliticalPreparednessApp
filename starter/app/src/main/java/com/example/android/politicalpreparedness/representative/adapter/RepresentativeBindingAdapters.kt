package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.representative.model.Representative

/**
 * [BindingAdapter]s for the [Representative]s list.
 */
@BindingAdapter("app:repItems")
fun setRepresentativeItems(listView: RecyclerView, items: List<Representative>?) {
    items?.let {
        (listView.adapter as RepresentativeListAdapter).submitList(items)
    }
}

@BindingAdapter("profileImage")
fun fetchImage(imageView: ImageView, src: String?) {
    src?.let {
        val imgUri = src.toUri().buildUpon().scheme("https").build()
        Glide.with(imageView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
            )
            .into(imageView)
        //TODO: Add Glide call to load image and circle crop - user ic_profile as a placeholder and for errors.
    }
}

@BindingAdapter("app:stateValue")
fun Spinner.setNewValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T> {
    return adapter as ArrayAdapter<T>
}
