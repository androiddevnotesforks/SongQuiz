package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

enum class HomeUiState{
    LOADING, READY
}

enum class HomeNotification{
    NONE, ERROR_DELETE_PLAYLIST, SUCCESS_DELETE_PLAYLIST, NO_PLAYLISTS, START_QUIZ, SHOW_ADD_SCREEN
}

enum class PartOfTheDay{
    UNKNOWN, MORNING, AFTERNOON, EVENING, NIGHT
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    companion object{
        var notificationFromCaller = HomeNotification.NONE

        fun getInitialNotification() : HomeNotification {
            val value = notificationFromCaller
            notificationFromCaller = HomeNotification.NONE
            return value
        }
    }

    val callerType = InfoPlaylistScreenCaller.HOME.name
    var selectedPlaylistId = ""

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

    fun play(playListId: String) = mustAuthenticatedLaunch {
            startQuiz(playListId)
    }

    private fun startQuiz(playListId: String) = viewModelScope.launch {
        selectedPlaylistId = playListId
        notification.postValue(HomeNotification.START_QUIZ)
    }

    fun startRandomQuiz() = viewModelScope.launch {
        val playlistsToPickFrom = playlists.value ?: listOf()

        if(playlistsToPickFrom.isNotEmpty()){
            val selectedPlaylist = playlistsToPickFrom.random()
            startQuiz(selectedPlaylist.id)
        }
        else{
            notification.postValue(HomeNotification.NO_PLAYLISTS)
        }
    }

    fun showAddPlaylistScreen() = viewModelScope.launch(Dispatchers.Default) {
        notification.postValue(HomeNotification.SHOW_ADD_SCREEN)
    }

    fun getPartOfTheDay() : PartOfTheDay{
        val currentTime = LocalTime.now()
        return when(currentTime.hour){
            in 5..11 -> {PartOfTheDay.MORNING}
            in 12..16 -> {PartOfTheDay.AFTERNOON}
            in 17..20 -> {PartOfTheDay.EVENING}
            in 21..24 -> {PartOfTheDay.NIGHT}
            in 0..4 -> {PartOfTheDay.NIGHT}
            else -> {PartOfTheDay.UNKNOWN}
        }
    }

}