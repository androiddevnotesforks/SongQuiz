package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Profile
import com.aaronfodor.android.songquiz.safeArithmetic

class ViewModelProfile (
    var id: String = "",
    var timestampUTC: String = "",
    var name: String = "",
    var email: String = "",
    var country: String = "",
    var imageUri: String = "",
    var uri: String = "",
    // stats
    // single player
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
    var multi_TotalNumPlayers: Long = 0L
)

fun ViewModelProfile.getTotalXP() : Long {
    val singleHitPercentage = safeArithmetic { (this.single_TotalArtistHits + this.single_TotalTitleHits) / (2.0 * this.single_TotalNumSongs) }
    val singleDifficultyPercentage = safeArithmetic{ (this.single_TotalSongDifficulty / this.single_TotalNumSongs) / 100.0 }
    val singlePlayerXP = this.single_TotalNumSongs + (singleHitPercentage * this.single_TotalNumSongs) + (singleDifficultyPercentage * this.single_TotalNumSongs)

    val multiHitPercentage = safeArithmetic { (this.multi_TotalArtistHits + this.multi_TotalTitleHits) / (2.0 * this.multi_TotalNumSongs) }
    val multiDifficultyPercentage = safeArithmetic { (this.multi_TotalSongDifficulty / this.multi_TotalNumSongs) / 100.0 }
    val multiPlayerXP = this.multi_TotalNumSongs + (multiHitPercentage * this.multi_TotalNumSongs) + (multiDifficultyPercentage * this.multi_TotalNumSongs)

    return (singlePlayerXP + multiPlayerXP).toLong()
}

fun Profile.toViewModelProfile() : ViewModelProfile {
    return ViewModelProfile(
        id = this.id,
        timestampUTC = this.timestampUTC,
        name = this.name,
        email = this.email,
        country = this.country,
        imageUri = this.imageUri,
        uri = this.uri,
        single_NumGamesPlayed = this.single_NumGamesPlayed,
        single_TotalWins = this.single_TotalWins,
        single_TotalTies = this.single_TotalTies,
        single_TotalTitleHits = this.single_TotalTitleHits,
        single_TotalArtistHits = this.single_TotalArtistHits,
        single_TotalNumSongs = this.single_TotalNumSongs,
        single_TotalSongLength = this.single_TotalSongLength,
        single_TotalSongDifficulty = this.single_TotalSongDifficulty,
        multi_NumGamesPlayed = this.multi_NumGamesPlayed,
        multi_TotalWins = this.multi_TotalWins,
        multi_TotalTies = this.multi_TotalTies,
        multi_TotalTitleHits = this.multi_TotalTitleHits,
        multi_TotalArtistHits = this.multi_TotalArtistHits,
        multi_TotalNumSongs = this.multi_TotalNumSongs,
        multi_TotalSongLength = this.multi_TotalSongLength,
        multi_TotalSongDifficulty = this.multi_TotalSongDifficulty,
        multi_TotalNumPlayers = this.multi_TotalNumPlayers,
    )
}

fun ViewModelProfile.toProfile() : Profile {
    return Profile(
        id = this.id,
        timestampUTC = this.timestampUTC,
        name = this.name,
        email = this.email,
        country = this.country,
        imageUri = this.imageUri,
        uri = this.uri,
        single_NumGamesPlayed = this.single_NumGamesPlayed,
        single_TotalWins = this.single_TotalWins,
        single_TotalTies = this.single_TotalTies,
        single_TotalTitleHits = this.single_TotalTitleHits,
        single_TotalArtistHits = this.single_TotalArtistHits,
        single_TotalNumSongs = this.single_TotalNumSongs,
        single_TotalSongLength = this.single_TotalSongLength,
        single_TotalSongDifficulty = this.single_TotalSongDifficulty,
        multi_NumGamesPlayed = this.multi_NumGamesPlayed,
        multi_TotalWins = this.multi_TotalWins,
        multi_TotalTies = this.multi_TotalTies,
        multi_TotalTitleHits = this.multi_TotalTitleHits,
        multi_TotalArtistHits = this.multi_TotalArtistHits,
        multi_TotalNumSongs = this.multi_TotalNumSongs,
        multi_TotalSongLength = this.multi_TotalSongLength,
        multi_TotalSongDifficulty = this.multi_TotalSongDifficulty,
        multi_TotalNumPlayers = this.multi_TotalNumPlayers,
    )
}