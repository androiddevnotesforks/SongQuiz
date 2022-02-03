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

enum class HomeUiState{
    LOADING, READY, AUTH_NEEDED, START_QUIZ
}

enum class HomeNotification{
    NONE, ERROR_DELETE_PLAYLIST, SUCCESS_DELETE_PLAYLIST
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    val accountService: AccountService
) : ViewModel() {

    companion object{
        var notificationFromCaller = HomeNotification.NONE

        fun getInitialNotification() : HomeNotification {
            val value = notificationFromCaller
            notificationFromCaller = HomeNotification.NONE
            return value
        }
    }

    val playlists : MutableLiveData<List<ViewModelPlaylist>> by lazy {
        MutableLiveData<List<ViewModelPlaylist>>()
    }

    val uiState: MutableLiveData<HomeUiState> by lazy {
        MutableLiveData<HomeUiState>()
    }

    val notification: MutableLiveData<HomeNotification> by lazy {
        MutableLiveData<HomeNotification>(getInitialNotification())
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(HomeUiState.LOADING)
        playlists.postValue(repository.getPlaylists().map { it.toViewModelPlaylist() })
        uiState.postValue(HomeUiState.READY)
    }

    fun deletePlaylist(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val success = repository.deletePlaylistById(id)
        if(success){
            notification.postValue(HomeNotification.SUCCESS_DELETE_PLAYLIST)
        }
        else{
            notification.postValue(HomeNotification.ERROR_DELETE_PLAYLIST)
        }

        val newList = repository.getPlaylists().map { it.toViewModelPlaylist() }
        playlists.postValue(newList)
    }

    fun startQuiz() = viewModelScope.launch {
        if(accountService.accountState.value != AccountState.LOGGED_IN){
            uiState.value = HomeUiState.AUTH_NEEDED
            return@launch
        }

        uiState.value = HomeUiState.START_QUIZ
    }

    fun ready() = viewModelScope.launch {
        uiState.value = HomeUiState.READY
    }

}