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

    fun recordCurrentProfileGameResults(){
        val profile = getCurrentProfile()
        val currentPublicAccount = accountService.getPublicInfo()

        if(currentPublicAccount.id == AccountService.DEFAULTS_ACCOUNT_ID){
            return
        }
        // apply change
        val updatedProfile = profile
        // update
        updateProfile(updatedProfile)
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