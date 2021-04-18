package com.arpadfodor.android.songquiz.model.api

import android.content.Context
import android.util.Base64
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class ApiService @Inject constructor(
        @ApplicationContext val context: Context
) {

    val spotifyClient: OkHttpClient = OkHttpClient.Builder().build()

    val spotifyAPI = Retrofit.Builder()
        .baseUrl(SpotifyAPI.API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(spotifyClient)
        .build()
        .create(SpotifyAPI::class.java)

    private fun getToken() : String{
        val sec = context.getString(R.string.client_info)
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

    fun getPlaylistById(id: String) : ApiPlaylist{
        var response = ApiPlaylist(name = "", id = "")
        try {
            val token = getToken()
            val dataCall = spotifyAPI.getPlaylistData(id, bearerTokenEncoder(token))
            response = dataCall.execute().body() ?: ApiPlaylist(name = "", id = "")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }
    }

    private fun bearerTokenEncoder(token: String) : String{
        return "Bearer $token"
    }

    private fun tokenRequestParamEncoder(toEncode: String) : String{
        var encoded = Base64.encodeToString(toEncode.toByteArray(charset = Charsets.UTF_8), Base64.NO_WRAP)
        encoded = "Basic $encoded"
        return encoded
    }

}