package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InfoUiState{
    LOADING, READY, PLAYLIST_FALLBACK, ERROR_PLAYLIST_LOAD, CLOSE, AUTH_NEEDED, START_QUIZ
}

enum class TtsInfoState{
    ENABLED, SPEAKING
}

enum class InfoScreenCaller{
    UNSPECIFIED, PLAYLISTS, ADD_PLAYLIST
}

@HiltViewModel
class InfoViewModel  @Inject constructor(
    var repository: PlaylistsRepository,
    var textToSpeechService: TextToSpeechService,
    val accountService: AccountService
) : ViewModel() {

    var infoScreenCaller = InfoScreenCaller.UNSPECIFIED

    /**
     * Info state
     */
    val uiState: MutableLiveData<InfoUiState> by lazy {
        MutableLiveData<InfoUiState>()
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
    val playlist: MutableLiveData<Playlist> by lazy {
        MutableLiveData<Playlist>()
    }

    init { viewModelScope.launch {
        if(textToSpeechService.isSpeaking()){
            ttsState.value = TtsInfoState.SPEAKING
        }
        else{
            ttsState.value = TtsInfoState.ENABLED
        }

        subscribeTtsListeners()
    } }

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

    fun setPlaylistById(playlistId: String) = viewModelScope.launch(Dispatchers.IO) {
        if(playlist.value?.id == playlistId){
            ready()
            return@launch
        }
        else{
            // loading state
            uiState.postValue(InfoUiState.LOADING)

            val downloadedPlaylist = repository.downloadPlaylistById(playlistId)

            // successfully downloaded
            if(downloadedPlaylist.id == playlistId){
                ready()
                playlist.postValue(downloadedPlaylist)
                if(infoScreenCaller == InfoScreenCaller.PLAYLISTS){
                    repository.updatePlaylist(downloadedPlaylist)
                }
            }
            // cannot download
            else{
                val fallbackPlaylist = repository.getPlaylistById(playlistId)
                // load from disk
                if(fallbackPlaylist.id == playlistId){
                    uiState.postValue(InfoUiState.PLAYLIST_FALLBACK)
                    playlist.postValue(fallbackPlaylist)
                }
                // cannot load from disk either
                else{
                    uiState.postValue(InfoUiState.ERROR_PLAYLIST_LOAD)
                }
            }
        }
    }

    fun deletePlaylist() = viewModelScope.launch(Dispatchers.IO) {
        playlist.value?.let {
            if(infoScreenCaller == InfoScreenCaller.PLAYLISTS){
                repository.deletePlaylistById(it.id)
                uiState.postValue(InfoUiState.CLOSE)
            }
        }
    }

    fun addPlaylist() = viewModelScope.launch(Dispatchers.IO) {
        playlist.value?.let {
            uiState.postValue(InfoUiState.LOADING)
            repository.insertPlaylist(it)
            uiState.postValue(InfoUiState.CLOSE)
        }
    }

    fun ready() = viewModelScope.launch {
        uiState.value = InfoUiState.READY
    }

    fun startQuiz() = viewModelScope.launch {
        if(accountService.accountState.value != AccountState.LOGGED_IN){
            uiState.value = InfoUiState.AUTH_NEEDED
            return@launch
        }

        uiState.value = InfoUiState.START_QUIZ
    }

}