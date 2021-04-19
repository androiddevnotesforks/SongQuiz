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
class DbTest{

    lateinit var db: ApplicationDB

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.databaseBuilder(
            context, ApplicationDB::class.java, ApplicationDB.APPLICATION_DB_NAME
        ).build()
    }

    @Test
    @Throws(Exception::class)
    fun insertPlaylistTest() {
        val toInsert = DbPlaylist("1", "Best playlist ever")
        db.playlistDAO().insert(toInsert)
        Assert.assertFalse(db.playlistDAO().getAll().isNullOrEmpty())
    }

    @After
    fun finish() {
        db.close()
    }

}