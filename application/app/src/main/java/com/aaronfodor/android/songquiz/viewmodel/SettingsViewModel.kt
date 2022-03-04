package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.model.repository.ProfileRepository
import com.aaronfodor.android.songquiz.model.repository.TracksRepository
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsNotification{
    NONE, CACHE_CLEARED, PLAYLISTS_DELETED, FAVOURITES_DELETED, PROFILE_STATS_DELETED, DEFAULT_PLAYLISTS_RESTORED, ACCOUNT_LOGGED_OUT
}

@HiltViewModel
class SettingsViewModel  @Inject constructor(
    private val playlistsRepository: PlaylistsRepository,
    private val tracksRepository: TracksRepository,
    private val profileRepository: ProfileRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    /**
     * Settings state
     */
    val notification: MutableLiveData<SettingsNotification> by lazy {
        MutableLiveData<SettingsNotification>()
    }

    fun getUserNameAndEmail() : Pair<String, String>{
        val publicAccountInfo = accountService.getPublicInfo()
        return Pair(publicAccountInfo.name, publicAccountInfo.email)
    }

    fun cacheCleared() = viewModelScope.launch {
        notification.value = SettingsNotification.CACHE_CLEARED
    }

    fun deletePlaylists() = viewModelScope.launch(Dispatchers.IO) {
        playlistsRepository.deleteAllPlaylists()
        notification.postValue(SettingsNotification.PLAYLISTS_DELETED)
    }

    fun deleteFavourites() = viewModelScope.launch(Dispatchers.IO) {
        tracksRepository.deleteAllTracks()
        notification.postValue(SettingsNotification.FAVOURITES_DELETED)
    }

    fun deleteProfileStats() = viewModelScope.launch(Dispatchers.IO) {
        profileRepository.deleteCurrentProfile()
        notification.postValue(SettingsNotification.PROFILE_STATS_DELETED)
    }

    fun restoreDefaultPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        // rollback to default database content
        playlistsRepository.restoreDefaultPlaylists()
        notification.postValue(SettingsNotification.DEFAULT_PLAYLISTS_RESTORED)
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        accountService.logout()
        notification.postValue(SettingsNotification.ACCOUNT_LOGGED_OUT)
    }

    // for debug mode
    fun changeDefaultPlaylistsMode(){
        val newMode = !accountService.setDefaultsMode
        accountService.setDefaultsMode = newMode
    }
    // for debug mode
    fun getDefaultPlaylistsMode() : Boolean{
        return accountService.setDefaultsMode
    }

}