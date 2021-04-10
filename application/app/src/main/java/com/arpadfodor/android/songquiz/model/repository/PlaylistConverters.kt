package com.arpadfodor.android.songquiz.model.repository

import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist

fun Playlist.toDbPlaylist() : DbPlaylist {
    return DbPlaylist(this.id)
}

fun DbPlaylist.toPlaylist() : Playlist {
    return Playlist(this.id)
}

fun Playlist.toApiPlaylist() : ApiPlaylist {
    return ApiPlaylist(this.id)
}

fun ApiPlaylist.toPlaylist() : Playlist {
    return Playlist(this.id)
}