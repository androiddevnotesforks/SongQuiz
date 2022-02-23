package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsUiState{
    READY, CACHE_CLEARED, PLAYLISTS_DELETED, DEFAULT_PLAYLISTS_RESTORED, ACCOUNT_LOGGED_OUT
}

@HiltViewModel
class SettingsViewModel  @Inject constructor(
    val playlistsRepository: PlaylistsRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

    /**
     * Settings state
     */
    val uiState: MutableLiveData<SettingsUiState> by lazy {
        MutableLiveData<SettingsUiState>()
    }

    fun getUserNameAndEmail() : Pair<String, String>{
        val publicAccountInfo = accountService.getPublicInfo()
        return Pair(publicAccountInfo.name, publicAccountInfo.email)
    }

    fun deletePlaylists() = viewModelScope.launch(Dispatchers.IO) {
        playlistsRepository.deleteAllPlaylists()
        uiState.postValue(SettingsUiState.PLAYLISTS_DELETED)
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        accountService.logout()
        uiState.postValue(SettingsUiState.ACCOUNT_LOGGED_OUT)
    }

    fun ready() = viewModelScope.launch {
        uiState.value = SettingsUiState.READY
    }

    fun cacheCleared() = viewModelScope.launch {
        uiState.value = SettingsUiState.CACHE_CLEARED
    }

    fun restoreDefaultPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        // rollback to default database content
        playlistsRepository.restoreDefaultPlaylists()
        uiState.postValue(SettingsUiState.DEFAULT_PLAYLISTS_RESTORED)
    }

    fun changeDefaultPlaylistsMode(){
        val newMode = !accountService.setDefaultsMode
        accountService.setDefaultsMode = newMode
    }

    fun getDefaultPlaylistsMode() : Boolean{
        return accountService.setDefaultsMode
    }

}