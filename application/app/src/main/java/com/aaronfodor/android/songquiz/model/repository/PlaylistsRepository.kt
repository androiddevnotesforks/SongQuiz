package com.aaronfodor.android.songquiz.model.repository

import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.TimeService
import com.aaronfodor.android.songquiz.model.api.SpotifyApiService
import com.aaronfodor.android.songquiz.model.database.PlaylistDAO
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PlaylistSearchResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class PlaylistsRepository @Inject constructor(
    private val dao: PlaylistDAO,
    private val spotifyApiService: SpotifyApiService,
    private val accountService: AccountService,
    private val timeService: TimeService
) {

    fun getPlaylists() : List<Playlist>{

        // if logged out, show defaults
        val dbPlaylists = if(accountService.accountState.value == AccountState.LOGGED_OUT){
            dao.getAll(AccountService.DEFAULTS_ACCOUNT_ID) ?: listOf()
        }
        // if logged in, load playlists
        else{
            var loadedPlaylists = dao.getAll(accountService.getAccountId()) ?: listOf()
            val isFirstLoadAfterLogin = accountService.getPublicInfo().isFirstLoadAfterLogin

            // if this is the first load after login, and there are no playlists
            if(isFirstLoadAfterLogin && loadedPlaylists.isEmpty()){
                // set defaults to user
                restoreDefaultPlaylists()
                loadedPlaylists = dao.getAll(accountService.getAccountId()) ?: listOf()
            }

            if(isFirstLoadAfterLogin){
                // first load after login has finished
                accountService.firstLoadFinishedAfterLogin()
            }

            loadedPlaylists
        }

        val playlists = mutableListOf<Playlist>()
        for(item in dbPlaylists){
            playlists.add(item.toPlaylist())
        }
        return playlists.reversed()
    }


    fun getPlaylistById(id: String) : Playlist{
        val dbPlaylists = dao.getById(id, accountService.getAccountId()) ?: listOf()

        return if(dbPlaylists.isNotEmpty()){
            dbPlaylists[0].toPlaylist()
        }
        else{
            Playlist("")
        }
    }

    fun insertPlaylistById(id: String) : Boolean{
        val result = spotifyApiService.getPlaylistById(id)
        if(result.id == ""){
            return false
        }
        val playlist = result.toPlaylist()
        val toInsert = playlist.toDbPlaylist(accountService.getAccountId(), timeService.getTimestampUTC())
        dao.insert(toInsert)
        return true
    }

    fun insertPlaylist(playlist: Playlist) : Boolean{
        val toInsert = playlist.toDbPlaylist(accountService.getAccountId(), timeService.getTimestampUTC())
        dao.insert(toInsert)
        return true
    }

    fun updatePlaylist(playlist: Playlist) : Boolean{
        val toUpdate = playlist.toDbPlaylist(accountService.getAccountId(), timeService.getTimestampUTC())
        dao.update(toUpdate)
        return true
    }

    fun deletePlaylistById(id: String) : Boolean{
        dao.delete(id, accountService.getAccountId())
        return true
    }

    fun deleteAllPlaylists() : Boolean{
        dao.deleteAll(accountService.getAccountId())
        return true
    }

    fun restoreDefaultPlaylists() : Boolean{
        dao.deleteAll(accountService.getAccountId())
        val defaults = dao.getAll(AccountService.DEFAULTS_ACCOUNT_ID) ?: listOf()

        if(accountService.accountState.value == AccountState.LOGGED_IN){
            val currentAccountId = accountService.getAccountId()
            defaults.forEach { it.accountId = currentAccountId }
            dao.insert(defaults)
        }

        return true
    }

    fun downloadPlaylistById(id: String) : Playlist{
        val apiPlaylist = spotifyApiService.getPlaylistById(id)
        return apiPlaylist.toPlaylist()
    }

    fun searchPlaylistByIdOrName(searchExpression: String, offset: Int = 0) : PlaylistSearchResult{
        val rawResults = spotifyApiService.getPlaylistsByIdOrName(searchExpression, offset)
        return rawResults.toSearchResult(searchExpression)
    }

    fun searchGetNextBatch(currentResult: PlaylistSearchResult) : PlaylistSearchResult{
        val offset = currentResult.offset + currentResult.limit
        if(offset > currentResult.total){
            return currentResult
        }

        val nextResults = spotifyApiService.getPlaylistsByIdOrName(currentResult.searchExpression, offset).toSearchResult(currentResult.searchExpression)
        return PlaylistSearchResult(
            items = currentResult.items + nextResults.items,
            searchExpression = nextResults.searchExpression,
            // maxOf needed, as Spotify API retrieves 0s after the 1000th offset
            limit = maxOf(nextResults.limit, currentResult.limit),
            offset = maxOf(nextResults.offset, currentResult.offset),
            total = maxOf(nextResults.total, currentResult.total)
        )
    }

}