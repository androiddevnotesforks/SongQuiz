package com.arpadfodor.android.songquiz

import com.arpadfodor.android.songquiz.model.api.ApiService
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.database.PlaylistDAO
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

private const val FAKE_ID = "1"
private val FAKE_RESPONSE = ApiPlaylist(id="1", name="Best playlist")
private val FAKE_DB_CONTENT = DbPlaylist(id="1", name="Best playlist")

@RunWith(MockitoJUnitRunner::class)
class ApiTest {

    @Mock
    private lateinit var mockApi: ApiService

    @Mock
    private lateinit var mockDAO: PlaylistDAO

    @Test
    fun readStringFromContext_LocalizedString() {

        // Given a mocked API injected into the object under test...
        `when`(mockApi.getPlaylistById(FAKE_ID)).thenReturn(FAKE_RESPONSE)
        `when`(mockDAO.getById(FAKE_ID)).thenReturn(listOf(FAKE_DB_CONTENT))


        val myObjectUnderTest = PlaylistsRepository(mockDAO, mockApi)

        // ...when the string is returned from the object under test...
        val result: Playlist = myObjectUnderTest.getGamePlaylistById(FAKE_ID)

        // ...then the result should be the expected one.
        assertThat(result.id, `is`(FAKE_ID))
    }
}