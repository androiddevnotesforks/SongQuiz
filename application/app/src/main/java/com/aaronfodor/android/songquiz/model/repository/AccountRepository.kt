package com.aaronfodor.android.songquiz.model.repository

import android.content.Context
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.api.ApiService
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class AccountRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val apiService: ApiService
) {

    private val keyAccountId = context.getString(R.string.PREF_KEY_ACCOUNT_ID)
    private val keyAccountName = context.getString(R.string.PREF_KEY_ACCOUNT_NAME)
    private val keyAccountEmail = context.getString(R.string.PREF_KEY_ACCOUNT_EMAIL)
    private val keyAccountUri = context.getString(R.string.PREF_KEY_ACCOUNT_URI)
    private val keyAccountCountry = context.getString(R.string.PREF_KEY_ACCOUNT_COUNTRY)
    private val keyToken = context.getString(R.string.PREF_KEY_TOKEN)
    private val keyTokenExpireTime = context.getString(R.string.PREF_KEY_TOKEN_EXPIRE_TIME)

    fun searchSelfAccount(token: String) : Account{
        val apiAccount = apiService.getCurrentAccount(token)
        return apiAccount.toAccount()
    }

    fun getAccount() : Account{
        // get saved account info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val accountId = sharedPreferences.getString(keyAccountId, "") ?: ""
        val accountName = sharedPreferences.getString(keyAccountName, "") ?: ""
        val accountEmail = sharedPreferences.getString(keyAccountEmail, "") ?: ""
        val accountUri = sharedPreferences.getString(keyAccountUri, "") ?: ""
        val accountCountry = sharedPreferences.getString(keyAccountCountry, "") ?: ""
        val token = sharedPreferences.getString(keyToken, "") ?: ""
        val tokenExpireTime = sharedPreferences.getString(keyTokenExpireTime, "") ?: ""

        var tokenExpireTimeLong = 0L
        if(tokenExpireTime.isNotBlank()){
            tokenExpireTimeLong = tokenExpireTime.toLong()
        }

        return Account(accountId, accountName, accountEmail, accountUri, accountCountry, token, tokenExpireTimeLong)
    }

    fun updateAccount(account: Account){
        // write account info to preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(sharedPreferences.edit()){
            remove(keyAccountId)
            putString(keyAccountId, account.id)
            remove(keyAccountName)
            putString(keyAccountName, account.name)
            remove(keyAccountEmail)
            putString(keyAccountEmail, account.email)
            remove(keyAccountUri)
            putString(keyAccountUri, account.uri)
            remove(keyAccountCountry)
            putString(keyAccountCountry, account.country)
            remove(keyToken)
            putString(keyToken, account.token)
            remove(keyTokenExpireTime)
            putString(keyTokenExpireTime, account.tokenExpireTime.toString())
            apply()
        }
    }

    fun deleteAccount(){
        // delete account info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(sharedPreferences.edit()){
            remove(keyAccountId)
            putString(keyAccountId, "")
            remove(keyAccountName)
            putString(keyAccountName, "")
            remove(keyAccountEmail)
            putString(keyAccountEmail, "")
            remove(keyAccountUri)
            putString(keyAccountUri, "")
            remove(keyAccountCountry)
            putString(keyAccountCountry, "")
            remove(keyToken)
            putString(keyToken, "")
            remove(keyTokenExpireTime)
            putString(keyTokenExpireTime, "")
            apply()
        }
    }

}