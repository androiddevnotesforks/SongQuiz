package com.aaronfodor.android.songquiz.model.database

import androidx.room.*
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbTrack

@Dao
interface TrackDAO {

    @Query("SELECT * FROM ${ApplicationDB.TRACK_TABLE_NAME} WHERE accountId=:accountId ")
    fun getAll(accountId: String): List<DbTrack>?

    @Query("SELECT * FROM ${ApplicationDB.TRACK_TABLE_NAME} WHERE accountId=:accountId ORDER BY rowid DESC LIMIT :numItems")
    fun getLastN(accountId: String, numItems: Int): List<DbTrack>?

    @Query("SELECT * FROM ${ApplicationDB.TRACK_TABLE_NAME} WHERE id=:trackId AND accountId=:accountId ")
    fun getById(trackId: String, accountId: String): List<DbTrack>?

    @Query("DELETE FROM ${ApplicationDB.TRACK_TABLE_NAME} WHERE accountId=:accountId ")
    fun deleteAll(accountId: String)

    @Query("DELETE FROM ${ApplicationDB.TRACK_TABLE_NAME} WHERE id=:trackId AND accountId=:accountId ")
    fun delete(trackId: String, accountId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: DbTrack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<DbTrack>)

    @Update
    fun update(vararg item: DbTrack)

}