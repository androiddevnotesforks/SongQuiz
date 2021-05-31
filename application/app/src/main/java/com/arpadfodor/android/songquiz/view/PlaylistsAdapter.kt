package com.arpadfodor.android.songquiz.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.PlaylistItemBinding
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PlaylistsAdapter(private val context: Context,
                       private val onStart: (Playlist) -> Unit, private val onDelete: (Playlist) -> Unit) :
        ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    /* ViewHolder for Playlist, takes the view binding, the onClick behavior, and the context. */
    class PlaylistViewHolder(private val itemBinding: PlaylistItemBinding, private val context: Context,
                             val onStart: (Playlist) -> Unit, val onDelete: (Playlist) -> Unit) :
            RecyclerView.ViewHolder(itemBinding.root){

        fun bind(playlist: Playlist){
            itemBinding.playlistItemLayout.setOnClickListener {
                onStart(playlist)
            }
            itemBinding.playlistStart.setOnClickListener {
                onStart(playlist)
            }
            itemBinding.playlistDelete.setOnClickListener {
                onDelete(playlist)
            }

            itemBinding.playlistTitle.text = playlist.name
            itemBinding.playlistDescription.text = playlist.description
            if(playlist.owner.isNotBlank()){
                itemBinding.playlistOwner.text = context.getString(R.string.owner_text, playlist.owner)
            }

            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.icon_album)
                .error(R.drawable.icon_album)
            Glide.with(itemBinding.playlistImage).load(playlist.previewImageUri).apply(options).into(itemBinding.playlistImage)
        }

    }

    /* Creates and inflates view and return PlaylistViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistViewHolder{
        val itemBinding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(itemBinding, context, onStart, onDelete)
    }

    /* Gets current playlist and uses it to bind view. */
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int){
        val playlist = getItem(position)
        holder.bind(playlist)
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