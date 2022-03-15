/*
 * Copyright (c) Aaron Fodor  - All Rights Reserved
 *
 * https://github.com/aaronfodor
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aaronfodor.android.songquiz.model.repository.dataclasses

import com.aaronfodor.android.songquiz.model.database.dataclasses.DbProfile

fun Profile.toDbProfile(timestampUTC: String) : DbProfile {
    return DbProfile(
        id = this.id,
        timestampUTC = timestampUTC,
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
        totalReward = this.totalReward
    )
}

fun DbProfile.toProfile() : Profile {
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
        totalReward = this.totalReward
    )
}

fun Profile.setWith(accountInfo: PublicAccountInfo) : Profile {
    if(this.id == accountInfo.id){
        this.name = accountInfo.name
        this.email = accountInfo.email
        this.country = accountInfo.country
        this.uri = accountInfo.uri
        this.imageUri = accountInfo.imageUri
    }
    return this
}