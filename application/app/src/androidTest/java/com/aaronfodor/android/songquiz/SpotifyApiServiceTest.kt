package com.aaronfodor.android.songquiz

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.api.SpotifyApiService
import com.aaronfodor.android.songquiz.model.repository.AccountRepository
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpotifyApiServiceTest{

    lateinit var spotifyApi: SpotifyApiService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AccountRepository(context)
        val accountService = AccountService(context, repository)
        spotifyApi = SpotifyApiService(accountService)
    }

    @Test
    fun getPlaylistByIdTest() {
        // given
        // Id of my test playlist on Spotify
        val testPlaylistId = "1DXBJmdOLooVWLNq8DFwKB"
        // when
        val rawPlaylist = spotifyApi.getPlaylistById(testPlaylistId)
        // then
        // discard this assertion as Spotify login is needed for that
        //Assert.assertTrue(rawPlaylist.id == testPlaylistId)
    }

    @Test
    fun getPlaylistByIdInvalidTest() {
        // given
        val invalidId = "aaaaaxsxsxsxsxsesesse"
        // when
        val result = spotifyApi.getPlaylistById(invalidId)
        // then
        Assert.assertTrue(result.id.isEmpty())
    }

    @After
    fun finish() {}

}