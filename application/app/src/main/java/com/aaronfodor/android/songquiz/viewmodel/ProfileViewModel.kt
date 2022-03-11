package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.ProfileRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelProfile
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelProfile
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileNotification{
    NONE, LOGGED_OUT, PROFILE_STATS_DELETED
}

enum class TtsProfileState{
    ENABLED, SPEAKING
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: ProfileRepository,
    accountService: AccountService,
    val textToSpeechService: TextToSpeechService,
) : AppViewModel(accountService) {

    val currentProfile : MutableLiveData<ViewModelProfile> by lazy {
        MutableLiveData<ViewModelProfile>()
    }

    val notification: MutableLiveData<ProfileNotification> by lazy {
        MutableLiveData<ProfileNotification>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsProfileState> by lazy {
        MutableLiveData<TtsProfileState>()
    }

    init {
        viewModelScope.launch {
            if(textToSpeechService.isSpeaking()){
                ttsState.value = TtsProfileState.SPEAKING
            }
            else{
                ttsState.value = TtsProfileState.ENABLED
            }

            subscribeTtsListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsProfileState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsProfileState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsProfileState.ENABLED)
            }
        )

        if(textToSpeechService.isSpeaking()){
            ttsState.postValue(TtsProfileState.SPEAKING)
        }
        else{
            ttsState.postValue(TtsProfileState.ENABLED)
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

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        val profile = repository.getCurrentProfile()
        currentProfile.postValue(profile.toViewModelProfile())
    }

    fun deleteProfileStats() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCurrentProfile()
        notification.postValue(ProfileNotification.PROFILE_STATS_DELETED)
        loadData()
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        accountService.logout()
        currentProfile.postValue(ViewModelProfile())
        notification.postValue(ProfileNotification.LOGGED_OUT)
    }

}