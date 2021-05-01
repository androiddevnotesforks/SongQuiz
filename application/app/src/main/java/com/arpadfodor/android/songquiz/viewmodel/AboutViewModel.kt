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

    private suspend fun speakToUser(text: String){

        suspendCoroutine<Boolean?> { cont ->

            val started = {
                ttsState.postValue(TtsAboutState.SPEAKING)
            }

            val finished = {
                ttsState.postValue(TtsAboutState.ENABLED)
                cont.resume(true)
            }

            val error = {
                ttsState.postValue(TtsAboutState.ENABLED)
                cont.resume(true)
            }

            textToSpeechService.speak(text, started, finished, error)

        }

    }

    fun speak(text: String){
        viewModelScope.launch {
            speakToUser(text)
        }
    }

    fun stopSpeaking(){
        textToSpeechService.stop()
        ttsState.postValue(TtsAboutState.ENABLED)
    }

}