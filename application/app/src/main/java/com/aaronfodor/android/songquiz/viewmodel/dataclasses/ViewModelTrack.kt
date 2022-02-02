package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track
import com.aaronfodor.android.songquiz.view.Listable

class ViewModelTrack (
    var id: String,
    val name: String = "",
    val artists: List<String> = arrayListOf(),
    val album: String = "",
    val durationMs: Int = 0,
    val uri: String = "",
    val popularity: Int = 0,
    val previewUri: String = "",
    val type: String = "",
)

fun Track.toViewModelTrack() : ViewModelTrack {
    return ViewModelTrack(
        id = this.id,
        name = this.name,
        artists = this.artists,
        album = this.album,
        durationMs = this.durationMs,
        uri = this.uri,
        popularity = this.popularity,
        previewUri = this.previewUri,
        type = this.type
    )
}

fun ViewModelTrack.toTrack() : Track {
    return Track(
        id = this.id,
        name = this.name,
        artists = this.artists,
        album = this.album,
        durationMs = this.durationMs,
        uri = this.uri,
        popularity = this.popularity,
        previewUri = this.previewUri,
        type = this.type
    )
}

fun ViewModelTrack.toListable() : Listable {
    return Listable(
        id = this.id,
        title = this.name,
        content1 = this.artists.toString(),
        content2 = this.popularity.toString(),
        imageUri = this.previewUri
    )
}