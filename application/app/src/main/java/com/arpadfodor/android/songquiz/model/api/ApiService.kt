package com.arpadfodor.android.songquiz.model.api

import android.util.Base64
import com.arpadfodor.android.songquiz.model.AccountService
import com.arpadfodor.android.songquiz.model.api.dataclasses.AccountDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistsDTO
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
open class ApiService @Inject constructor(
        val accountService: AccountService
) {

    val spotifyClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(
        EncoderInterceptor()
    ).build()

    val spotifyAPI = Retrofit.Builder()
        .baseUrl(SpotifyAPI.API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(spotifyClient)
        .build()
        .create(SpotifyAPI::class.java)

    private fun bearerTokenEncoder(token: String) : String{
        return "Bearer $token"
    }

    private fun tokenRequestParamEncoder(toEncode: String) : String{
        var encoded = Base64.encodeToString(toEncode.toByteArray(charset = Charsets.UTF_8), Base64.NO_WRAP)
        encoded = "Basic $encoded"
        return encoded
    }

    private fun getToken() : String{
        val sec = "" //context.getString(R.string.client_info)
        var response = ""
        try {
            val dataCall = spotifyAPI.requestToken(base64Content = tokenRequestParamEncoder(sec), grantType = "client_credentials")
            response = dataCall.execute().body()?.access_token ?: ""
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

    // must be open for mock testing
    open fun getPlaylistById(id: String) : PlaylistDTO {
        var response = PlaylistDTO(name = "", id = "")

        try {
            val token = accountService.getValidToken()
            val dataCall = spotifyAPI.getPlaylistById(id, bearerTokenEncoder(token))
            response = dataCall.execute().body() ?: PlaylistDTO(name = "", id = "")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

    fun getPlaylistsByName(name: String, offset: Int) : PlaylistsDTO {
        var response = PlaylistsDTO(items = arrayOf())

        try {
            val token = accountService.getValidToken()
            val dataCall = spotifyAPI.getPlaylistsByName(name=name, authHeader=bearerTokenEncoder(token), offset=offset)
            dataCall.execute().body()?.let {
                response = it.playlists
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

    fun getPlaylistsByIdOrName(searchExpression: String, offset: Int) : PlaylistsDTO{
        var response = PlaylistsDTO(items = arrayOf())

        try {
            // id search
            val foundItem = getPlaylistById(searchExpression)
            // search expression is not empty and found item Id is equal to it
            if(searchExpression.isNotBlank() && foundItem.id == searchExpression){
                response = PlaylistsDTO(items = arrayOf(foundItem))
            }
            // name search
            if(response.items.isEmpty()){
                response = getPlaylistsByName(searchExpression, offset)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

    fun getCurrentAccount(accessToken: String) : AccountDTO{
        var response = AccountDTO()

        try {
            val token = if(accessToken.isNotBlank()){
                accessToken
            }
            else{
                accountService.getValidToken()
            }

            val dataCall = spotifyAPI.getAccount(authHeader=bearerTokenEncoder(token))
            dataCall.execute().body()?.let {
                response = it
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

}