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

    fun getPlaylists() : List<Playlist>{
        val dbPlaylists = dao.getAll() ?: listOf()
        val playlists = mutableListOf<Playlist>()
        for(item in dbPlaylists){
            playlists.add(item.toPlaylist())
        }
        return playlists
    }

    fun addPlaylistById(id: String) : Boolean{
        val result = apiService.getPlaylistById(id)
        if(result.id == ""){
            return false
        }
        val playlist = result.toPlaylist()
        val toInsert = playlist.toDbPlaylist()
        dao.insert(toInsert)
        return true
    }

    fun updatePlaylist(playlist: Playlist) : Boolean{
        dao.insert(playlist.toDbPlaylist())
        return true
    }

    fun deletePlaylistById(id: String){
        dao.delete(id)
    }

    fun downloadPlaylistById(id: String) : Playlist{
        val apiPlaylist = apiService.getPlaylistById(id)
        return apiPlaylist.toPlaylist()
    }

}