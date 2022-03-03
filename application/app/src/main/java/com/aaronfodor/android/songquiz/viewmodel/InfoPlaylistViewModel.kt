package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.getDifficulty
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InfoPlaylistUiState{
    LOADING, READY_COMPLETE, READY_FALLBACK, CLOSE, START_QUIZ
}

enum class InfoPlaylistUiNotification{
    NONE, FALLBACK_LOAD, ERROR_LOAD, ERROR_ADD_ITEM, SUCCESS_ADD_ITEM, ERROR_DELETE_ITEM, SUCCESS_DELETE_ITEM
}

enum class TtsInfoPlaylistState{
    ENABLED, SPEAKING
}

enum class InfoPlaylistScreenCaller{
    UNSPECIFIED, HOME, PLAY, ADD_PLAYLIST
}

@HiltViewModel
class InfoPlaylistViewModel  @Inject constructor(
    var repository: PlaylistsRepository,
    var textToSpeechService: TextToSpeechService,
    accountService: AccountService
) : AppViewModel(accountService) {

    var infoScreenCaller = InfoPlaylistScreenCaller.UNSPECIFIED
        private set
    fun setCaller(text: String){
        infoScreenCaller = InfoPlaylistScreenCaller.valueOf(text)
    }

    val uiState: MutableLiveData<InfoPlaylistUiState> by lazy {
        MutableLiveData<InfoPlaylistUiState>()
    }

    val notification: MutableLiveData<InfoPlaylistUiNotification> by lazy {
        MutableLiveData<InfoPlaylistUiNotification>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsInfoPlaylistState> by lazy {
        MutableLiveData<TtsInfoPlaylistState>()
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
                ttsState.value = TtsInfoPlaylistState.SPEAKING
            }
            else{
                ttsState.value = TtsInfoPlaylistState.ENABLED
            }
            subscribeTtsListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsInfoPlaylistState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsInfoPlaylistState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsInfoPlaylistState.ENABLED)
            }
        )

        if(textToSpeechService.isSpeaking()){
            ttsState.postValue(TtsInfoPlaylistState.SPEAKING)
        }
        else{
            ttsState.postValue(TtsInfoPlaylistState.ENABLED)
        }
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

    fun setItem(playlistId: String, forceLoad: Boolean = false) = tryAuthenticateLaunch {
        setItemById(playlistId, forceLoad)
    }

    private fun setItemById(playlistId: String, forceLoad: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {

        if(item.value?.id == playlistId && !forceLoad){
            item.value?.let {
                if(it.tracks.size > 0 || it.followers > 0 || it.getDifficulty() > 0){
                    ready(InfoPlaylistUiState.READY_COMPLETE)
                    return@launch
                }
            }
            ready(InfoPlaylistUiState.READY_FALLBACK)
            return@launch
        }
        else{
            // loading state
            uiState.postValue(InfoPlaylistUiState.LOADING)

            val downloadedPlaylist = repository.downloadPlaylistById(playlistId)

            // successfully downloaded
            if(downloadedPlaylist.id == playlistId){
                ready(InfoPlaylistUiState.READY_COMPLETE)
                item.postValue(downloadedPlaylist.toViewModelPlaylist())
                if(infoScreenCaller == InfoPlaylistScreenCaller.PLAY){
                    repository.updatePlaylist(downloadedPlaylist)
                }
            }
            // cannot download
            else{
                ready(InfoPlaylistUiState.READY_FALLBACK)
                val fallbackPlaylist = repository.getPlaylistById(playlistId)
                // load from disk
                if(fallbackPlaylist.id == playlistId){
                    notification.postValue(InfoPlaylistUiNotification.FALLBACK_LOAD)
                    item.postValue(fallbackPlaylist.toViewModelPlaylist())
                }
                // cannot load from disk either
                else{
                    notification.postValue(InfoPlaylistUiNotification.ERROR_LOAD)
                }
            }
        }

    }

    fun deleteItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            if(infoScreenCaller == InfoPlaylistScreenCaller.PLAY || infoScreenCaller == InfoPlaylistScreenCaller.HOME){

                val success = repository.deletePlaylistById(it.id)
                if(success){
                    notification.postValue(InfoPlaylistUiNotification.SUCCESS_DELETE_ITEM)
                }
                else{
                    notification.postValue(InfoPlaylistUiNotification.ERROR_DELETE_ITEM)
                }

                uiState.postValue(InfoPlaylistUiState.CLOSE)
            }
        }
    }

    fun addItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            uiState.postValue(InfoPlaylistUiState.LOADING)

            val success = repository.insertPlaylist(it.toPlaylist())
            if(success){
                notification.postValue(InfoPlaylistUiNotification.SUCCESS_ADD_ITEM)
            }
            else{
                notification.postValue(InfoPlaylistUiNotification.ERROR_ADD_ITEM)
            }

            uiState.postValue(InfoPlaylistUiState.CLOSE)
        }
    }

    fun ready(vale: InfoPlaylistUiState) = viewModelScope.launch {
        uiState.value = vale
    }

    fun startQuiz() = mustAuthenticatedLaunch {
        uiState.value = InfoPlaylistUiState.START_QUIZ
    }

}