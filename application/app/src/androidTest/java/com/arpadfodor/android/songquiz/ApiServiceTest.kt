package com.arpadfodor.android.songquiz

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arpadfodor.android.songquiz.model.api.ApiService
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApiServiceTest{

    lateinit var api: ApiService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        api = ApiService(context)
    }

    @Test
    fun getPlaylistByIdTest() {
        // given
        // Id of my test playlist on Spotify
        val testPlaylistId = "1DXBJmdOLooVWLNq8DFwKB"
        // when
        val rawPlaylist = api.getPlaylistById(testPlaylistId)
        // then
        Assert.assertTrue(rawPlaylist.id == testPlaylistId)
    }

    @Test
    fun getPlaylistByIdInvalidTest() {
        // given
        val invalidId = "aaaaaxsxsxsxsxsesesse"
        // when
        val result = api.getPlaylistById(invalidId)
        // then
        Assert.assertTrue(result.id.isEmpty())
    }

    @After
    fun finish() {}

}