package com.arpadfodor.android.songquiz.model.database.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arpadfodor.android.songquiz.model.database.ApplicationDB

@Entity(tableName = ApplicationDB.PLAYLIST_TABLE_NAME)
data class DbPlaylist(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int = 0
)