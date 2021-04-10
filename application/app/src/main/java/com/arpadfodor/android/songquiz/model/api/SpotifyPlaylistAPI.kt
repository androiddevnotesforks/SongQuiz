package com.arpadfodor.android.songquiz.model.api

import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SpotifyPlaylistAPI {

    companion object {
        //emulator testing
        //const val BASE_URL = "http://10.0.2.2:8080/"
        const val BASE_URL = "http://spotify.com/"
        const val API_URL = "${BASE_URL}api/v1/"
        const val GET_PLAYLIST = "${API_URL}playlist/"
    }

    @GET("${GET_PLAYLIST}/{provided_id}")
    fun getPlaylistData(@Path("provided_id") id: Int): Call<ApiPlaylist>

}