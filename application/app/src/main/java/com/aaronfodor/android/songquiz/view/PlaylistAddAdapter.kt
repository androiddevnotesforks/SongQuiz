package com.aaronfodor.android.songquiz.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.PlaylistAddItemBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions

class PlaylistAddAdapter(private val context: Context, private val onAdd: (Playlist) -> Unit,
                         private val onLastItemReached: () -> Unit) :
        ListAdapter<Playlist, PlaylistAddAdapter.PlaylistAddViewHolder>(PlaylistAddDiffCallback) {

    /* ViewHolder for Playlist, takes the view binding, the onClick behavior, and the context. */
    class PlaylistAddViewHolder(val itemBinding: PlaylistAddItemBinding, private val context: Context,
                                val onAdd: (Playlist) -> Unit) :
        RecyclerView.ViewHolder(itemBinding.root){

        val imageSize = context.resources.getDimension(R.dimen.list_item_image_size).toInt()

        fun bind(playlist: Playlist){
            itemBinding.playlistItemLayout.setOnClickListener {
                onAdd(playlist)
            }
            itemBinding.playlistAdd.setOnClickListener {
                onAdd(playlist)
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
                .apply(options)
                .into(itemBinding.playlistImage)
        }

    }

    /* Creates and inflates view and return PlaylistViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistAddViewHolder{
        val itemBinding = PlaylistAddItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistAddViewHolder(itemBinding, context, onAdd)
    }

    /* Gets current playlist and uses it to bind view. */
    override fun onBindViewHolder(holder: PlaylistAddViewHolder, position: Int){
        // last item reached, invoke callback
        if(position == itemCount-1){
            onLastItemReached()
        }

        val playlist = getItem(position)
        holder.bind(playlist)
    }

    override fun onViewRecycled(holder: PlaylistAddViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemBinding.playlistImage).clear(holder.itemBinding.playlistImage)
    }

}

object PlaylistAddDiffCallback : DiffUtil.ItemCallback<Playlist>(){

    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

}