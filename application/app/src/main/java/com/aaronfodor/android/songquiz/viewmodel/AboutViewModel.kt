package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TtsAboutState{
    ENABLED, SPEAKING
}

@HiltViewModel
class AboutViewModel  @Inject constructor(
    var textToSpeechService: TextToSpeechService,
    accountService: AccountService
) : AppViewModel(accountService) {

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsAboutState> by lazy {
        MutableLiveData<TtsAboutState>()
    }

    init {
        viewModelScope.launch {
            if(textToSpeechService.isSpeaking()){
                ttsState.value = TtsAboutState.SPEAKING
            }
            else{
                ttsState.value = TtsAboutState.ENABLED
            }
            subscribeTtsListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsAboutState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsAboutState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsAboutState.ENABLED)
            }
        )

        if(textToSpeechService.isSpeaking()){
            ttsState.postValue(TtsAboutState.SPEAKING)
        }
        else{
            ttsState.postValue(TtsAboutState.ENABLED)
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

}