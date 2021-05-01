package com.arpadfodor.android.songquiz

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arpadfodor.android.songquiz.model.database.ApplicationDB
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationDBTest{

    lateinit var db: ApplicationDB

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.databaseBuilder(
            context, ApplicationDB::class.java, ApplicationDB.APPLICATION_DB_NAME
        ).build()
    }

    @Test
    fun insertPlaylistTest() {
        // given
        val toInsert = DbPlaylist("1", "Best fake playlist ever")
        // when
        db.playlistDAO().insert(toInsert)
        // then
        val result = db.playlistDAO().getAll() ?: listOf()
        Assert.assertTrue(result.contains(toInsert))
    }

    @Test
    fun getPlaylistByIdTest() {
        // given
        val toInsert1 = DbPlaylist("1", "Best fake playlist ever")
        val toInsert2 = DbPlaylist("2", "2nd Best fake playlist ever")
        // when
        db.playlistDAO().insert(listOf(toInsert1, toInsert2))
        // then
        val result = db.playlistDAO().getById("2") ?: listOf()
        Assert.assertTrue(result.contains(toInsert2))
    }

    @Test
    fun getPlaylistsTest() {
        // given
        db.playlistDAO().deleteAll()
        val toInsert1 = DbPlaylist("1", "Best fake playlist ever")
        val toInsert2 = DbPlaylist("2", "2nd Best fake playlist ever")
        // when
        db.playlistDAO().insert(listOf(toInsert1, toInsert2))
        // then
        val result = db.playlistDAO().getAll() ?: listOf()
        Assert.assertTrue(result.size == 2)
    }

    @Test
    fun deletePlaylistTest() {
        // given
        val toInsert = DbPlaylist("1", "Best fake playlist ever")
        db.playlistDAO().insert(toInsert)
        // when
        db.playlistDAO().delete("1")
        // then
        val result = db.playlistDAO().getAll() ?: listOf()
        Assert.assertFalse(result.contains(toInsert))
    }

    @Test
    fun deleteAllPlaylistsTest() {
        // given
        val toInsert1 = DbPlaylist("1", "Best fake playlist ever")
        val toInsert2 = DbPlaylist("2", "2nd Best fake playlist ever")
        db.playlistDAO().insert(listOf(toInsert1, toInsert2))
        // when
        db.playlistDAO().deleteAll()
        // then
        val result = db.playlistDAO().getAll() ?: listOf()
        Assert.assertTrue(result.isNullOrEmpty())
    }

    @Test
    fun replaceExistingPlaylist() {
        // given
        val toInsert = DbPlaylist("1", "Best fake playlist ever")
        db.playlistDAO().insert(toInsert)
        // when
        val toUpdate = DbPlaylist("1", "Best updated fake playlist ever")
        db.playlistDAO().insert(toUpdate)
        // then
        val result = db.playlistDAO().getById("1") ?: listOf()
        Assert.assertTrue(result.contains(toUpdate))
    }

    @After
    fun finish() {
        db.close()
    }

}