package com.aaronfodor.android.songquiz.model

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.repository.AccountRepository
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Account
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PublicAccountInfo
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toPublicAccountInfo
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class AccountState{
    LOGGED_IN, LOGGED_OUT, NONE
}

/**
 * Injected everywhere as a singleton
 */
@Singleton
class AccountService @Inject constructor(
    @ApplicationContext val context: Context,
    val repository: AccountRepository
){

    companion object{
        const val DEFAULTS_ACCOUNT_ID = "default"
    }

    private var account = Account("")

    private val clientId = context.getString(R.string.spotifyClientId)
    private val spotifyRedirectURI = context.getString(R.string.com_spotify_sdk_redirect_uri)

    // for setting the defaults
    var setDefaultsMode = false

    /**
     * Used for fallback token requests on behalf of the app. Works when client secret is available.
     * Only one valid token is existing at the same time with this option. Only use it for testing.
     */
    val spotifyTokenRequestParam = context.getString(R.string.spotifyClientId) + ":" + context.getString(R.string.spotifyClientSec)

    /**
     * Require a new token before it expires in that many seconds
     */
    val tokenRequireBeforeExpiresSec = 30

    val accountState: MutableLiveData<AccountState> by lazy {
        MutableLiveData<AccountState>()
    }

    fun initialize(){
        // if a user was logged in, remember it from the disk
        setAccount(repository.getAccount())
    }

    fun getAuthRequest() : AuthorizationRequest{
        if(accountState.value == AccountState.LOGGED_OUT){
            // force the logged out dialog
            AuthorizationClient.clearCookies(context)
        }
        val builder = AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, spotifyRedirectURI)
        return builder.setScopes(arrayOf("user-read-email")).setShowDialog(false).build()
    }

    fun getResponse(resultCode: Int, data: Intent) : AuthorizationResponse{
        return AuthorizationClient.getResponse(resultCode, data)
    }

    fun setAccount(accountToSet: Account){
        if(accountToSet.id.isNotBlank()){
            account = accountToSet
            repository.updateAccount(accountToSet)
            accountState.postValue(AccountState.LOGGED_IN)
        }
        else{
            val emptyAccount = Account("")
            account = emptyAccount
            repository.updateAccount(emptyAccount)
            accountState.postValue(AccountState.LOGGED_OUT)
        }
    }

    fun updateToken(token: String, tokenExpireTime: Long){
        val accountToSet = Account(
            id = account.id,
            name = account.name,
            email = account.email,
            uri = account.uri,
            country = account.country,
            token = token,
            tokenExpireTime = tokenExpireTime,
            isFirstLoadAfterLogin = account.isFirstLoadAfterLogin
        )
        account = accountToSet
        accountState.postValue(AccountState.LOGGED_IN)
    }

    private fun isValidToken(token: String, tokenExpireTime: Long) : Boolean{
        val currentTime = System.currentTimeMillis()
        return token.isNotBlank() && currentTime < tokenExpireTime
    }

    fun isAuthNeeded() : Boolean {
        return !(accountState.value == AccountState.LOGGED_IN && isValidToken(account.token, account.tokenExpireTime))
    }

    fun getValidToken() : String{
        return if(isAuthNeeded()){
            // token is invalid
            ""
        }
        else{
            // token is valid
            account.token
        }
    }

    fun logout(){
        AuthorizationClient.clearCookies(context)
        repository.deleteAccount()
        account = Account("")
        accountState.postValue(AccountState.LOGGED_OUT)
    }

    fun getPublicInfo() : PublicAccountInfo {
        return account.toPublicAccountInfo()
    }

    fun getAccountId() : String{
        val id = if(setDefaultsMode){
            AccountService.DEFAULTS_ACCOUNT_ID
        }
        else{
            account.id
        }

        return id
    }

    fun firstLoadFinishedAfterLogin(){
        repository.firstLoadFinishedAfterLogin()
        account.isFirstLoadAfterLogin = false
    }

}