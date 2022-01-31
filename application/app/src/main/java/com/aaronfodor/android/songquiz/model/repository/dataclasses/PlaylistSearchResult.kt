package com.aaronfodor.android.songquiz.model.repository.dataclasses

data class PlaylistSearchResult(
        val items: List<Playlist> = listOf(),
        val searchExpression: String = "",
        val limit: Int = 0,
        val offset: Int = 0,
        val total: Int = 0
)