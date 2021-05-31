package com.arpadfodor.android.songquiz.model.repository.dataclasses

data class PlaylistSearchResult(
        val items: Array<Playlist> = emptyArray(),
        val searchExpression: String = "",
        val limit: Int = 0,
        val offset: Int = 0,
        val total: Int = 0
)