package com.aaronfodor.android.songquiz.model.database

import androidx.room.*
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist

@Dao
interface PlaylistDAO {

    @Query("SELECT * FROM ${ApplicationDB.PLAYLIST_TABLE_NAME}")
    fun getAll(): List<DbPlaylist>?

    @Query("SELECT * FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE id=:playlistId ")
    fun getById(playlistId: String): List<DbPlaylist>?

    @Query("DELETE FROM ${ApplicationDB.PLAYLIST_TABLE_NAME}")
    fun deleteAll()

    @Query("DELETE FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE id=:playlistId ")
    fun delete(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: DbPlaylist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<DbPlaylist>)

    @Update
    fun update(vararg item: DbPlaylist)

}