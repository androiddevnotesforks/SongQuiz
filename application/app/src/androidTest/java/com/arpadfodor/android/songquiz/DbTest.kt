package com.arpadfodor.android.songquiz

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.arpadfodor.android.songquiz.model.database.ApplicationDB
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DbTest{

    var instrumentationContext: Context? = null
    lateinit var db: ApplicationDB

    @Before
    fun setUp() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        db = Room.databaseBuilder(
            instrumentationContext!!,
            ApplicationDB::class.java,
            ApplicationDB.APPLICATION_DB_NAME
        ).build()
    }

    @Test
    fun testDbInsert() {
        val toInsert = DbPlaylist("1", "Best playlist ever")
        db.playlistDAO().insert(toInsert)
        Assert.assertFalse(db.playlistDAO().getAll().isNullOrEmpty())
    }

    @After
    fun finish() {
        db.close()
    }

}