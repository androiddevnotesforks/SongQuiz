package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PlaylistSearchResult

class ViewModelPlaylistSearchResult(
    val items: List<ViewModelPlaylist> = listOf(),
    val searchExpression: String = "",
    val limit: Int = 0,
    val offset: Int = 0,
    val total: Int = 0
)

fun PlaylistSearchResult.toViewModelPlaylistSearchResult() : ViewModelPlaylistSearchResult {
    val convertedPlaylists = mutableListOf<ViewModelPlaylist>()
    for(item in this.items){
        convertedPlaylists.add(item.toViewModelPlaylist())
    }

    return ViewModelPlaylistSearchResult(
        items = convertedPlaylists,
        searchExpression = this.searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}

fun ViewModelPlaylistSearchResult.toPlaylistSearchResult() : PlaylistSearchResult {
    val convertedPlaylists = mutableListOf<Playlist>()
    for(playlist in this.items){
        convertedPlaylists.add(playlist.toPlaylist())
    }

    return PlaylistSearchResult(
        items = convertedPlaylists,
        searchExpression = this.searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}

fun ViewModelPlaylistSearchResult.removeIds(removeIds: List<String>) : ViewModelPlaylistSearchResult {
    val clearedPlaylist = items.filter { !removeIds.contains(it.id) }

    return ViewModelPlaylistSearchResult(
        items = clearedPlaylist,
        searchExpression = this.searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}