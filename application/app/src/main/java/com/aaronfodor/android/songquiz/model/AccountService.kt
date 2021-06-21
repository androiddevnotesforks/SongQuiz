package com.aaronfodor.android.songquiz.model

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Account
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class AccountState{
    LOGGED_IN, INVALID_TOKEN, LOGGED_OUT
}

/**
 * Injected everywhere as a singleton
 */
@Singleton
class AccountService @Inject constructor(
    @ApplicationContext val context: Context
) {

    private var account= Account("")

    private val clientId = context.getString(R.string.client_id)
    private val spotifyRedirectURI = context.getString(R.string.com_spotify_sdk_redirect_uri)

    /**
     * Used for fallback token requests on behalf of the app. Works when client secret is available.
     * Only one valid token is existing at the same time with this option. Only use it for testing.
     */
    val spotifyTokenRequestParam = context.getString(R.string.spotify_token_request_param)

    /**
     * Require a new token before it expires in that many seconds
     */
    val tokenRequireBeforeExpiresSec = 30

    val accountState: MutableLiveData<AccountState> by lazy {
        MutableLiveData<AccountState>()
    }

    fun getAuthRequest() : AuthorizationRequest{
        if(accountState.value == AccountState.LOGGED_OUT){
            // force the logged out dialog
            AuthorizationClient.clearCookies(context)
        }

        val builder = AuthorizationRequest.Builder(
            clientId, AuthorizationResponse.Type.TOKEN, spotifyRedirectURI)
        return builder.setScopes(arrayOf("user-read-email")).setShowDialog(false).build()
    }

    fun getResponse(resultCode: Int, data: Intent) : AuthorizationResponse{
        return AuthorizationClient.getResponse(resultCode, data)
    }

    fun setAccount(accountToSet: Account){
        if(accountToSet.id.isNotBlank()){
            account = accountToSet

            if(isValidToken(accountToSet.token, accountToSet.tokenExpireTime)){
                accountState.postValue(AccountState.LOGGED_IN)
            }
            else{
                accountState.postValue(AccountState.INVALID_TOKEN)
            }
        }
        else{
            accountState.postValue(AccountState.LOGGED_OUT)
            account = Account("")
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
            tokenExpireTime = tokenExpireTime
        )
        account = accountToSet

        if(isValidToken(token, tokenExpireTime)){
            accountState.postValue(AccountState.LOGGED_IN)
        }
        else{
            accountState.postValue(AccountState.INVALID_TOKEN)
        }
    }

    private fun isValidToken(token: String, tokenExpireTime: Long) : Boolean{
        val currentTime = System.currentTimeMillis()
        return token.isNotBlank() && currentTime < tokenExpireTime
    }

    fun getValidToken() : String{
        val currentTime = System.currentTimeMillis()
        if(account.token.isBlank() || currentTime > account.tokenExpireTime){
            accountState.postValue(AccountState.INVALID_TOKEN)
        }
        return account.token
    }

    fun logout(){
        AuthorizationClient.clearCookies(context)
        account = Account("")
        accountState.postValue(AccountState.LOGGED_OUT)
    }

    fun getUserNameAndEmail() : Pair<String, String>{
        return Pair(account.name, account.email)
    }

}