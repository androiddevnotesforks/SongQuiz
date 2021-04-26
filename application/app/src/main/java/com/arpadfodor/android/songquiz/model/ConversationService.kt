package com.arpadfodor.android.songquiz.model

import android.content.Context
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

enum class InfoType{
    SPEECH, SOUND
}

/**
 * type: the type of information
 * payload: data (text if SPEECH, Sound URI if SOUND)
 */
data class InformationItem(
    val type: InfoType,
    val payload: String
)

/**
 * contents: list of InformationItems
 * immediateAnswerNeeded: whether immediate answer is required after broadcasting contents to user
 */
data class InformationPacket(
        val contents: List<InformationItem>,
        val immediateAnswerNeeded: Boolean
)

/**
 * Injected to bind lifecycle to a ViewModel scope
 */
@ViewModelScoped
class ConversationService @Inject constructor(
    @ApplicationContext val context: Context,
    var repository: PlaylistsRepository
){

    var state = 0
    var lastSaidByUser = ""
    var playlistId = ""
    var playlist = Playlist("")

    /**
     * Reset state
     */
    fun reset(){
        playlistId = ""
        state = 0
    }

    /**
     * Set the playlist by Id
     */
    fun setPlaylistById(playlistId: String) : Boolean{

        if(playlistId.isEmpty()){
            return false
        }
        else if(playlistId == this.playlistId){
            return true
        }

        playlist = repository.getGamePlaylistById(playlistId)
        if(playlist.id.isNotEmpty()){
            this.playlistId = playlistId
            return true
        }
        return false

    }

    /**
     * Returns the current info to the user
     *
     * @return an InformationPacket
     */
    fun getCurrentInfo() : InformationPacket {

        val response = when(state){
            0 -> welcome()
            1 -> InformationPacket(listOf(InformationItem(InfoType.SPEECH, lastSaidByUser)), false)
            else -> InformationPacket(listOf(InformationItem(InfoType.SPEECH, "Looks like an error occurred.")), false)
        }

        // reset the state now for testing
        state = 0

        return response
    }

    /**
     * Update state based on user input
     *
     * @param probableSpeeches      ArrayList<String> of probable user inputs
     */
    fun userInput(probableSpeeches : ArrayList<String>) : Boolean{
        // process what the user said
        // now for testing
        state = 1
        lastSaidByUser = "Looks like you said: ${probableSpeeches[0]}"
        return true
    }

    private fun welcome() : InformationPacket {

        val randomTrack = playlist.tracks.random()

        return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, "Welcome to song quiz!"),
                InformationItem(InfoType.SPEECH, "Your chosen playlist is ${playlist.name}, which contains ${playlist.tracks.size} playable songs."),
                InformationItem(InfoType.SPEECH, "One example is ${randomTrack.name} from ${randomTrack.artists[0]}."),
                InformationItem(InfoType.SOUND, "How many player wants to play?")
        ), true)

    }

}