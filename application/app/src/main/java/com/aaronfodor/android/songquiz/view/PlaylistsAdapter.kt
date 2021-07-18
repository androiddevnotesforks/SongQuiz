package com.aaronfodor.android.songquiz.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.PlaylistItemBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.view.utils.DrawableCrossFadeFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

class PlaylistsAdapter(private val context: Context,
                       private val onMainAction: (Playlist) -> Unit,
                       private val onMainText: String,
                       private val onSecondaryAction: (Playlist) -> Unit,
                       private val onSecondaryIcon: Drawable?,
                       private val onLastItemReached: () -> Unit) :
        ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    /* ViewHolder for Playlist, takes the view binding, the click behaviors, and the context. */
    class PlaylistViewHolder(val itemBinding: PlaylistItemBinding,
                             val context: Context,
                             val onMainAction: (Playlist) -> Unit,
                             val onMainText: String,
                             val onSecondaryAction: (Playlist) -> Unit,
                             val onSecondaryIcon: Drawable?) :
            RecyclerView.ViewHolder(itemBinding.root){

        val imageSize = context.resources.getDimension(R.dimen.list_item_image_pixels).toInt()

        fun bind(playlist: Playlist){
            itemBinding.playlistItemLayout.setOnClickListener {
                onMainAction(playlist)
            }
            itemBinding.primaryAction.setOnClickListener {
                onMainAction(playlist)
            }
            itemBinding.secondaryAction.setOnClickListener {
                onSecondaryAction(playlist)
            }

            itemBinding.primaryAction.text = onMainText
            onSecondaryIcon?.let {
                itemBinding.secondaryAction.background = it
            }

            itemBinding.playlistTitle.text = playlist.name
            itemBinding.playlistDescription.text = playlist.description
            if(playlist.owner.isNotBlank()){
                itemBinding.playlistOwner.text = context.getString(R.string.owner_text, playlist.owner)
            }

            val options = RequestOptions()
                .centerCrop()
                // smaller image: only 2 bytes per pixel
                .format(DecodeFormat.PREFER_RGB_565)
                // specific, small image needed as thumbnail
                .override(imageSize, imageSize)
                .placeholder(R.drawable.icon_album)
                .error(R.drawable.icon_album)

            Glide.with(itemBinding.playlistImage)
                .load(playlist.previewImageUri)
                .transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory()))
                .apply(options)
                .into(itemBinding.playlistImage)
        }

    }

    /* Creates and inflates view and return PlaylistViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistViewHolder{
        val itemBinding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(itemBinding, context, onMainAction, onMainText, onSecondaryAction, onSecondaryIcon)
    }

    /* Get current playlist and use it to bind view. */
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int){
        // last item reached, invoke callback
        if(position == itemCount-1){
            onLastItemReached()
        }

        val playlist = getItem(position)
        holder.bind(playlist)
    }

    private fun setAppearingAnimation(viewToAnimate: View){
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        viewToAnimate.startAnimation(animation)
    }

    override fun onViewRecycled(holder: PlaylistViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemBinding.playlistImage).clear(holder.itemBinding.playlistImage)
    }

    override fun onViewAttachedToWindow(holder: PlaylistViewHolder) {
        setAppearingAnimation(holder.itemView)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: PlaylistViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

}

object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>(){
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }
}