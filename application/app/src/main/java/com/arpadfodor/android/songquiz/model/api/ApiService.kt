package com.arpadfodor.android.songquiz.model.api

import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class ApiService @Inject constructor() {

    val httpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(
        EncoderInterceptor()
    ).build()

    val playlistAPI = Retrofit.Builder()
        .baseUrl(SpotifyPlaylistAPI.API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
        .create(SpotifyPlaylistAPI::class.java)

    fun getPlaylistById(id: Int) : ApiPlaylist{

        var response = ApiPlaylist(0)

        try {
            val dataCall = playlistAPI.getPlaylistData(id)
            response = dataCall.execute().body() ?: ApiPlaylist(0)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            return response
        }

    }

}