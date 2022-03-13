package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.LoggerService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsUiState{
    LOADING, READY
}

enum class PlaylistsNotification{
    NONE, ERROR_DELETE_PLAYLIST, SUCCESS_DELETE_PLAYLIST, START_QUIZ, SHOW_ADD_SCREEN
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    val loggerService: LoggerService,
    accountService: AccountService
) : AppViewModel(accountService) {

    companion object{
        var notificationFromCaller = PlaylistsNotification.NONE

        fun getInitialNotification() : PlaylistsNotification {
            val value = notificationFromCaller
            notificationFromCaller = PlaylistsNotification.NONE
            return value
        }
    }

    val callerType = InfoPlaylistScreenCaller.PLAY.name
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
        val loadedPlaylists = repository.getPlaylists()
        uiState.postValue(PlaylistsUiState.READY)
        playlists.postValue(loadedPlaylists.map { it.toViewModelPlaylist() })
    }

    fun deletePlaylist(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val success = repository.deletePlaylistById(id)
        loggerService.logDeletePlaylist(id)
        if(success){
            notification.postValue(PlaylistsNotification.SUCCESS_DELETE_PLAYLIST)
        }
        else{
            notification.postValue(PlaylistsNotification.ERROR_DELETE_PLAYLIST)
        }

        val newList = repository.getPlaylists().map { it.toViewModelPlaylist() }
        playlists.postValue(newList)
    }

    fun startQuiz(playListId: String) = mustAuthenticatedLaunch {
        selectedPlaylistId = playListId
        loggerService.logShowQuizScreen(playListId)
        notification.postValue(PlaylistsNotification.START_QUIZ)
    }

    fun showAddPlaylistScreen() = viewModelScope.launch(Dispatchers.Default) {
        notification.postValue(PlaylistsNotification.SHOW_ADD_SCREEN)
    }

}