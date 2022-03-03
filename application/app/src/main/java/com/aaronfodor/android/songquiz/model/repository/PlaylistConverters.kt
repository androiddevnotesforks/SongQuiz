package com.aaronfodor.android.songquiz.model.repository

import androidx.core.text.HtmlCompat
import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistsDTO
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PlaylistSearchResult
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track

fun Playlist.toDbPlaylist(accountId: String, timestampUTC: String) : DbPlaylist {
    return DbPlaylist(
        id = this.id,
        accountId = accountId,
        timestampUTC = timestampUTC,
        name = this.name,
        description = this.description,
        owner = this.owner,
        imageUri = this.imageUri,
        uri = this.uri
    )
}

fun DbPlaylist.toPlaylist() : Playlist {
    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        imageUri = this.imageUri,
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

    var imageUri = ""
    var imageDim = 0
    val maxImageDim = 680
    this.images?.let {
        for(image in it){
            if(imageUri.isBlank() && image.url.isNotBlank()){
                imageUri = image.url
                imageDim = image.height
            }

            if((image.height in imageDim until maxImageDim) && image.url.isNotBlank()){
                imageUri = image.url
                imageDim = image.height
            }
        }
    }

    return Playlist(
        id = this.id,
        name = this.name,
        description = HtmlCompat.fromHtml(this.description ?: "", 0).toString(),
        owner = this.owner?.display_name ?: "",
        imageUri = imageUri,
        followers = this.followers?.total ?: 0,
        type = this.type ?: "",
        uri = this.uri ?: "",
        primary_color = this.primary_color ?: "",
        tracks = tracks
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