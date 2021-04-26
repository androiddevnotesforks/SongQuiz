package com.arpadfodor.android.songquiz.model.api

import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.api.dataclasses.TokenDTO
import retrofit2.Call
import retrofit2.http.*

interface SpotifyAPI {

    companion object {
        private const val REQUEST_TOKEN_URL = "https://accounts.spotify.com/api/token"
        private const val API_BASE_URL = "https://api.spotify.com/"
        const val API_URL = "${API_BASE_URL}v1/"
        const val GET_PLAYLIST = "${API_URL}playlists/"
    }

    @GET("${GET_PLAYLIST}{playlist_id}")
    fun getPlaylistData(
        @Path("playlist_id") id: String,
        @Header("Authorization") authHeader: String
    ): Call<ApiPlaylist>

    @FormUrlEncoded
    @POST(REQUEST_TOKEN_URL)
    fun requestToken(
        @Header("Authorization") base64Content: String,
        @Field("grant_type") grantType : String
    ): Call<TokenDTO>

}