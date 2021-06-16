package com.arpadfodor.android.songquiz.model.api

import com.arpadfodor.android.songquiz.model.api.dataclasses.AccountDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.PlaylistsResponseDTO
import com.arpadfodor.android.songquiz.model.api.dataclasses.TokenDTO
import retrofit2.Call
import retrofit2.http.*

interface SpotifyAPI {

    companion object {
        private const val REQUEST_TOKEN_URL = "https://accounts.spotify.com/api/token"
        private const val API_BASE_URL = "https://api.spotify.com/"
        const val API_URL = "${API_BASE_URL}v1/"
        const val GET_SEARCH = "${API_URL}search"
        const val GET_PLAYLIST = "${API_URL}playlists/"
        const val GET_ACCOUNT = "${API_URL}me"
    }

    @GET(GET_SEARCH)
    fun getPlaylistsByName(
        @Query("q") name: String,
        @Query("type") type: String = "playlist",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int,
        @Header("Authorization") authHeader: String
    ): Call<PlaylistsResponseDTO>

    @GET("${GET_PLAYLIST}{playlist_id}")
    fun getPlaylistById(
        @Path("playlist_id") id: String,
        @Header("Authorization") authHeader: String
    ): Call<PlaylistDTO>

    @GET(GET_ACCOUNT)
    fun getAccount(
        @Header("Authorization") authHeader: String
    ): Call<AccountDTO>

    @FormUrlEncoded
    @POST(REQUEST_TOKEN_URL)
    fun requestToken(
        @Header("Authorization") base64Content: String,
        @Field("grant_type") grantType : String
    ): Call<TokenDTO>

}