package com.arpadfodor.android.songquiz.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.view.utils.AppButton
import com.arpadfodor.android.songquiz.view.utils.AppPositiveButton
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PlaylistsAdapter(private val onStart: (Playlist) -> Unit, private val onDelete: (Playlist) -> Unit) :
        ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    /* ViewHolder for Playlist, takes in the inflated view and the onClick behavior. */
    class PlaylistViewHolder(itemView: View, val onStart: (Playlist) -> Unit, val onDelete: (Playlist) -> Unit) :
            RecyclerView.ViewHolder(itemView){

        private val playlistLayout: ConstraintLayout = itemView.findViewById(R.id.playlist_item_layout)
        private val playlistTitle: TextView = itemView.findViewById(R.id.playlist_title)
        private val playlistDescription: TextView = itemView.findViewById(R.id.playlist_description)
        private val playlistImage: ImageView = itemView.findViewById(R.id.playlist_image)
        private val playlistStartButton: AppPositiveButton = itemView.findViewById(R.id.playlist_start)
        private val playlistDeleteButton: AppButton = itemView.findViewById(R.id.playlist_delete)
        private var currentPlaylist: Playlist? = null

        init {
            playlistLayout.setOnClickListener {
                currentPlaylist?.let {
                    onStart(it)
                }
            }

            playlistStartButton.setOnClickListener {
                currentPlaylist?.let {
                    onStart(it)
                }
            }

            playlistDeleteButton.setOnClickListener {
                currentPlaylist?.let {
                    onDelete(it)
                }
            }
        }

        fun bind(playlist: Playlist){
            currentPlaylist = playlist

            playlistTitle.text = playlist.name
            playlistDescription.text = playlist.description

            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.icon_album)
                .error(R.drawable.icon_album)
            Glide.with(playlistImage).load(playlist.previewImageUri).apply(options).into(playlistImage)
        }
    }

    /* Creates and inflates view and return PlaylistViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PlaylistViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view, onStart, onDelete)
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