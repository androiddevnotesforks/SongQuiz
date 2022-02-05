package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsUiState{
    LOADING, READY, AUTH_NEEDED, START_QUIZ, SHOW_ADD_SCREEN
}

enum class PlaylistsNotification{
    NONE, ERROR_DELETE_PLAYLIST, SUCCESS_DELETE_PLAYLIST
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    val accountService: AccountService
) : ViewModel() {

    companion object{
        var notificationFromCaller = PlaylistsNotification.NONE

        fun getInitialNotification() : PlaylistsNotification {
            val value = notificationFromCaller
            notificationFromCaller = PlaylistsNotification.NONE
            return value
        }
    }

    var selectedPlaylistId = ""

    val playlists : MutableLiveData<List<ViewModelPlaylist>> by lazy {
        MutableLiveData<List<ViewModelPlaylist>>()
    }

    val uiState: MutableLiveData<PlaylistsUiState> by lazy {
        MutableLiveData<PlaylistsUiState>()
    }

    val notification: MutableLiveData<PlaylistsNotification> by lazy {
        MutableLiveData<PlaylistsNotification>(getInitialNotification())
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(PlaylistsUiState.LOADING)
        playlists.postValue(repository.getPlaylists().map { it.toViewModelPlaylist() })
        uiState.postValue(PlaylistsUiState.READY)
    }

    fun deletePlaylist(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val success = repository.deletePlaylistById(id)
        if(success){
            notification.postValue(PlaylistsNotification.SUCCESS_DELETE_PLAYLIST)
        }
        else{
            notification.postValue(PlaylistsNotification.ERROR_DELETE_PLAYLIST)
        }

        val newList = repository.getPlaylists().map { it.toViewModelPlaylist() }
        playlists.postValue(newList)
    }

    fun startQuiz(playListId: String) = viewModelScope.launch {
        selectedPlaylistId = playListId
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