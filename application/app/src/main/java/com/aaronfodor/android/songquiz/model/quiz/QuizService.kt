package com.aaronfodor.android.songquiz.model.quiz

import android.content.Context
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.TextParserService
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlin.collections.ArrayList

enum class InfoType{
    SPEECH, SOUND_URL, SOUND_LOCAL_ID
}

enum class QuizState{
    WELCOME,
    NUM_PLAYERS_NOT_UNDERSTOOD,
    GAME_TYPE_ASK,
    GAME_TYPE_NOT_UNDERSTOOD,
    GAME_TYPE_INVALID,
    START_GAME,
    PLAY_SONG,
    REPEAT_SONG,
    END,
    END_REPEAT,
    RESTART_GAME,
    CONFIGURE_GAME
}

enum class RepeatCause{
    NOT_UNDERSTOOD, INVALID_INPUT, RECONFIGURE_GAME, NOTHING
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
class QuizService @Inject constructor(
    @ApplicationContext val context: Context,
    textParserService: TextParserService
){

    companion object{
        const val MIN_NUM_TRACKS = 4
        const val SAD_SOUND_NAME = "sad.wav"
        const val HAPPY_SOUND_NAME = "happy.wav"
        const val END_SOUND_NAME = "end.wav"
    }

    var state = QuizState.WELCOME
    var lastSaidByUser = ""
    var playlist = Playlist("")
    var quizType = QuizType()
    var quizStanding = QuizStanding()
    private val textParser = textParserService

    var repeatSongAllowed = false
    var songDurationSec = 0

    var lastPlayerPoints = 0
    var lastPlayerAllPoints = 0
    var lastSongAlbum = ""
    var lastSongTitle = ""
    var lastSongArtist = ""
    var lastSongPopularity = 0
    var lastSongAlbumHit = false
    var lastSongTitleHit = false
    var lastSongArtistHit = false

    fun clear(){
        lastSaidByUser = ""
        playlist = Playlist("")
        quizType = QuizType()
        quizStanding = QuizStanding()
        state = QuizState.WELCOME
    }

    fun setStartQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quizStanding.clearState()
        state = QuizState.START_GAME
    }

    fun setConfigureQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quizStanding.clearState()
        state = QuizState.WELCOME
    }

    /**
     * Set the quiz playlist & settings
     */
    fun setQuizPlaylistAndSettings(playlistToPlay: Playlist, repeatAllowed: Boolean, songDuration: Int){
        playlist = playlistToPlay
        playlist.tracks.shuffle()
        repeatSongAllowed = repeatAllowed
        songDurationSec = songDuration
    }

    /**
     * Returns the current info to the user
     *
     * @return an InformationPacket
     */
    fun getCurrentInfo() : InformationPacket {
        val response = when(state){
            QuizState.WELCOME -> welcome()
            QuizState.NUM_PLAYERS_NOT_UNDERSTOOD -> welcome(true, RepeatCause.NOT_UNDERSTOOD)
            QuizState.GAME_TYPE_ASK -> askGameType()
            QuizState.GAME_TYPE_NOT_UNDERSTOOD -> askGameType(true, RepeatCause.NOT_UNDERSTOOD)
            QuizState.GAME_TYPE_INVALID -> askGameType(true, RepeatCause.INVALID_INPUT)
            QuizState.START_GAME -> startGame()
            QuizState.PLAY_SONG -> playSong()
            QuizState.REPEAT_SONG -> playSong(true)
            QuizState.END -> endGame()
            QuizState.END_REPEAT -> endGame(true)
            QuizState.RESTART_GAME -> restartGame()
            QuizState.CONFIGURE_GAME -> configureGame()
            else -> InformationPacket(listOf(InformationItem(InfoType.SPEECH, context.getString(R.string.c_error))), false)
        }
        return response
    }

    private fun welcome(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(playlist.tracks.size < MIN_NUM_TRACKS){
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome)),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_playlist_info, playlist.name, playlist.tracks.size.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_not_enough_tracks, MIN_NUM_TRACKS.toString()))
            ), false)
        }

        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = context.getString(R.string.c_not_understand)
                }
                RepeatCause.RECONFIGURE_GAME -> {
                    reasonText = context.getString(R.string.c_reconfigure, playlist.name, playlist.tracks.size.toString())
                }
                else -> {}
            }

            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, reasonText),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_num_players))
            ), true)
        }
        else{
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome)),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_playlist_info, playlist.name, playlist.tracks.size.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_num_players))
            ), true)
        }
    }

    private fun askGameType(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.INVALID_INPUT -> {
                    reasonText = context.getString(R.string.c_ask_game_type_invalid, quizType.name, playlist.tracks.size.toString(),
                        (quizStanding.numPlayers * quizStanding.numRounds).toString(), quizStanding.numPlayers.toString())
                }
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = context.getString(R.string.c_not_understand)
                }
                else -> {}
            }

            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, reasonText),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
            ), true)
        }
        else{
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_num_players_selected, quizStanding.numPlayers.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
            ), true)
        }
    }

    private fun firstTurnStringBuilder() : String{
        return context.getString(R.string.c_starting_game, quizType.name) + " " + context.getString(R.string.c_player_turn,
            quizStanding.getCurrentPlayerIndex().toString(), quizStanding.getCurrentRoundIndex().toString())
    }

    private fun startGame() : InformationPacket {
        quizStanding.clearState()

        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        } else{
            QuizState.PLAY_SONG
        }

        var isRepeatAllowed = ""
        if(!quizType.repeatAllowed){
            isRepeatAllowed = " ${context.getString(R.string.c_not)}"
        }

        var infoString = ""
        infoString += "${quizType.pointForTitle} ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)} ${context.getString(R.string.c_title)}, "
        infoString += "${quizType.pointForArtist} ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)} ${context.getString(R.string.c_artist)}, "
        infoString += "${context.getString(R.string.c_and)} ${quizType.pointForAlbum} ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)} ${context.getString(R.string.c_album)}. "
        infoString += context.getString(R.string.c_settings_info, quizType.songDurationSec.toString(), isRepeatAllowed)

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_game_type_selected, quizType.name, quizType.numRounds.toString(), infoString)),
            InformationItem(InfoType.SPEECH, firstTurnStringBuilder()),
            InformationItem(InfoType.SOUND_URL, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
        ), true)
    }

    private fun lastGuessStringBuilder() : String{
        var resultString = ""
        if(lastPlayerPoints > 0){
            var pointsString = ""
            pointsString += "$lastPlayerPoints ${context.getString(R.string.c_points)} ${context.getString(R.string.c_for)}"

            var forWhatString = ""
            if(lastSongTitleHit){
                forWhatString += context.getString(R.string.c_title)
            }
            if(lastSongArtistHit){
                if(forWhatString.isNotEmpty()){
                    forWhatString += " ${context.getString(R.string.c_and)} "
                }
                forWhatString += context.getString(R.string.c_artist)
            }
            if(lastSongAlbumHit){
                if(forWhatString.isNotEmpty()){
                    forWhatString += " ${context.getString(R.string.c_and)} ${context.getString(R.string.c_few_extra)} ${context.getString(R.string.c_for)} "
                }
                forWhatString += context.getString(R.string.c_album)
            }
            val goodGuessPrefix = context.resources.getStringArray(R.array.good_guess_prefixes).random()
            resultString = context.getString(R.string.c_player_good_guess, goodGuessPrefix, "$pointsString $forWhatString")
        }
        else{
            resultString = context.resources.getStringArray(R.array.failed_guess_prefixes).random()
        }

        val songWasString = context.getString(R.string.c_what_song_was, lastSongTitle, lastSongArtist, lastSongAlbum,
                lastSongPopularity.toString(), lastPlayerAllPoints.toString())
        return "$resultString $songWasString"
    }

    private fun playSong(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {

        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        }
        else{
            QuizState.PLAY_SONG
        }

        if(isRepeat){
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_repeating_song)),
                InformationItem(InfoType.SOUND_URL, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
            ), true)
        }
        else{
            val previousGuessString = lastGuessStringBuilder()

            val localSoundName = if(lastPlayerPoints <= 0){
                SAD_SOUND_NAME
            }
            else{
                HAPPY_SOUND_NAME
            }

            return InformationPacket(listOf(
                InformationItem(InfoType.SOUND_LOCAL_ID, localSoundName),
                InformationItem(InfoType.SPEECH, previousGuessString),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_player_turn,
                    quizStanding.getCurrentPlayerIndex().toString(), quizStanding.getCurrentRoundIndex().toString())),
                InformationItem(InfoType.SOUND_URL, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
            ), true)
        }
    }

    private fun playFirstSong() : InformationPacket {
        state = if(quizType.repeatAllowed){
            QuizState.REPEAT_SONG
        }
        else{
            QuizState.PLAY_SONG
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, firstTurnStringBuilder()),
            InformationItem(InfoType.SOUND_URL, playlist.tracks[quizStanding.currentTrackIndex].previewUri)
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

        resultString += when {
            maxValue <= 0 -> {
                context.getString(R.string.c_next_time)
            }
            winnerIndex >= 0 -> {
                context.getString(R.string.c_winner_player, (winnerIndex+1).toString())
            }
            else -> {
                context.getString(R.string.c_winner_tie)
            }
        }

        return resultString
    }

    private fun endGameInformationBuilder() : List<InformationItem>{
        return listOf(
            InformationItem(InfoType.SPEECH, endResultStringBuilder()),
            InformationItem(InfoType.SOUND_LOCAL_ID, END_SOUND_NAME),
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_play_again_possible))
            )
    }

    private fun endGame(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        state = QuizState.END_REPEAT

        if(isRepeat){
            return InformationPacket(endGameInformationBuilder(), true)
        }
        else{
            val previousGuessString = lastGuessStringBuilder()

            val localSoundName = if(lastPlayerPoints <= 0){
                SAD_SOUND_NAME
            }
            else{
                HAPPY_SOUND_NAME
            }

            val informationItems = mutableListOf(
                InformationItem(InfoType.SOUND_LOCAL_ID, localSoundName),
                InformationItem(InfoType.SPEECH, previousGuessString),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_end_game)))
            informationItems.addAll(endGameInformationBuilder())

            return InformationPacket(informationItems, true)
        }
    }

    private fun restartGame() : InformationPacket {
        setStartQuizState()
        return playFirstSong()
    }

    private fun configureGame() : InformationPacket {
        setConfigureQuizState()
        return welcome(true, RepeatCause.RECONFIGURE_GAME)
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
            QuizState.WELCOME -> parseNumPlayers(probableSpeeches)
            QuizState.NUM_PLAYERS_NOT_UNDERSTOOD -> parseNumPlayers(probableSpeeches)
            QuizState.GAME_TYPE_ASK -> parseGameType(probableSpeeches)
            QuizState.GAME_TYPE_NOT_UNDERSTOOD -> parseGameType(probableSpeeches)
            QuizState.GAME_TYPE_INVALID -> parseGameType(probableSpeeches)
            QuizState.START_GAME -> parseArtistTitleAlbum(probableSpeeches)
            QuizState.PLAY_SONG -> parseArtistTitleAlbum(probableSpeeches)
            QuizState.REPEAT_SONG -> parseArtistTitleAlbum(probableSpeeches)
            QuizState.END_REPEAT -> parseRepeatGame(probableSpeeches)
            else -> false
        }
        return speakToUserNeeded
    }

    private fun parseNumPlayers(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "1" to listOf("one", "1", "van"),
            "2" to listOf("two", "2", "do", "to"),
            "3" to listOf("three", "3", "tree"),
            "4" to listOf("four", "4", "for")
        )

        val numPlayers = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(numPlayers.isEmpty()){
            state = QuizState.NUM_PLAYERS_NOT_UNDERSTOOD
        }
        else{
            quizStanding.numPlayers = numPlayers[0].toInt()
            state = QuizState.GAME_TYPE_ASK
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

        val gameType = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        var name = ""
        var numRounds = 0
        var pointForArtist = 0
        var pointForTrack = 0
        var pointForAlbum = 0

        if(gameType.isNotEmpty()){
            when(gameType[0]){
                "one-shot" -> {
                    name = "one-shot"
                    numRounds = 1
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                }
                "short" -> {
                    name = "short"
                    numRounds = 3
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                }
                "medium" -> {
                    name = "medium"
                    numRounds = 5
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                }
                "long" -> {
                    name = "long"
                    numRounds = 7
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                }
                else -> {}
            }
        }
        else{
            // Game type not caught
            state = QuizState.GAME_TYPE_NOT_UNDERSTOOD
            return true
        }

        quizType = QuizType(name, numRounds, pointForArtist, pointForTrack, pointForAlbum, repeatSongAllowed, songDurationSec)
        quizStanding.numRounds = numRounds

        state = if(quizType.numRounds * quizStanding.numPlayers > playlist.tracks.size){
            QuizState.GAME_TYPE_INVALID
        }
        else{
            QuizState.START_GAME
        }

        return true
    }

    private fun parseArtistTitleAlbum(probableSpeeches : ArrayList<String>) : Boolean{
        val currentTrack = playlist.tracks[quizStanding.currentTrackIndex]

        val artistParts = mutableListOf<String>()
        for(artist in currentTrack.artists){
            artistParts.addAll(textParser.normalizeText(artist))
        }
        val titleParts = textParser.normalizeText(currentTrack.name)
        val albumParts = textParser.normalizeText(currentTrack.album)

        val possibleHitWords = mutableMapOf<String, List<String>>()
        possibleHitWords["artist"] = artistParts
        possibleHitWords["title"] = titleParts
        possibleHitWords["album"] = albumParts
        // repeat command
        possibleHitWords["repeat"] = listOf("repeat", "again")

        val playerHits = textParser.searchForWordOccurrences(probableSpeeches, possibleHitWords, false)

        // if repeat allowed
        if(quizType.repeatAllowed){
            // if only the repeat command has been identified, repeat
            if(playerHits.size == 1 && playerHits.contains("repeat")){
                state = QuizState.REPEAT_SONG
                return true
            }
        }

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
        lastSongArtist = textParser.stringListToString(currentTrack.artists)
        lastSongPopularity = currentTrack.popularity

        lastPlayerPoints = points
        lastPlayerAllPoints = quizStanding.getCurrentPlayerPoints() + points

        quizStanding.recordResult(points)

        state = if(quizStanding.isFinished){
            QuizState.END
        }
        else{
            QuizState.PLAY_SONG
        }

        return true
    }

    private fun parseRepeatGame(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "restart" to listOf("restart", "repeat", "again"),
            "configure" to listOf("configure", "config", "conf", "con"),
        )

        val whatAsked = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(whatAsked.isEmpty()){
            return false
        }

        when(whatAsked[0]){
            "restart" -> state = QuizState.RESTART_GAME
            "configure" -> state = QuizState.CONFIGURE_GAME
        }

        return true
    }

}