package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class TtsAboutState{
    ENABLED, SPEAKING
}

@HiltViewModel
class AboutViewModel  @Inject constructor(
    var textToSpeechService: TextToSpeechService
) : ViewModel() {

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsAboutState> by lazy {
        MutableLiveData<TtsAboutState>()
    }

    init {
        if(textToSpeechService.isSpeaking()){
            ttsState.value = TtsAboutState.SPEAKING
        }
        else{
            ttsState.value = TtsAboutState.ENABLED
        }

        subscribeTextToSpeechListeners()
    }

    private fun subscribeTextToSpeechListeners(){
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
    }

    fun speak(text: String){
        viewModelScope.launch {
            textToSpeechService.speak(text)
        }
    }

    fun stopSpeaking(){
        textToSpeechService.stop()
    }

}