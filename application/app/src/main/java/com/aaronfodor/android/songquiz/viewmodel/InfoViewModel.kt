package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.getDifficulty
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InfoUiState{
    LOADING, READY_COMPLETE, READY_FALLBACK, CLOSE, AUTH_NEEDED, START_QUIZ
}

enum class InfoUiNotification{
    NONE, FALLBACK_LOAD, ERROR_LOAD, ERROR_ADD_ITEM, SUCCESS_ADD_ITEM, ERROR_DELETE_ITEM, SUCCESS_DELETE_ITEM
}

enum class TtsInfoState{
    ENABLED, SPEAKING
}

enum class InfoScreenCaller{
    UNSPECIFIED, HOME, PLAY, ADD_PLAYLIST
}

@HiltViewModel
class InfoViewModel  @Inject constructor(
    var repository: PlaylistsRepository,
    var textToSpeechService: TextToSpeechService,
    val accountService: AccountService
) : ViewModel() {

    var infoScreenCaller = InfoScreenCaller.UNSPECIFIED

    val uiState: MutableLiveData<InfoUiState> by lazy {
        MutableLiveData<InfoUiState>()
    }

    val notification: MutableLiveData<InfoUiNotification> by lazy {
        MutableLiveData<InfoUiNotification>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsInfoState> by lazy {
        MutableLiveData<TtsInfoState>()
    }

    /**
     * Playlist to show
     */
    val item: MutableLiveData<ViewModelPlaylist> by lazy {
        MutableLiveData<ViewModelPlaylist>()
    }

    init {
        viewModelScope.launch {
            if(textToSpeechService.isSpeaking()){
                ttsState.value = TtsInfoState.SPEAKING
            }
            else{
                ttsState.value = TtsInfoState.ENABLED
            }
            subscribeTtsListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsInfoState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsInfoState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsInfoState.ENABLED)
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

    fun setItemById(playlistId: String, forceLoad: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if(item.value?.id == playlistId && !forceLoad){
            item.value?.let {
                if(it.tracks.size > 0 || it.followers > 0 || it.getDifficulty() > 0){
                    ready(InfoUiState.READY_COMPLETE)
                    return@launch
                }
            }
            ready(InfoUiState.READY_FALLBACK)
            return@launch
        }
        else{
            // loading state
            uiState.postValue(InfoUiState.LOADING)

            val downloadedPlaylist = repository.downloadPlaylistById(playlistId)

            // successfully downloaded
            if(downloadedPlaylist.id == playlistId){
                ready(InfoUiState.READY_COMPLETE)
                item.postValue(downloadedPlaylist.toViewModelPlaylist())
                if(infoScreenCaller == InfoScreenCaller.PLAY){
                    repository.updatePlaylist(downloadedPlaylist)
                }
            }
            // cannot download
            else{
                ready(InfoUiState.READY_FALLBACK)
                val fallbackPlaylist = repository.getPlaylistById(playlistId)
                // load from disk
                if(fallbackPlaylist.id == playlistId){
                    notification.postValue(InfoUiNotification.FALLBACK_LOAD)
                    item.postValue(fallbackPlaylist.toViewModelPlaylist())
                }
                // cannot load from disk either
                else{
                    notification.postValue(InfoUiNotification.ERROR_LOAD)
                }
            }

        }
    }

    fun deleteItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            if(infoScreenCaller == InfoScreenCaller.PLAY || infoScreenCaller == InfoScreenCaller.HOME){

                val success = repository.deletePlaylistById(it.id)
                if(success){
                    notification.postValue(InfoUiNotification.SUCCESS_DELETE_ITEM)
                }
                else{
                    notification.postValue(InfoUiNotification.ERROR_DELETE_ITEM)
                }

                uiState.postValue(InfoUiState.CLOSE)
            }
        }
    }

    fun addItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            uiState.postValue(InfoUiState.LOADING)

            val success = repository.insertPlaylist(it.toPlaylist())
            if(success){
                notification.postValue(InfoUiNotification.SUCCESS_ADD_ITEM)
            }
            else{
                notification.postValue(InfoUiNotification.ERROR_ADD_ITEM)
            }

            uiState.postValue(InfoUiState.CLOSE)
        }
    }

    fun ready(vale: InfoUiState) = viewModelScope.launch {
        uiState.value = vale
    }

    fun startQuiz() = viewModelScope.launch {
        if(accountService.accountState.value != AccountState.LOGGED_IN){
            uiState.value = InfoUiState.AUTH_NEEDED
            return@launch
        }
        uiState.value = InfoUiState.START_QUIZ
    }

}