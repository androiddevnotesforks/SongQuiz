package com.arpadfodor.android.songquiz.model.repository.dataclasses

data class Playlist (
        val id: String,
        val name: String = "",
        val description: String = "",
        val previewImageUri: String = "",
        val followers: Int = 0,
        val type: String = "",
        val uri: String = "",
        val primary_color: String = "",
        val tracks: MutableList<Track> = arrayListOf()
)