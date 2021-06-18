package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.model.repository.dataclasses.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsAddUiState{
    LOADING, READY, NOT_FOUND, AUTH_NEEDED, ERROR_ADD_PLAYLIST, SUCCESS_ADD_PLAYLIST, PLAYLIST_ALREADY_ADDED
}

@HiltViewModel
class PlaylistsAddViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    val accountService: AccountService
) : ViewModel() {

    companion object{
        var transferPlaylistIdsAlreadyAdded : List<String> = listOf()
    }

    val searchResult : MutableLiveData<SearchResult> by lazy {
        MutableLiveData<SearchResult>()
    }

    val uiState: MutableLiveData<PlaylistsAddUiState> by lazy {
        MutableLiveData<PlaylistsAddUiState>()
    }

    var playlistIdsAlreadyAdded = mutableListOf<String>()

    init { viewModelScope.launch {
        uiState.value = PlaylistsAddUiState.READY
    } }

    fun setPlaylistIdsAlreadyAdded() = viewModelScope.launch {
        playlistIdsAlreadyAdded = transferPlaylistIdsAlreadyAdded.toMutableList()
    }

    fun searchPlaylistByIdOrName(searchExpression: String) = viewModelScope.launch(Dispatchers.IO) {
        if(accountService.accountState.value != AccountState.LOGGED_IN){
            uiState.postValue(PlaylistsAddUiState.AUTH_NEEDED)
            return@launch
        }

        uiState.postValue(PlaylistsAddUiState.LOADING)

        val result = repository.searchPlaylistByIdOrName(searchExpression)
        searchResult.postValue(result)

        if(result.items.isEmpty()){
            uiState.postValue(PlaylistsAddUiState.NOT_FOUND)
        }
        else{
            uiState.postValue(PlaylistsAddUiState.READY)
        }
    }

    fun searchGetNextBatch() = viewModelScope.launch(Dispatchers.IO) {
        val currentResult = searchResult.value ?: return@launch

        uiState.postValue(PlaylistsAddUiState.LOADING)
        searchResult.postValue(repository.searchGetNextBatch(currentResult))
        uiState.postValue(PlaylistsAddUiState.READY)
    }

    fun addPlaylistById(id: String) = viewModelScope.launch(Dispatchers.IO) {
        if(playlistIdsAlreadyAdded.contains(id)){
            uiState.postValue(PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED)
            return@launch
        }

        uiState.postValue(PlaylistsAddUiState.LOADING)
        val success = searchResult.value?.items?.let { repository.insertPlaylist(it.first{ item -> item.id == id }) } ?: false

        if(success){
            uiState.postValue(PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST)
            playlistIdsAlreadyAdded.add(id)
        }
        else{
            uiState.postValue(PlaylistsAddUiState.ERROR_ADD_PLAYLIST)
        }
    }

    fun ready() = viewModelScope.launch {
        uiState.value = PlaylistsAddUiState.READY
    }

}