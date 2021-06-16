package com.arpadfodor.android.songquiz.viewmodel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.AccountService
import com.arpadfodor.android.songquiz.model.AccountState
import com.arpadfodor.android.songquiz.model.repository.AccountRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Account
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthUiState{
    EMPTY, START_LOGIN, SUCCESS, ERROR_INTERNET, ERROR_DENIED, ERROR
}

enum class AuthAccountState{
    LOGGED_IN, INVALID_TOKEN, LOGGED_OUT
}

@HiltViewModel
class AuthViewModel  @Inject constructor(
    val repository: AccountRepository,
    val accountService: AccountService
) : ViewModel() {

    /**
     * Login state
     */
    val uiState: MutableLiveData<AuthUiState> by lazy {
        MutableLiveData<AuthUiState>()
    }

    // subscribe to the service's MutableLiveData from the ViewModel with Transformations
    val accountState = Transformations.map(accountService.accountState) { serviceAccountState ->
        when(serviceAccountState){
            AccountState.LOGGED_IN -> AuthAccountState.LOGGED_IN
            AccountState.INVALID_TOKEN -> AuthAccountState.INVALID_TOKEN
            AccountState.LOGGED_OUT -> AuthAccountState.LOGGED_OUT
            else -> AuthAccountState.LOGGED_OUT
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // initialize the account service
            accountService.setAccount(repository.getAccount())
        }
    }

    fun getLoginRequest() : AuthorizationRequest {
        return accountService.getAuthRequest()
    }

    fun processLoginResult(resultCode: Int, data: Intent){
        viewModelScope.launch(Dispatchers.IO) {
            val response = accountService.getResponse(resultCode, data)

            val token = response.accessToken
            val tokenExpireTime = System.currentTimeMillis() + ((response.expiresIn - accountService.tokenRequireBeforeExpiresSec) * 1000)

            when (response.type) {
                // success
                AuthorizationResponse.Type.TOKEN -> {
                    val account = repository.searchSelfAccount(token)
                    val accountToSet = Account(
                        id = account.id,
                        name = account.name,
                        email = account.email,
                        uri = account.uri,
                        country = account.country,
                        token = token,
                        tokenExpireTime = tokenExpireTime
                    )
                    repository.updateAccount(accountToSet)
                    accountService.setAccount(accountToSet)
                    uiState.postValue(AuthUiState.SUCCESS)
                }
                AuthorizationResponse.Type.ERROR -> {
                    uiState.postValue(AuthUiState.ERROR_INTERNET)
                }
                AuthorizationResponse.Type.EMPTY -> {
                    uiState.postValue(AuthUiState.ERROR_DENIED)
                }
                else -> {
                    uiState.postValue(AuthUiState.ERROR)
                }
            }
        }
    }

}