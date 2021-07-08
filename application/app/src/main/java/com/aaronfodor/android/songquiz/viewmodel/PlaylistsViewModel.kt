package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsUiState{
    LOADING, READY, AUTH_NEEDED, START_QUIZ, SHOW_ADD_SCREEN
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    val accountService: AccountService
) : ViewModel() {

    val playlists : MutableLiveData<List<Playlist>> by lazy {
        MutableLiveData<List<Playlist>>()
    }

    val uiState: MutableLiveData<PlaylistsUiState> by lazy {
        MutableLiveData<PlaylistsUiState>()
    }

    init { viewModelScope.launch {
        loadData()
    } }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(PlaylistsUiState.LOADING)
        playlists.postValue(repository.getPlaylists())
        uiState.postValue(PlaylistsUiState.READY)
    }

    fun startQuiz() = viewModelScope.launch {
        if(accountService.accountState.value != AccountState.LOGGED_IN){
            uiState.value = PlaylistsUiState.AUTH_NEEDED
            return@launch
        }

        uiState.value = PlaylistsUiState.START_QUIZ
    }

    fun showAddPlaylistScreen() = viewModelScope.launch(Dispatchers.Default) {
        val playlistIdsAlreadyAdded : List<String> = playlists.value?.map { it -> it.id } ?: listOf()
        PlaylistsAddViewModel.transferPlaylistIdsAlreadyAdded = playlistIdsAlreadyAdded
        uiState.postValue(PlaylistsUiState.SHOW_ADD_SCREEN)
    }

    fun ready() = viewModelScope.launch {
        uiState.value = PlaylistsUiState.READY
    }

}