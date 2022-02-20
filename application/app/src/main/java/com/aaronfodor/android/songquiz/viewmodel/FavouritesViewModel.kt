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
import javax.inject.Inject

enum class FavouritesUiState{
    LOADING, READY
}

enum class FavouritesNotification{
    NONE, ERROR_DELETE_TRACK, SUCCESS_DELETE_TRACK
}

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    val repository: PlaylistsRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    companion object{
        var notificationFromCaller = FavouritesNotification.NONE

        fun getInitialNotification() : FavouritesNotification {
            val value = notificationFromCaller
            notificationFromCaller = FavouritesNotification.NONE
            return value
        }
    }

    val callerType = InfoTrackScreenCaller.FAVOURITES.name

    val tracks : MutableLiveData<List<ViewModelPlaylist>> by lazy {
        MutableLiveData<List<ViewModelPlaylist>>()
    }

    val uiState: MutableLiveData<FavouritesUiState> by lazy {
        MutableLiveData<FavouritesUiState>()
    }

    val notification: MutableLiveData<FavouritesNotification> by lazy {
        MutableLiveData<FavouritesNotification>(getInitialNotification())
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(FavouritesUiState.LOADING)
        tracks.postValue(repository.getPlaylists().map { it.toViewModelPlaylist() })
        uiState.postValue(FavouritesUiState.READY)
    }

    fun deleteTrack(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val success = repository.deletePlaylistById(id)
        if(success){
            notification.postValue(FavouritesNotification.SUCCESS_DELETE_TRACK)
        }
        else{
            notification.postValue(FavouritesNotification.ERROR_DELETE_TRACK)
        }

        val newList = repository.getPlaylists().map { it.toViewModelPlaylist() }
        tracks.postValue(newList)
    }

}