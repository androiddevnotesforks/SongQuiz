package com.arpadfodor.android.songquiz.model.repository

import com.arpadfodor.android.songquiz.model.api.ApiService
import com.arpadfodor.android.songquiz.model.database.PlaylistDAO
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class PlaylistsRepository @Inject constructor(
    val dao: PlaylistDAO,
    val apiService: ApiService
) {

    fun addPlaylistById(id: Int){
        val result = apiService.getPlaylistById(id)
        //val toInsert = result.toPlaylist().toDbPlaylist()
        val toInsert = Playlist(1).toDbPlaylist()
        dao.insert(toInsert)
    }

}