package com.arpadfodor.android.songquiz

import com.arpadfodor.android.songquiz.model.api.ApiService
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiArtist
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiPlaylist
import com.arpadfodor.android.songquiz.model.api.dataclasses.ApiTrack
import com.arpadfodor.android.songquiz.model.database.PlaylistDAO
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Track
import com.arpadfodor.android.songquiz.model.repository.toDbPlaylist
import com.arpadfodor.android.songquiz.model.repository.toPlaylist
import com.arpadfodor.android.songquiz.model.repository.toTrack
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PlaylistRepositoryTest{

    private val FAKE_ID = "1"
    private val FAKE_API_RESPONSE = ApiPlaylist(id = "1", name = "Best playlist")
    private val FAKE_DB_CONTENT = DbPlaylist(id = "1", name = "Best playlist")

    @Mock
    private lateinit var mockApi: ApiService
    @Mock
    private lateinit var mockDAO: PlaylistDAO

    private lateinit var repo: PlaylistsRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockApi = mock(ApiService::class.java)
        mockDAO = mock(PlaylistDAO::class.java)
        repo = PlaylistsRepository(mockDAO, mockApi)
    }

    @Test
    fun getPlaylistFromApiTest() {
        // Given
        `when`(mockApi.getPlaylistById(FAKE_ID)).thenReturn(FAKE_API_RESPONSE)
        // When
        val result: Playlist = repo.downloadPlaylistById(FAKE_ID)
        // Then
        assertThat(result.id, `is`(FAKE_ID))
    }

    @Test
    fun getPlaylistFromDBTest() {
        // Given
        `when`(mockDAO.getAll()).thenReturn(listOf(FAKE_DB_CONTENT))
        // When
        val results: List<Playlist> = repo.getPlaylists()
        // Then
        assertThat(results, `is`(not(emptyList())))
    }

    @Test
    fun addPlaylistByIdTest() {
        // Given
        `when`(mockApi.getPlaylistById(FAKE_ID)).thenReturn(FAKE_API_RESPONSE)
        // When
        repo.addPlaylistById(FAKE_ID)
        // Then
        verify(mockApi, times(1)).getPlaylistById(FAKE_ID)
        verify(mockDAO, times(1)).insert(FAKE_DB_CONTENT)
    }

    @Test
    fun deleteDbPlaylistTest() {
        // Given
        // When
        repo.deletePlaylistById(FAKE_DB_CONTENT.id)
        // Then
        verify(mockDAO, times(1)).delete(FAKE_DB_CONTENT.id)
    }

}

@RunWith(MockitoJUnitRunner::class)
class PlaylistConverterTest{

    private val FAKE_ID = "1"
    private val FAKE_CONTENT = "Best playlist"
    private val API_TRACK = ApiTrack(artists = arrayOf(ApiArtist(id="2", name="Elton John")), duration_ms = 100, id = "20", name="I'm still standing")
    private val API_PLAYLIST = ApiPlaylist(id = FAKE_ID, name = FAKE_CONTENT)
    private val DB_PLAYLIST = DbPlaylist(id = FAKE_ID, name = FAKE_CONTENT)
    private val PLAYLIST = Playlist(id = FAKE_ID, name = FAKE_CONTENT)

    @Test
    fun convertPlaylistToDbPlaylistTest() {
        // Given
        // When
        val result = PLAYLIST.toDbPlaylist()
        // Then
        assertThat(result, instanceOf(DbPlaylist::class.java))
        assertThat(result.id, `is`(PLAYLIST.id))
        assertThat(result.name, `is`(PLAYLIST.name))
    }

    @Test
    fun convertDbPlaylistToPlaylistTest() {
        // Given
        // When
        val result = DB_PLAYLIST.toPlaylist()
        // Then
        assertThat(result, instanceOf(Playlist::class.java))
        assertThat(result.id, `is`(DB_PLAYLIST.id))
        assertThat(result.name, `is`(DB_PLAYLIST.name))
    }

    @Test
    fun convertApiTrackToTrack() {
        // Given
        // When
        val result = API_TRACK.toTrack()
        // Then
        assertThat(result, instanceOf(Track::class.java))
        assertThat(result.id, `is`(API_TRACK.id))
        assertThat(result.name, `is`(API_TRACK.name))
    }

    @Test
    fun convertApiPlaylistToPlaylistTest() {
        // Given
        // When
        val result = API_PLAYLIST.toPlaylist()
        // Then
        assertThat(result, instanceOf(Playlist::class.java))
        assertThat(result.id, `is`(API_PLAYLIST.id))
        assertThat(result.name, `is`(API_PLAYLIST.name))
    }

}