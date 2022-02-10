package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track
import com.aaronfodor.android.songquiz.view.utils.Listable
import kotlin.math.roundToInt

class ViewModelPlaylist (
    val id: String,
    val name: String = "",
    val description: String = "",
    val owner: String = "",
    val previewImageUri: String = "",
    val followers: Int = 0,
    val type: String = "",
    val uri: String = "",
    val primary_color: String = "",
    val tracks: MutableList<ViewModelTrack> = arrayListOf()
)

fun Playlist.toViewModelPlaylist() : ViewModelPlaylist {
    val convertedTracks = mutableListOf<ViewModelTrack>()
    for(track in this.tracks){
        convertedTracks.add(track.toViewModelTrack())
    }

    return ViewModelPlaylist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        previewImageUri = this.previewImageUri,
        followers = this.followers,
        type = this.type,
        uri = this.uri,
        primary_color = this.primary_color,
        tracks = convertedTracks
    )
}

fun ViewModelPlaylist.toPlaylist() : Playlist {
    val convertedTracks = mutableListOf<Track>()
    for(track in this.tracks){
        convertedTracks.add(track.toTrack())
    }

    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        previewImageUri = this.previewImageUri,
        followers = this.followers,
        type = this.type,
        uri = this.uri,
        primary_color = this.primary_color,
        tracks = convertedTracks
    )
}

fun ViewModelPlaylist.toListable() : Listable {
    return Listable(
        id = this.id,
        title = this.name,
        content1 = this.owner,
        content2 = this.description,
        imageUri = this.previewImageUri
    )
}

fun ViewModelPlaylist.getDifficulty() : Int {
    var sumPopularity = 0
    var validItems = 0

    for(track in this.tracks){
        if(track.popularity > 0){
            sumPopularity += track.popularity
            validItems += 1
        }
    }

    var difficulty = 0
    if(sumPopularity > 0 && validItems > 0){
        difficulty = 100 - (sumPopularity.toDouble() / validItems.toDouble()).roundToInt()
    }

    return difficulty
}