package com.aaronfodor.android.songquiz.model.database.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aaronfodor.android.songquiz.model.database.ApplicationDB

@Entity(tableName = ApplicationDB.PROFILE_TABLE_NAME)
data class DbProfile(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: String = "",
    var timestampUTC: String = "",
    var name: String = "",
    var email: String = "",
    var country: String = "",
    var imageUri: String = "",
    var uri: String = "",
    // stats
    // single
    var single_NumGamesPlayed: Long = 0L,
    var single_TotalWins: Long = 0L,
    var single_TotalTies: Long = 0L,
    var single_TotalTitleHits: Long = 0L,
    var single_TotalArtistHits: Long = 0L,
    var single_TotalNumSongs: Long = 0L,
    var single_TotalSongLength: Long = 0L,
    var single_TotalSongDifficulty: Long = 0L,
    // multiplayer
    var multi_NumGamesPlayed: Long = 0L,
    var multi_TotalWins: Long = 0L,
    var multi_TotalTies: Long = 0L,
    var multi_TotalTitleHits: Long = 0L,
    var multi_TotalArtistHits: Long = 0L,
    var multi_TotalNumSongs: Long = 0L,
    var multi_TotalSongLength: Long = 0L,
    var multi_TotalSongDifficulty: Long = 0L,
    var multi_TotalNumPlayers: Long = 0L,
    // reward
    var totalReward: Long = 0L
)