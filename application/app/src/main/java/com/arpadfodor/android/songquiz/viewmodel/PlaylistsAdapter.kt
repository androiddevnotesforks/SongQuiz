package com.arpadfodor.android.songquiz.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist

class PlaylistsAdapter(private val onStart: (Playlist) -> Unit, private val onDelete: (Playlist) -> Unit) :
        ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    /* ViewHolder for Playlist, takes in the inflated view and the onClick behavior. */
    class PlaylistViewHolder(itemView: View, val onStart: (Playlist) -> Unit, val onDelete: (Playlist) -> Unit) :
            RecyclerView.ViewHolder(itemView){

        private val playlistTitle: TextView = itemView.findViewById(R.id.playlist_title)
        private val playlistDescription: TextView = itemView.findViewById(R.id.playlist_description)
        private val playlistImage: ImageView = itemView.findViewById(R.id.playlist_image)
        private val playlistStartButton: ImageButton = itemView.findViewById(R.id.playlist_start)
        private val playlistDeleteButton: ImageButton = itemView.findViewById(R.id.playlist_delete)
        private var currentPlaylist: Playlist? = null

        init {

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