package com.aaronfodor.android.songquiz.model.repository.dataclasses

data class Playlist (
        val id: String,
        val name: String = "",
        val description: String = "",
        val owner: String = "",
        val imageUri: String = "",
        val followers: Int = 0,
        val type: String = "",
        val uri: String = "",
        val primary_color: String = "",
        val tracks: MutableList<Track> = arrayListOf()
)