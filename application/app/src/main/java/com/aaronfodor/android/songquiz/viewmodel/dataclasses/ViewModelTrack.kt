package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track
import com.aaronfodor.android.songquiz.view.utils.Listable

class ViewModelTrack (
    var id: String,
    val name: String = "",
    val artists: List<String> = arrayListOf(),
    val album: String = "",
    val imageUri: String = "",
    val durationMs: Int = 0,
    val uri: String = "",
    val popularity: Int = 0,
    val previewUrl: String = ""
)

fun Track.toViewModelTrack() : ViewModelTrack {
    return ViewModelTrack(
        id = this.id,
        name = this.name,
        artists = this.artists,
        album = this.album,
        imageUri = this.imageUri,
        durationMs = this.durationMs,
        uri = this.uri,
        popularity = this.popularity,
        previewUrl = this.previewUrl
    )
}

fun ViewModelTrack.toTrack() : Track {
    return Track(
        id = this.id,
        name = this.name,
        artists = this.artists,
        album = this.album,
        imageUri = this.imageUri,
        durationMs = this.durationMs,
        uri = this.uri,
        popularity = this.popularity,
        previewUrl = this.previewUrl
    )
}

fun ViewModelTrack.toListable() : Listable {
    return Listable(
        id = this.id,
        title = this.name,
        content1 = this.artists.toString(),
        content2 = this.popularity.toString(),
        imageUri = this.imageUri
    )
}