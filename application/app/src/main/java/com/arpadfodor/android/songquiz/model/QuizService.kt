package com.arpadfodor.android.songquiz.model

import android.content.Context
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.model.quiz.QuizStanding
import com.arpadfodor.android.songquiz.model.quiz.QuizType
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

enum class QuizState{
    WELCOME,
    ASK_NUM_PLAYERS_AGAIN,
    ASK_GAME_TYPE,
    ASK_GAME_TYPE_AGAIN,
    FIRST_PLAY_SONG,
    PLAY_SONG,
    REPEAT_SONG,
    END_LAST_SONG,
    END
}

/**
 * Injected to bind lifecycle to a ViewModel scope
 */
@ViewModelScoped
class QuizService @Inject constructor(
    @ApplicationContext val context: Context,
    var repository: PlaylistsRepository
){

    companion object{
        const val MIN_NUM_TRACKS = 4
        val CHARS_TO_SEPARATE_BY = listOf(" ", "-", ",", "?", "!", ".", "(", ")").toTypedArray()
    }

    var state = QuizState.WELCOME
    var lastSaidByUser = ""
    var playlistId = ""
    var playlist = Playlist("")
    var quizType = QuizType()
    var quizStanding = QuizStanding()

    var lastPlayerPoints = 0
    var lastPlayerAllPoints = 0
    var lastSongAlbum = ""
    var lastSongTitle = ""
    var lastSongArtist = ""
    var lastSongAlbumHit = false
    var lastSongTitleHit = false
    var lastSongArtistHit = false

    /**
     * Reset state
     */
    fun reset(){
        state = QuizState.WELCOME
        lastSaidByUser = ""
        playlistId = ""
        playlist = Playlist("")
        quizType = QuizType()
        quizStanding = QuizStanding()
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
            // record playlist Id
            this.playlistId = playlistId
            // random shuffle the tracks
            playlist.tracks.shuffle()
            return true
        }
        return false

    }

    fun getPlaylistUri() : String{
        return playlist.previewImageUri
    }

    /**
     * Returns the current info to the user
     *
     * @return an InformationPacket
     */
    fun getCurrentInfo() : InformationPacket {

        val response = when(state){
            QuizState.WELCOME -> welcome()
            QuizState.ASK_NUM_PLAYERS_AGAIN -> askNumPlayersAgain()
            QuizState.ASK_GAME_TYPE -> askGameType()
            QuizState.ASK_GAME_TYPE_AGAIN -> askGameTypeAgain()
            QuizState.FIRST_PLAY_SONG -> firstPlaySong()
            QuizState.PLAY_SONG -> playSong()
            QuizState.REPEAT_SONG -> repeatSong()
            QuizState.END_LAST_SONG -> endGameAfterLastSong()
            QuizState.END -> endGame()
            else -> InformationPacket(listOf(InformationItem(InfoType.SPEECH, context.getString(R.string.c_error))), false)
        }

        return response
    }

    private fun welcome() : InformationPacket {

        if(playlist.tracks.size < MIN_NUM_TRACKS){
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome)),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_playlist_info, playlist.name, playlist.tracks.size.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_not_enough_tracks, MIN_NUM_TRACKS.toString()))
            ), false)
        }

        state = QuizState.ASK_NUM_PLAYERS_AGAIN
        return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome)),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_playlist_info, playlist.name, playlist.tracks.size.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_num_players))
        ), true)
    }

    private fun askNumPlayersAgain() : InformationPacket {
        state = QuizState.ASK_NUM_PLAYERS_AGAIN
        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_not_understand)),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_num_players))
        ), true)
    }

    private fun askGameType() : InformationPacket {
        state = QuizState.ASK_GAME_TYPE
        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_num_players_selected, quizStanding.numPlayers.toString())),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
        ), true)
    }

    private fun askGameTypeAgain() : InformationPacket {
        state = QuizState.ASK_GAME_TYPE_AGAIN
        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type_invalid, quizType.name, playlist.tracks.size.toString(),
                (quizStanding.numPlayers * quizStanding.numRounds).toString(), quizStanding.numPlayers.toString())),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
        ), true)
    }

    private fun firstPlaySong() : InformationPacket {
        quizStanding.resetGame()

        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        } else{
            QuizState.PLAY_SONG
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_game_type_selected, quizType.name)),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_player_turn,
                quizStanding.getCurrentPlayerIndex().toString(), quizStanding.getCurrentRoundIndex().toString())),
            InformationItem(InfoType.SOUND, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
        ), true)
    }

    private fun playSong() : InformationPacket {

        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        } else{
            QuizState.PLAY_SONG
        }

        val lastPlayerPointsInfo = if(lastPlayerPoints > 0){
            var pointsForWhatString = ""
            pointsForWhatString += "$lastPlayerPoints ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)} "
            if(lastSongTitleHit){
                pointsForWhatString += "${context.getString(R.string.c_title)} and "
            }
            if(lastSongArtistHit){
                pointsForWhatString += "${context.getString(R.string.c_artist)} and "
            }
            if(lastSongAlbumHit){
                pointsForWhatString += "${context.getString(R.string.c_album)} and "
            }
            context.getString(R.string.c_player_good_guess, pointsForWhatString, lastSongTitle, lastSongArtist, lastSongAlbum, lastPlayerAllPoints.toString())
        }
        else{
            context.getString(R.string.c_player_failed_guess, lastSongTitle, lastSongArtist, lastSongAlbum, lastPlayerAllPoints.toString())
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, lastPlayerPointsInfo),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_player_turn,
                quizStanding.getCurrentPlayerIndex().toString(), quizStanding.getCurrentRoundIndex().toString())),
            InformationItem(InfoType.SOUND, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
        ), true)
    }

    private fun repeatSong() : InformationPacket {

        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        } else{
            QuizState.PLAY_SONG
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_repeating_song)),
            InformationItem(InfoType.SOUND, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
        ), true)
    }

    private fun endResultStringBuilder() : String{
        val results = quizStanding.scores
        var resultString = ""

        var winnerIndex = 0
        var maxValue = 0

        for((index, result) in results.withIndex()){
            resultString += context.getString(R.string.c_player_scored, (index + 1).toString(), result.toString()) + " "
            if(result > maxValue){
                maxValue = result
                winnerIndex = index
            }
            else if(result == maxValue){
                winnerIndex = -1
            }
        }

        resultString += if(winnerIndex >= 0){
            context.getString(R.string.c_winner_player, (winnerIndex+1).toString())
        } else{
            context.getString(R.string.c_winner_tie)
        }

        return resultString
    }

    private fun endGameAfterLastSong() : InformationPacket {
        state = QuizState.END

        val lastPlayerPointsInfo = if(lastPlayerPoints > 0){
            var pointsForWhatString = ""
            pointsForWhatString += "$lastPlayerPoints ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)} "
            if(lastSongTitleHit){
                pointsForWhatString += "${context.getString(R.string.c_title)} and "
            }
            if(lastSongArtistHit){
                pointsForWhatString += "${context.getString(R.string.c_artist)} and "
            }
            if(lastSongAlbumHit){
                pointsForWhatString += "${context.getString(R.string.c_album)} and "
            }
            context.getString(R.string.c_player_good_guess, pointsForWhatString, lastSongTitle, lastSongArtist, lastSongAlbum, lastPlayerAllPoints.toString())
        }
        else{
            context.getString(R.string.c_player_failed_guess, lastSongTitle, lastSongArtist, lastSongAlbum, lastPlayerAllPoints.toString())
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, lastPlayerPointsInfo),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_end_game)),
            InformationItem(InfoType.SPEECH, endResultStringBuilder()),
        ), false)
    }

    private fun endGame() : InformationPacket {
        state = QuizState.END
        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_end_game)),
            InformationItem(InfoType.SPEECH, endResultStringBuilder()),
        ), false)
    }

    /**
     * Update state based on user input
     *
     * @param probableSpeeches      ArrayList<String> of probable user inputs
     * @return Whether speak to user is needed
     */
    fun userInput(probableSpeeches : ArrayList<String>) : Boolean{
        // process what the user said
        val speakToUserNeeded = when(state){
            QuizState.ASK_NUM_PLAYERS_AGAIN -> parseNumPlayers(probableSpeeches)
            QuizState.ASK_GAME_TYPE -> parseGameType(probableSpeeches)
            QuizState.ASK_GAME_TYPE_AGAIN -> parseGameType(probableSpeeches)
            QuizState.FIRST_PLAY_SONG -> parseArtistTitle(probableSpeeches)
            QuizState.PLAY_SONG -> parseArtistTitle(probableSpeeches)
            QuizState.REPEAT_SONG -> parseArtistTitle(probableSpeeches)
            else -> false
        }
        return speakToUserNeeded
    }

    /**
     * Search for word occurrences
     *
     * @param probableSpeeches      ArrayList<String> of probable user inputs
     * @param searchedWords         Map<String, List<String>> of the searched word and the accepted variants
     * @param onlyOneNeeded         Whether only one match needed
     * @return List<String> containing the found words from the searched ones
     */
    private fun searchForWordOccurrences(probableSpeeches : ArrayList<String>,
                                         searchedWords: Map<String, List<String>>,
                                         onlyOneNeeded: Boolean) : List<String>{
        val wordsFound = mutableListOf<String>()
        for(speech in probableSpeeches) {
            val speechWords = speech.toLowerCase(Locale.ROOT).split(*CHARS_TO_SEPARATE_BY)
            for(speechWord in speechWords){
                for(searchedWord in searchedWords){
                    for(acceptedWordForm in searchedWord.value){
                        if(acceptedWordForm == speechWord){
                            wordsFound.add(searchedWord.key)
                            if(onlyOneNeeded){
                                return wordsFound
                            }
                        }
                    }
                }
            }
        }
        return wordsFound.distinct()
    }

    private fun isRepeatAsked(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "repeat" to listOf("repeat", "again")
        )
        return searchForWordOccurrences(probableSpeeches, possibleWords, true).isNotEmpty()
    }

    private fun parseNumPlayers(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "1" to listOf("one", "1"),
            "2" to listOf("two", "2"),
            "3" to listOf("three", "3"),
            "4" to listOf("four", "4")
        )

        val playersNumber = searchForWordOccurrences(probableSpeeches, possibleWords, true)
        if(playersNumber.isNotEmpty()){
            quizStanding.numPlayers = playersNumber[0].toInt()
            state = QuizState.ASK_GAME_TYPE
            return true
        }

        return true
    }

    private fun parseGameType(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "one-shot" to listOf("one", "1", "shot"),
            "short" to listOf("short"),
            "medium" to listOf("med", "medium"),
            "long" to listOf("long", "log")
        )

        val gameType = searchForWordOccurrences(probableSpeeches, possibleWords, true)

        var name = ""
        var numRounds = 0
        var pointForArtist = 0
        var pointForTrack = 0
        var pointForAlbum = 0
        var pointForSpeed = 0
        var repeatAllowed = false

        if(gameType.isNotEmpty()){
            when(gameType[0]){
                "one-shot" -> {
                    name = "one-shot"
                    numRounds = 1
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 1
                    pointForSpeed = 0
                    repeatAllowed = true
                }
                "short" -> {
                    name = "short"
                    numRounds = 3
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 1
                    pointForSpeed = 0
                    repeatAllowed = true
                }
                "medium" -> {
                    name = "medium"
                    numRounds = 7
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 1
                    pointForSpeed = 0
                    repeatAllowed = true
                }
                "long" -> {
                    name = "long"
                    numRounds = 10
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 1
                    pointForSpeed = 0
                    repeatAllowed = true
                }
                else -> {}
            }
        }
        quizType = QuizType(name, numRounds, pointForArtist, pointForTrack, pointForAlbum, pointForSpeed, repeatAllowed)
        quizStanding.numRounds = numRounds

        state = if(quizType.numRounds * quizStanding.numPlayers > playlist.tracks.size){
            QuizState.ASK_GAME_TYPE_AGAIN
        }
        else{
            QuizState.FIRST_PLAY_SONG
        }
        return true
    }

    private fun parseArtistTitle(probableSpeeches : ArrayList<String>) : Boolean{

        val currentTrack = playlist.tracks[quizStanding.currentTrackIndex]

        val albumParts = currentTrack.album.toLowerCase(Locale.ROOT).split(*CHARS_TO_SEPARATE_BY)
        val titleParts = currentTrack.name.toLowerCase(Locale.ROOT).split(*CHARS_TO_SEPARATE_BY)
        val artistParts = mutableListOf<String>()
        for(artist in currentTrack.artists){
            artistParts.addAll(artist.toLowerCase(Locale.ROOT).split(*CHARS_TO_SEPARATE_BY))
        }

        if(quizType.repeatAllowed){
            if(isRepeatAsked(probableSpeeches)){
                state = QuizState.REPEAT_SONG
                return true
            }
        }

        val possibleHitWords = mutableMapOf<String, List<String>>()
        possibleHitWords["album"] = albumParts
        possibleHitWords["title"] = titleParts
        possibleHitWords["artist"] = artistParts

        val playerHits = searchForWordOccurrences(probableSpeeches, possibleHitWords, false)

        var points = 0
        if(playerHits.contains("album")){
            points += quizType.pointForAlbum
            lastSongAlbumHit = true
        }
        else{
            lastSongAlbumHit = false
        }

        if(playerHits.contains("title")){
            points += quizType.pointForTitle
            lastSongTitleHit = true
        }
        else{
            lastSongTitleHit = false
        }

        if(playerHits.contains("artist")){
            points += quizType.pointForArtist
            lastSongArtistHit = true
        }
        else{
            lastSongArtistHit = false
        }

        lastSongAlbum = currentTrack.album
        lastSongTitle = currentTrack.name
        lastSongArtist = currentTrack.artists.toString().replace("[", "").replace("]", "")

        lastPlayerPoints = points
        lastPlayerAllPoints = quizStanding.getCurrentPlayerPoints() + points

        quizStanding.recordResult(points)

        state = if(quizStanding.isFinished){
            QuizState.END_LAST_SONG
        } else{
            QuizState.PLAY_SONG
        }
        return true
    }

}