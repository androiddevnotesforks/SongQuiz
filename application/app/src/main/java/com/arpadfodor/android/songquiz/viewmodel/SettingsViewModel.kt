package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.AccountService
import com.arpadfodor.android.songquiz.model.AccountState
import com.arpadfodor.android.songquiz.model.repository.AccountRepository
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsUiState{
    READY, CACHE_CLEARED, PLAYLISTS_DELETED, ACCOUNT_LOGGED_OUT
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

    fun getUserName() : String{
        return accountService.getUserName()
    }

    fun deletePlaylists(){
        viewModelScope.launch(Dispatchers.IO) {
            playlistsRepository.deleteAllPlaylists()
            uiState.postValue(SettingsUiState.PLAYLISTS_DELETED)
        }
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.deleteAccount()
            accountService.logout()
            uiState.postValue(SettingsUiState.ACCOUNT_LOGGED_OUT)
        }
    }

}