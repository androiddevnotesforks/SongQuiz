package com.arpadfodor.android.songquiz.model.repository

import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiTrack
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Track

fun Playlist.toDbPlaylist() : DbPlaylist {
    return DbPlaylist(
            id = this.id,
            name = this.name,
            description = this.description,
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
            previewImageUri = this.previewImageUri,
            type = this.type,
            uri = this.uri,
            tracks = arrayListOf()
    )
}

fun ApiPlaylist.toPlaylist() : Playlist {

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

    return Playlist(
            id = this.id,
            name = this.name,
            description = this.description ?: "",
            previewImageUri = this.images?.get(0)?.url ?: "",
            followers = this.followers?.total ?: 0,
            type = this.type ?: "",
            uri = this.uri ?: "",
            primary_color = this.primary_color ?: "",
            tracks = tracks
    )

}

fun ApiTrack.toTrack() : Track{

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