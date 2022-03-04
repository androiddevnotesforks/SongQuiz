package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.repository.ProfileRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelProfile
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelProfile
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileNotification{
    NONE, LOGGED_OUT
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: ProfileRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    val currentProfile : MutableLiveData<ViewModelProfile> by lazy {
        MutableLiveData<ViewModelProfile>()
    }

    val notification: MutableLiveData<ProfileNotification> by lazy {
        MutableLiveData<ProfileNotification>()
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        val profile = repository.getCurrentProfile()
        currentProfile.postValue(profile.toViewModelProfile())
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        accountService.logout()
        currentProfile.postValue(ViewModelProfile())
        notification.postValue(ProfileNotification.LOGGED_OUT)
    }

}