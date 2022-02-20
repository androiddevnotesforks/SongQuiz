package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.*
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsAddUiState{
    LOADING, READY
}

enum class PlaylistsAddNotification{
    NONE, DISCARDED, ERROR_ADD_PLAYLIST, SUCCESS_ADD_PLAYLIST, PLAYLIST_ALREADY_ADDED, NOT_FOUND
}

@HiltViewModel
class PlaylistsAddViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    companion object{
        var notificationFromCaller = PlaylistsAddNotification.NONE

        fun getInitialNotification() : PlaylistsAddNotification {
            val value = notificationFromCaller
            notificationFromCaller = PlaylistsAddNotification.NONE
            return value
        }
    }

    val callerType = InfoPlaylistScreenCaller.ADD_PLAYLIST.name
    var lastSearchExpression = ""
    var playlistIdsAlreadyAdded = mutableListOf<String>()

    val searchResult : MutableLiveData<ViewModelPlaylistSearchResult> by lazy {
        MutableLiveData<ViewModelPlaylistSearchResult>()
    }

    val uiState: MutableLiveData<PlaylistsAddUiState> by lazy {
        MutableLiveData<PlaylistsAddUiState>()
    }

    val notification: MutableLiveData<PlaylistsAddNotification> by lazy {
        MutableLiveData<PlaylistsAddNotification>(getInitialNotification())
    }

    init {
        viewModelScope.launch {
            uiState.value = PlaylistsAddUiState.READY
        }
    }

    fun setPlaylistIdsAlreadyAdded() = viewModelScope.launch(Dispatchers.IO) {
        playlistIdsAlreadyAdded = repository.getPlaylists().map{it.id}.toMutableList()
        searchResult.postValue(searchResult.value?.removeIds(playlistIdsAlreadyAdded))
    }

    fun searchPlaylist(searchExpression: String) = tryAuthenticateLaunch{
        searchPlaylistByIdOrName(searchExpression)
    }

    private fun searchPlaylistByIdOrName(searchExpression: String) = viewModelScope.launch(Dispatchers.IO) {
        lastSearchExpression = searchExpression
        uiState.postValue(PlaylistsAddUiState.LOADING)
        val result = repository.searchPlaylistByIdOrName(searchExpression).toViewModelPlaylistSearchResult()
        searchResult.postValue(result.removeIds(playlistIdsAlreadyAdded))

        if(result.items.isEmpty()){
            notification.postValue(PlaylistsAddNotification.NOT_FOUND)
        }
        uiState.postValue(PlaylistsAddUiState.READY)
    }

    fun getNextBatch() = tryAuthenticateLaunch{
        searchGetNextBatch()
    }

    private fun searchGetNextBatch() = viewModelScope.launch(Dispatchers.IO) {
        val currentResult = searchResult.value ?: return@launch

        uiState.postValue(PlaylistsAddUiState.LOADING)
        val searchBatch = repository.searchGetNextBatch(currentResult.toPlaylistSearchResult()).toViewModelPlaylistSearchResult()
        searchResult.postValue(searchBatch.removeIds(playlistIdsAlreadyAdded))
        uiState.postValue(PlaylistsAddUiState.READY)
    }

    fun addPlaylistById(id: String) = viewModelScope.launch(Dispatchers.IO) {
        if(playlistIdsAlreadyAdded.contains(id)){
            notification.postValue(PlaylistsAddNotification.PLAYLIST_ALREADY_ADDED)
            return@launch
        }

        val success = searchResult.value?.items?.let {
            repository.insertPlaylist(it.first{ item -> item.id == id }.toPlaylist())
        } ?: false

        if(success){
            notification.postValue(PlaylistsAddNotification.SUCCESS_ADD_PLAYLIST)
            playlistIdsAlreadyAdded.add(id)
            searchResult.postValue(searchResult.value?.removeIds(playlistIdsAlreadyAdded))
        }
        else{
            notification.postValue(PlaylistsAddNotification.ERROR_ADD_PLAYLIST)
        }
    }

}