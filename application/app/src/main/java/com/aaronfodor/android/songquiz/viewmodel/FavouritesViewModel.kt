package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.MediaPlayerService
import com.aaronfodor.android.songquiz.model.repository.TracksRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelTrack
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelTrack
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FavouritesUiState{
    LOADING, READY
}

enum class FavouritesNotification{
    NONE, ERROR_DELETE_TRACK, SUCCESS_DELETE_TRACK, ERROR_PLAY_SONG
}

enum class MediaPlayerFavouritesState{
    PLAYING, STOPPED
}

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    val repository: TracksRepository,
    val mediaPlayerService: MediaPlayerService,
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

    val tracks : MutableLiveData<List<ViewModelTrack>> by lazy {
        MutableLiveData<List<ViewModelTrack>>()
    }

    val uiState: MutableLiveData<FavouritesUiState> by lazy {
        MutableLiveData<FavouritesUiState>()
    }

    val notification: MutableLiveData<FavouritesNotification> by lazy {
        MutableLiveData<FavouritesNotification>(getInitialNotification())
    }

    /**
     * Is media player currently playing
     */
    val mediaPlayerState: MutableLiveData<MediaPlayerFavouritesState> by lazy {
        MutableLiveData<MediaPlayerFavouritesState>()
    }

    init {
        subscribeMediaPlayerListeners()
    }

    fun subscribeMediaPlayerListeners() = viewModelScope.launch {
        mediaPlayerService.setCallbacks(
            started = {
                mediaPlayerState.postValue(MediaPlayerFavouritesState.PLAYING)
            },
            finished = {
                mediaPlayerState.postValue(MediaPlayerFavouritesState.STOPPED)
            },
            error = {
                mediaPlayerState.postValue(MediaPlayerFavouritesState.STOPPED)
            }
        )

        if(mediaPlayerService.isPlaying()){
            mediaPlayerState.postValue(MediaPlayerFavouritesState.PLAYING)
        }
        else{
            mediaPlayerState.postValue(MediaPlayerFavouritesState.STOPPED)
        }
    }

    fun unsubscribeMediaPlayerListeners() = viewModelScope.launch {
        mediaPlayerService.setCallbacks(
            started = {},
            finished = {},
            error = {}
        )
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        uiState.postValue(FavouritesUiState.LOADING)
        val loadedTracks = repository.getTracks()
        uiState.postValue(FavouritesUiState.READY)
        tracks.postValue(loadedTracks.map { it.toViewModelTrack() })
    }

    fun deleteTrack(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val success = repository.deleteTrackById(id)
        if(success){
            notification.postValue(FavouritesNotification.SUCCESS_DELETE_TRACK)
        }
        else{
            notification.postValue(FavouritesNotification.ERROR_DELETE_TRACK)
        }

        val newList = repository.getTracks().map { it.toViewModelTrack() }
        tracks.postValue(newList)
    }

    fun playSong(trackId: String) = viewModelScope.launch(Dispatchers.IO) {
        val track = repository.getTrackById(trackId)
        playUrlSound(track.previewUrl)
    }

    fun stopSong() = viewModelScope.launch {
        mediaPlayerService.stop()
    }

    private fun playUrlSound(soundUri: String) : Boolean{
        var isSuccess = false

        val started = {
            isSuccess = false
            mediaPlayerState.postValue(MediaPlayerFavouritesState.PLAYING)
        }
        val finished = {
            isSuccess = true
            mediaPlayerState.postValue(MediaPlayerFavouritesState.STOPPED)
        }
        val error = {
            isSuccess = false
            mediaPlayerState.postValue(MediaPlayerFavouritesState.STOPPED)
            notification.postValue(FavouritesNotification.ERROR_PLAY_SONG)
        }

        mediaPlayerService.playUrlSound(soundUri, started, finished, error)

        return isSuccess
    }

}