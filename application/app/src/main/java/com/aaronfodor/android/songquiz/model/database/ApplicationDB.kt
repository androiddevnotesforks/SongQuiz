package com.aaronfodor.android.songquiz.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
@Database(entities = [DbPlaylist::class], version = 3, exportSchema = false)
abstract class ApplicationDB : RoomDatabase() {

    companion object{
        const val APPLICATION_DB_NAME = "application_database"
        const val PLAYLIST_TABLE_NAME = "playlist_table"
        const val DEFAULT_DB_FILE_PATH = "database/application_database.db"
    }

    abstract fun playlistDAO(): PlaylistDAO

}

@InstallIn(SingletonComponent::class)
@Module
class DatabaseInjector {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): ApplicationDB {
        val builder = Room.databaseBuilder(
            appContext,
            ApplicationDB::class.java,
            ApplicationDB.APPLICATION_DB_NAME
        ).createFromAsset(ApplicationDB.DEFAULT_DB_FILE_PATH)
        // enable traditional DB execution (no wal, shm files) - use to generate a .db file
        // database file restore works with JournalMode.TRUNCATE
        // more: https://blog.devart.com/increasing-sqlite-performance.html
        // more: https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
        builder.setJournalMode(RoomDatabase.JournalMode.TRUNCATE)

        return builder.build()
    }

    @Provides
    fun providePlaylistDAO(db: ApplicationDB): PlaylistDAO {
        return db.playlistDAO()
    }

}