package com.aaronfodor.android.songquiz.viewmodel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.api.SpotifyApiService
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Account
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toAccount
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthUiState{
    EMPTY, START_LOGIN, SUCCESS
}

enum class AuthNotification{
    ERROR_INTERNET, ERROR_DENIED, ERROR, NONE
}

@HiltViewModel
class AuthViewModel @Inject constructor(accountService: AccountService, private val apiService: SpotifyApiService) : AppViewModel(accountService) {

    /**
     * Login state
     */
    val uiState: MutableLiveData<AuthUiState> by lazy {
        MutableLiveData<AuthUiState>()
    }

    val notification: MutableLiveData<AuthNotification> by lazy {
        MutableLiveData<AuthNotification>()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // initialize the account service
            accountService.initialize()
        }
    }

    fun isAuthNeeded() : Boolean{
        return accountService.isAuthNeeded()
    }

    fun getLoginRequest() : AuthorizationRequest {
        return accountService.getAuthRequest()
    }

    fun processLoginResult(resultCode: Int, data: Intent) = viewModelScope.launch(Dispatchers.IO) {
        val response = accountService.getResponse(resultCode, data)

        val token = response.accessToken
        val tokenExpireTime = System.currentTimeMillis() + ((response.expiresIn - accountService.tokenRequireBeforeExpiresSec) * 1000)
        // to make a token to expire every second
        //val tokenExpireTime = System.currentTimeMillis() + 1000

        when (response.type) {
            // success
            AuthorizationResponse.Type.TOKEN -> {
                val account = apiService.getCurrentAccount(token).toAccount(accountService.getPublicInfo().isFirstLoadAfterLogin)
                val accountToSet = Account(
                    id = account.id,
                    name = account.name,
                    email = account.email,
                    country = account.country,
                    uri = account.uri,
                    imageUri = account.imageUri,
                    token = token,
                    tokenExpireTime = tokenExpireTime,
                    isFirstLoadAfterLogin = account.isFirstLoadAfterLogin
                )
                accountService.setAccount(accountToSet)
                uiState.postValue(AuthUiState.SUCCESS)
            }
            AuthorizationResponse.Type.ERROR -> {
                notification.postValue(AuthNotification.ERROR_INTERNET)
                uiState.postValue(AuthUiState.EMPTY)
            }
            AuthorizationResponse.Type.EMPTY -> {
                notification.postValue(AuthNotification.ERROR_DENIED)
                uiState.postValue(AuthUiState.EMPTY)
            }
            else -> {
                notification.postValue(AuthNotification.ERROR)
                uiState.postValue(AuthUiState.EMPTY)
            }
        }
    }

}