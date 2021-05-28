package com.arpadfodor.android.songquiz.model

import android.content.Context
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.model.quiz.QuizStanding
import com.arpadfodor.android.songquiz.model.quiz.QuizType
import com.arpadfodor.android.songquiz.model.quiz.TextConverter
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
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
    SONG_DURATION_ASK,
    SONG_DURATION_NOT_UNDERSTOOD,
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
    var textConverter = TextConverter()
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

    fun clearState(){
        state = QuizState.WELCOME
        lastSaidByUser = ""
        playlist = Playlist("")
        quizType = QuizType()
        quizStanding = QuizStanding()
    }

    private fun reset(){
        state = QuizState.WELCOME
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quizType = QuizType()
        quizStanding = QuizStanding()
    }

    private fun restart(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quizStanding.resetGame()
    }

    /**
     * Set the quiz playlist
     */
    fun setQuizPlaylist(playlistToPlay: Playlist){
        playlist = playlistToPlay
        playlist.tracks.shuffle()
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
            QuizState.SONG_DURATION_ASK -> askSongDuration()
            QuizState.SONG_DURATION_NOT_UNDERSTOOD -> askSongDuration(true, RepeatCause.NOT_UNDERSTOOD)
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

    private fun askSongDuration(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = context.getString(R.string.c_not_understand)
                }
                else -> {}
            }

            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, reasonText),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_song_duration))
            ), true)
        }
        else{
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_num_players_selected, quizStanding.numPlayers.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_song_duration))
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
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_song_duration_selected, songDurationSec.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
            ), true)
        }
    }

    private fun firstTurnStringBuilder() : String{
        return context.getString(R.string.c_starting_game, quizType.name) + " " + context.getString(R.string.c_player_turn,
            quizStanding.getCurrentPlayerIndex().toString(), quizStanding.getCurrentRoundIndex().toString())
    }

    private fun startGame() : InformationPacket {
        quizStanding.resetGame()

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
        infoString += context.getString(R.string.c_repeat_info, isRepeatAllowed)

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
            resultString = context.getString(R.string.c_player_good_guess, "$pointsString $forWhatString")
        }
        else{
            resultString = context.getString(R.string.c_player_failed_guess)
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
        restart()
        return playFirstSong()
    }

    private fun configureGame() : InformationPacket {
        reset()
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
            QuizState.SONG_DURATION_ASK -> parseSongDuration(probableSpeeches)
            QuizState.SONG_DURATION_NOT_UNDERSTOOD -> parseSongDuration(probableSpeeches)
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
            val speechWords = textConverter.normalizeText(speech)
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

    private fun parseNumPlayers(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "1" to listOf("one", "1", "van"),
            "2" to listOf("two", "2", "do", "to"),
            "3" to listOf("three", "3", "tree"),
            "4" to listOf("four", "4", "for")
        )

        val playersNumber = searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(playersNumber.isEmpty()){
            state = QuizState.NUM_PLAYERS_NOT_UNDERSTOOD
        }
        else{
            quizStanding.numPlayers = playersNumber[0].toInt()
            state = QuizState.SONG_DURATION_ASK
        }

        return true
    }

    private fun parseSongDuration(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            "short" to listOf("short"),
            "medium" to listOf("med", "medium"),
            "long" to listOf("long", "log")
        )

        val songDuration = searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(songDuration.isEmpty()){
            // Song duration not caught
            state = QuizState.SONG_DURATION_NOT_UNDERSTOOD
            return true
        }

        when(songDuration[0]){
            "short" -> songDurationSec = 10
            "medium" -> songDurationSec = 20
            "long" -> songDurationSec = 30
        }

        state = QuizState.GAME_TYPE_ASK
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
        var repeatAllowed = false

        if(gameType.isNotEmpty()){
            when(gameType[0]){
                "one-shot" -> {
                    name = "one-shot"
                    numRounds = 1
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                    repeatAllowed = true
                }
                "short" -> {
                    name = "short"
                    numRounds = 3
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                    repeatAllowed = true
                }
                "medium" -> {
                    name = "medium"
                    numRounds = 5
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                    repeatAllowed = true
                }
                "long" -> {
                    name = "long"
                    numRounds = 7
                    pointForArtist = 10
                    pointForTrack = 10
                    pointForAlbum = 2
                    repeatAllowed = true
                }
                else -> {}
            }
        }
        else{
            // Game type not caught
            state = QuizState.GAME_TYPE_NOT_UNDERSTOOD
            return true
        }

        quizType = QuizType(name, numRounds, pointForArtist, pointForTrack, pointForAlbum, repeatAllowed)
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
            artistParts.addAll(textConverter.normalizeText(artist))
        }
        val titleParts = textConverter.normalizeText(currentTrack.name)
        val albumParts = textConverter.normalizeText(currentTrack.album)

        val possibleHitWords = mutableMapOf<String, List<String>>()
        possibleHitWords["artist"] = artistParts
        possibleHitWords["title"] = titleParts
        possibleHitWords["album"] = albumParts
        // repeat command
        possibleHitWords["repeat"] = listOf("repeat", "again")

        val playerHits = searchForWordOccurrences(probableSpeeches, possibleHitWords, false)

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
        lastSongArtist = textConverter.stringListToString(currentTrack.artists)
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

        val whatAsked = searchForWordOccurrences(probableSpeeches, possibleWords, true)

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