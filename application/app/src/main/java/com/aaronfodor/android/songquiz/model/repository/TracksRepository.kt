package com.aaronfodor.android.songquiz.model.repository

import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.AccountState
import com.aaronfodor.android.songquiz.model.TimeService
import com.aaronfodor.android.songquiz.model.database.TrackDAO
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toDbTrack
import com.aaronfodor.android.songquiz.model.repository.dataclasses.toTrack
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class TracksRepository @Inject constructor(
    private val dao: TrackDAO,
    private val accountService: AccountService,
    private val timeService: TimeService
) {

    fun getTracks() : List<Track>{
        // if logged out, no tracks
        val dbTracks = if(accountService.accountState.value == AccountState.LOGGED_OUT){
            listOf()
        }
        // if logged in, load tracks
        else{
            dao.getAll(accountService.getAccountId()) ?: listOf()
        }

        val tracks = mutableListOf<Track>()
        for(item in dbTracks){
            tracks.add(item.toTrack())
        }
        return tracks.reversed()
    }


    fun getTrackById(id: String) : Track{
        val dbTracks = dao.getById(id, accountService.getAccountId()) ?: listOf()

        return if(dbTracks.isNotEmpty()){
            dbTracks[0].toTrack()
        }
        else{
            Track("")
        }
    }

    fun insertTrack(track: Track) : Boolean{
        val toInsert = track.toDbTrack(accountService.getAccountId(), timeService.getTimestampUTC())
        dao.insert(toInsert)
        return true
    }

    fun updateTrack(track: Track) : Boolean{
        val toUpdate = track.toDbTrack(accountService.getAccountId(), timeService.getTimestampUTC())
        dao.update(toUpdate)
        return true
    }

    fun deleteTrackById(id: String) : Boolean{
        dao.delete(id, accountService.getAccountId())
        return true
    }

    fun deleteAllTracks() : Boolean{
        dao.deleteAll(accountService.getAccountId())
        return true
    }

}