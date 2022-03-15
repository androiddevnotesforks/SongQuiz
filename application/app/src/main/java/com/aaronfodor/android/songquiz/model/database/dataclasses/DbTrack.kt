package com.aaronfodor.android.songquiz.model.database.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.aaronfodor.android.songquiz.model.database.ApplicationDB

@Entity(tableName = ApplicationDB.TRACK_TABLE_NAME, primaryKeys = ["id", "accountId"])
data class DbTrack(
    @ColumnInfo(name = "id")
    var id: String = "",
    @ColumnInfo(name = "accountId")
    var accountId: String = "",
    var timestampUTC: String = "",
    var name: String = "",
    var artists: String = "",
    var album: String = "",
    var imageUri: String = "",
    val durationMs: Int = 0,
    val popularity: Int = 0,
    val uri: String = "",
    val previewUrl: String = ""
)