package com.aaronfodor.android.songquiz.model.repository

import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistsDTO
import com.aaronfodor.android.songquiz.model.api.dataclasses.TrackDTO
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PlaylistSearchResult
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track

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

    this.tracks?.items?.let {
        for(apiTrackWrapper in it){
            apiTrackWrapper.track?.preview_url?.let { previewUrl ->
                if(previewUrl.isNotBlank()){
                    val currentTrack = apiTrackWrapper.track.toTrack()
                    tracks.add(currentTrack)
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
    this.artists?.let {
        for(artist in it){
            artists.add(artist.name)
        }
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

fun PlaylistsDTO.toSearchResult(searchExpression: String) : PlaylistSearchResult{
    val playlists = mutableListOf<Playlist>()
    for(item in this.items){
        playlists.add(item.toPlaylist())
    }

    return PlaylistSearchResult(
        items = playlists,
        searchExpression = searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}