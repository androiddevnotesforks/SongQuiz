package com.arpadfodor.android.songquiz.model.repository

import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistsDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.TrackDTO
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.SearchResult
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Track

fun Playlist.toDbPlaylist() : DbPlaylist {
    return DbPlaylist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        previewImageUri = this.previewImageUri,
        type = this.type,
        uri = this.uri
    )
}

fun DbPlaylist.toPlaylist() : Playlist {
    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        previewImageUri = this.previewImageUri,
        type = this.type,
        uri = this.uri,
        tracks = arrayListOf()
    )
}

fun PlaylistDTO.toPlaylist() : Playlist {

    val tracks = mutableListOf<Track>()
    if(this.tracks != null){
        if(this.tracks.items != null){

            for(apiTrackWrapper in this.tracks.items){
                if(apiTrackWrapper.track.preview_url?.isNotBlank() == true){
                    tracks.add(apiTrackWrapper.track.toTrack())
                }
            }

        }
    }

    var previewUri = ""
    if(!this.images.isNullOrEmpty()){
        previewUri = this.images.random().url ?: ""
    }

    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        owner = this.owner?.display_name ?: "",
        previewImageUri = previewUri,
        followers = this.followers?.total ?: 0,
        type = this.type ?: "",
        uri = this.uri ?: "",
        primary_color = this.primary_color ?: "",
        tracks = tracks
    )

}

fun TrackDTO.toTrack() : Track{
    val artists = mutableListOf<String>()
    for(artist in this.artists){
        artists.add(artist.name)
    }

    return Track(
        id = this.id,
        name = this.name,
        artists = artists,
        album = this.album?.name?: "",
        durationMs = this.duration_ms,
        uri = this.uri ?: "",
        popularity = this.popularity ?: 0,
        previewUri = this.preview_url ?: "",
        type = this.type ?: ""
    )
}

fun PlaylistsDTO.toSearchResult(searchExpression: String) : SearchResult{
    val playlists = mutableListOf<Playlist>()
    for(item in this.items){
        playlists.add(item.toPlaylist())
    }

    return SearchResult(
        items = playlists,
        searchExpression = searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}