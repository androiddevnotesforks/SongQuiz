package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InfoTrackUiState{
    LOADING, READY, CLOSE
}

enum class InfoTrackUiNotification{
    NONE, ERROR_LOAD, ERROR_DELETE_ITEM, SUCCESS_DELETE_ITEM
}

enum class TtsInfoTrackState{
    ENABLED, SPEAKING
}

enum class InfoTrackScreenCaller{
    UNSPECIFIED, FAVOURITES
}

@HiltViewModel
class InfoTrackViewModel  @Inject constructor(
    var repository: PlaylistsRepository,
    var textToSpeechService: TextToSpeechService,
    accountService: AccountService
) : AppViewModel(accountService) {

    var infoScreenCaller = InfoTrackScreenCaller.UNSPECIFIED
        private set
    fun setCaller(text: String){
        infoScreenCaller = InfoTrackScreenCaller.valueOf(text)
    }

    val uiState: MutableLiveData<InfoTrackUiState> by lazy {
        MutableLiveData<InfoTrackUiState>()
    }

    val notification: MutableLiveData<InfoTrackUiNotification> by lazy {
        MutableLiveData<InfoTrackUiNotification>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsInfoTrackState> by lazy {
        MutableLiveData<TtsInfoTrackState>()
    }

    /**
     * Item to show
     */
    val item: MutableLiveData<ViewModelPlaylist> by lazy {
        MutableLiveData<ViewModelPlaylist>()
    }

    init {
        viewModelScope.launch {
            if(textToSpeechService.isSpeaking()){
                ttsState.value = TtsInfoTrackState.SPEAKING
            }
            else{
                ttsState.value = TtsInfoTrackState.ENABLED
            }
            subscribeTtsListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsInfoTrackState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsInfoTrackState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsInfoTrackState.ENABLED)
            }
        )
    }

    fun unsubscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {},
            finished = {},
            error = {}
        )
    }

    fun speak(text: String) = viewModelScope.launch {
        textToSpeechService.speak(text)
    }

    fun stopSpeaking() = viewModelScope.launch {
        textToSpeechService.stop()
    }

    fun setItemById(trackId: String, forceLoad: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if(item.value?.id == trackId && !forceLoad){
            ready()
            return@launch
        }
        else{
            // loading state
            uiState.postValue(InfoTrackUiState.LOADING)

            val track = repository.getPlaylistById(trackId)
            // load from disk
            if(track.id == trackId){
                item.postValue(track.toViewModelPlaylist())
            }
            // cannot load from disk either
            else{
                notification.postValue(InfoTrackUiNotification.ERROR_LOAD)
            }

            uiState.postValue(InfoTrackUiState.READY)
        }
    }

    fun deleteItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            val success = repository.deletePlaylistById(it.id)
            if(success){
                notification.postValue(InfoTrackUiNotification.SUCCESS_DELETE_ITEM)
            }
            else{
                notification.postValue(InfoTrackUiNotification.ERROR_DELETE_ITEM)
            }

            uiState.postValue(InfoTrackUiState.CLOSE)
        }
    }

    fun ready() = viewModelScope.launch {
        uiState.value = InfoTrackUiState.READY
    }

}