package com.arpadfodor.android.songquiz

import com.arpadfodor.android.songquiz.model.api.ApiService
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.database.PlaylistDAO
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MockedTest {

    private val FAKE_ID = "1"
    private val FAKE_RESPONSE = ApiPlaylist(id = "1", name = "Best playlist")
    private val FAKE_DB_CONTENT = DbPlaylist(id = "1", name = "Best playlist")

    @Mock
    private lateinit var mockApi: ApiService
    @Mock
    private lateinit var mockDAO: PlaylistDAO

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockApi = mock(ApiService::class.java)
        mockDAO = mock(PlaylistDAO::class.java)
    }

    @Test
    fun getGamePlaylistTest() {
        // Given
        `when`(mockApi.getPlaylistById(FAKE_ID)).thenReturn(FAKE_RESPONSE)
        val repo = PlaylistsRepository(mockDAO, mockApi)

        // When
        val result: Playlist = repo.getGamePlaylistById(FAKE_ID)

        // Then
        assertThat(result.id, `is`(FAKE_ID))
    }

    @Test
    fun getDbPlaylistTest() {
        // Given
        `when`(mockDAO.getAll()).thenReturn(listOf(FAKE_DB_CONTENT))
        val repo = PlaylistsRepository(mockDAO, mockApi)

        // When
        val results: List<Playlist> = repo.getPlaylists()

        // Then
        assertThat(results, `is`(not(emptyList())))
    }

}