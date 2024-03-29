package com.aaronfodor.android.songquiz.model.repository

import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.TimeService
import com.aaronfodor.android.songquiz.model.database.ProfileDAO
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Profile
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toDbProfile
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toProfile
import com.aaronfodor.android.songquiz.model.repository.dataclasses.setWith
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val dao: ProfileDAO,
    private val accountService: AccountService,
    private val timeService: TimeService
) {

    fun getCurrentProfile() : Profile{
        val dbProfiles = dao.getById(accountService.getAccountId()) ?: listOf()
        val currentPublicAccount = accountService.getPublicInfo()

        if(dbProfiles.isNotEmpty()){
            val currentProfile = dbProfiles[0].toProfile()
            if(accountService.getPublicInfo().id != AccountService.DEFAULTS_ACCOUNT_ID){
                // apply change
                val updatedProfile = currentProfile.setWith(currentPublicAccount)
                // update
                updateProfile(updatedProfile)
                return updatedProfile
            }
            else{
                return currentProfile
            }
        }
        else{
            if(currentPublicAccount.id != AccountService.DEFAULTS_ACCOUNT_ID){
                val newProfile = Profile(id=currentPublicAccount.id).setWith(currentPublicAccount)
                insertCurrentProfile(newProfile)
                return newProfile
            }
            else{
                return Profile()
            }
        }
    }

    fun recordCurrentProfileGameResults(isMultiPlayerGame: Boolean, isVictory: Boolean, isTie: Boolean,
                                        titleHits: Long, artistHits: Long, numSongs: Long,
                                        totalSongLength: Long, totalSongDifficulty: Long, totalNumPlayers: Long){
        val profile = getCurrentProfile()
        val currentPublicAccount = accountService.getPublicInfo()

        if(currentPublicAccount.id == AccountService.DEFAULTS_ACCOUNT_ID){
            return
        }

        if(isMultiPlayerGame){
            profile.multi_NumGamesPlayed += 1

            if(isVictory){
                profile.multi_TotalWins += 1
            }
            else{
                profile.multi_TotalTies += 1
            }

            profile.multi_TotalTitleHits += titleHits
            profile.multi_TotalArtistHits += artistHits
            profile.multi_TotalNumSongs += numSongs
            profile.multi_TotalSongLength += totalSongLength
            profile.multi_TotalSongDifficulty += totalSongDifficulty
            profile.multi_TotalNumPlayers += totalNumPlayers
        }
        else{
            profile.single_NumGamesPlayed += 1

            when {
                isVictory -> {
                    profile.single_TotalWins += 1
                }
                isTie -> {
                    profile.single_TotalTies += 1
                }
                else -> {}
            }

            profile.single_TotalTitleHits += titleHits
            profile.single_TotalArtistHits += artistHits
            profile.single_TotalNumSongs += numSongs
            profile.single_TotalSongLength += totalSongLength
            profile.single_TotalSongDifficulty += totalSongDifficulty
        }

        // apply change - update
        updateProfile(profile)
    }

    fun recordReward(){
        val profile = getCurrentProfile()
        val currentPublicAccount = accountService.getPublicInfo()

        if(currentPublicAccount.id == AccountService.DEFAULTS_ACCOUNT_ID){
            return
        }

        profile.totalReward += 1
        // apply change - update
        updateProfile(profile)
    }

    private fun insertCurrentProfile(profile: Profile) : Boolean{
        return if(profile.id == accountService.getAccountId()){
            val toInsert = profile.toDbProfile(timeService.getTimestampUTC())
            dao.insert(toInsert)
            true
        }
        else{
            false
        }
    }

    private fun updateProfile(profile: Profile) : Boolean{
        val toUpdate = profile.toDbProfile(timeService.getTimestampUTC())
        dao.update(toUpdate)
        return true
    }

    fun deleteCurrentProfile() : Boolean{
        dao.delete(accountService.getAccountId())
        return true
    }

    fun deleteAllProfiles() : Boolean{
        dao.deleteAll()
        return true
    }

}