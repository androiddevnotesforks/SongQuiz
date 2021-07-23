package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.repository.AccountRepository
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsUiState{
    READY, CACHE_CLEARED, PLAYLISTS_DELETED, DEFAULT_DB_RESTORED, ACCOUNT_LOGGED_OUT
}

enum class SettingsAccountState{
    LOGGED_IN, INVALID_TOKEN, LOGGED_OUT
}

@HiltViewModel
class SettingsViewModel  @Inject constructor(
    val playlistsRepository: PlaylistsRepository,
    val accountRepository: AccountRepository,
    val accountService: AccountService
) : ViewModel() {

    /**
     * Settings state
     */
    val uiState: MutableLiveData<SettingsUiState> by lazy {
        MutableLiveData<SettingsUiState>()
    }

    // subscribe to the service's MutableLiveData from the ViewModel with Transformations
    val accountState = Transformations.map(accountService.accountState) { serviceAccountState ->
        when(serviceAccountState){
            AccountState.LOGGED_IN -> SettingsAccountState.LOGGED_IN
            AccountState.INVALID_TOKEN -> SettingsAccountState.INVALID_TOKEN
            AccountState.LOGGED_OUT -> SettingsAccountState.LOGGED_OUT
            else -> {SettingsAccountState.LOGGED_OUT}
        }
    }

    fun getUserNameAndEmail() : Pair<String, String>{
        return accountService.getUserNameAndEmail()
    }

    fun deletePlaylists() = viewModelScope.launch(Dispatchers.IO) {
        playlistsRepository.deleteAllPlaylists()
        uiState.postValue(SettingsUiState.PLAYLISTS_DELETED)
    }

    fun prepareToRestoreDefaultDB() = viewModelScope.launch(Dispatchers.IO) {
        playlistsRepository.prepareToRestoreDefaults()
        uiState.postValue(SettingsUiState.DEFAULT_DB_RESTORED)
    }

    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        accountRepository.deleteAccount()
        accountService.logout()
        uiState.postValue(SettingsUiState.ACCOUNT_LOGGED_OUT)
    }

    fun ready() = viewModelScope.launch {
        uiState.value = SettingsUiState.READY
    }

    fun cacheCleared() = viewModelScope.launch {
        uiState.value = SettingsUiState.CACHE_CLEARED
    }

}