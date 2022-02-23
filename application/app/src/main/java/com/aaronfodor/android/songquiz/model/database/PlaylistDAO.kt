package com.aaronfodor.android.songquiz.model.database

import androidx.room.*
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist

@Dao
interface PlaylistDAO {

    @Query("SELECT * FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE accountId=:accountId ")
    fun getAll(accountId: String): List<DbPlaylist>?

    @Query("SELECT * FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE id=:playlistId AND accountId=:accountId ")
    fun getById(playlistId: String, accountId: String): List<DbPlaylist>?

    @Query("DELETE FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE accountId=:accountId ")
    fun deleteAll(accountId: String)

    @Query("DELETE FROM ${ApplicationDB.PLAYLIST_TABLE_NAME} WHERE id=:playlistId AND accountId=:accountId ")
    fun delete(playlistId: String, accountId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: DbPlaylist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<DbPlaylist>)

    @Update
    fun update(vararg item: DbPlaylist)

}