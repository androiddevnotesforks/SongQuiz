package com.aaronfodor.android.songquiz.model.database.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.aaronfodor.android.songquiz.model.database.ApplicationDB

@Entity(tableName = ApplicationDB.PLAYLIST_TABLE_NAME, primaryKeys = ["id", "accountId"])
data class DbPlaylist(
    @ColumnInfo(name = "id")
    var id: String = "",
    @ColumnInfo(name = "accountId")
    var accountId: String = "",
    var timestampUTC: String = "",
    var name: String = "",
    var description: String = "",
    var owner: String = "",
    var imageUri: String = "",
    val uri: String = ""
)