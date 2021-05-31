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
    private val dao: PlaylistDAO,
    private val apiService: ApiService
) {

    fun getPlaylists() : List<Playlist>{
        val dbPlaylists = dao.getAll() ?: listOf()
        val playlists = mutableListOf<Playlist>()
        for(item in dbPlaylists){
            playlists.add(item.toPlaylist())
        }
        return playlists.reversed()
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

    fun addPlaylist(playlist: Playlist) : Boolean{
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

    fun searchPlaylistsByIdOrName(searchExpression: String, offset: Int = 0) : List<Playlist>{
        val playlists = mutableListOf<Playlist>()

        if(searchExpression.isBlank()){
            return playlists
        }

        val apiPlaylists = apiService.getPlaylistsByIdOrName(searchExpression, offset).items
        for(apiPlaylist in apiPlaylists){
            playlists.add(apiPlaylist.toPlaylist())
        }
        return playlists
    }

}