package com.arpadfodor.android.songquiz.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arpadfodor.android.songquiz.model.database.dataclasses.DbPlaylist
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
    }

    abstract fun playlistDAO(): PlaylistDAO

}

@InstallIn(SingletonComponent::class)
@Module
class DatabaseInjector {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): ApplicationDB {
        return Room.databaseBuilder(
            appContext,
            ApplicationDB::class.java,
            ApplicationDB.APPLICATION_DB_NAME
        ).build()
    }

    @Provides
    fun providePlaylistDAO(db: ApplicationDB): PlaylistDAO {
        return db.playlistDAO()
    }

}