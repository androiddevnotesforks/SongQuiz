package com.aaronfodor.android.songquiz.model.database

import androidx.room.*
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbProfile

@Dao
interface ProfileDAO {

    @Query("SELECT * FROM ${ApplicationDB.PROFILE_TABLE_NAME}")
    fun getAll(): List<DbProfile>?

    @Query("SELECT * FROM ${ApplicationDB.PROFILE_TABLE_NAME} WHERE id=:id ")
    fun getById(id: String): List<DbProfile>?

    @Query("DELETE FROM ${ApplicationDB.PROFILE_TABLE_NAME}")
    fun deleteAll()

    @Query("DELETE FROM ${ApplicationDB.PROFILE_TABLE_NAME} WHERE id=:id ")
    fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: DbProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<DbProfile>)

    @Update
    fun update(vararg item: DbProfile)

}