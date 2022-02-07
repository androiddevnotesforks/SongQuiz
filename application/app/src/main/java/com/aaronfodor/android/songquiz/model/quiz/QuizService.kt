package com.aaronfodor.android.songquiz.model.quiz

import android.content.Context
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.TextParserService
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

enum class InfoType{
    SPEECH, SOUND_URL, SOUND_LOCAL_ID, EXIT_QUIZ
}

enum class QuizState{
    WELCOME,
    NUM_PLAYERS_NOT_UNDERSTOOD,
    GAME_TYPE_ASK,
    GAME_TYPE_NOT_UNDERSTOOD,
    GAME_TYPE_INVALID,
    START_GAME,
    PLAY_SONG,
    PLAY_SONG_REPEATABLE,
    END,
    END_REPEAT,
    RESTART_GAME,
    CONFIGURE_GAME,
    EXIT_GAME
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
    var quiz = Quiz()
    private val textParser = textParserService

    var songDurationSec = 0
    var difficultyCompensation = true
    var repeatSongAllowed = true
    var extendedInfoAllowed = false

    var lastPlayerArtistTitlePoints = 0
    var lastPlayerDifficultyPoints = 0
    var lastPlayerDifficultyPercentage = 0
    var lastPlayerAllPoints = 0
    var lastSongAlbum = ""
    var lastSongTitle = ""
    var lastSongArtist = ""
    var lastSongPopularity = 0
    var lastSongTitleHit = false
    var lastSongArtistHit = false

    fun clear(){
        lastSaidByUser = ""
        playlist = Playlist("")
        quiz = Quiz()
        state = QuizState.WELCOME
    }

    fun setStartQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quiz.clearState()
        state = QuizState.START_GAME
    }

    fun setConfigureQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quiz.clearState()
        state = QuizState.WELCOME
    }

    /**
     * Set the quiz playlist & settings
     */
    fun setQuizPlaylistAndSettings(playlistToPlay: Playlist, songDuration: Int, repeatAllowed: Boolean,
                                   difficultyCompensation: Boolean, extendedInfoAllowed: Boolean){
        this.playlist = playlistToPlay
        this.playlist.tracks.shuffle()
        // settings
        this.songDurationSec = songDuration
        this.repeatSongAllowed = repeatAllowed
        this.difficultyCompensation = difficultyCompensation
        this.extendedInfoAllowed = extendedInfoAllowed
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
            QuizState.PLAY_SONG_REPEATABLE -> playSong(true)
            QuizState.END -> endGame()
            QuizState.END_REPEAT -> endGame(true)
            QuizState.RESTART_GAME -> restartGame()
            QuizState.CONFIGURE_GAME -> configureGame()
            QuizState.EXIT_GAME -> exitGame()
            else -> InformationPacket(listOf(InformationItem(InfoType.SPEECH, context.getString(R.string.c_error))), false)
        }
        return response
    }

    private fun welcome(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(playlist.tracks.size < MIN_NUM_TRACKS){
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome, context.getString(R.string.app_name))),
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
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_welcome, context.getString(R.string.app_name))),
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
                    val quizTypeName = context.resources.getStringArray(quiz.type.nameStringKey)[0]
                    reasonText = context.getString(R.string.c_ask_game_type_invalid, quizTypeName, playlist.tracks.size.toString(),
                        (quiz.numPlayers * quiz.type.numRounds).toString(), quiz.numPlayers.toString())
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
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_num_players_selected, quiz.numPlayers.toString())),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_ask_game_type)),
            ), true)
        }
    }

    private fun firstTurnStringBuilder() : String{
        val currentPlayer = quiz.getCurrentPlayer()
        val quizTypeName = context.resources.getStringArray(quiz.type.nameStringKey)[0]
        return context.getString(R.string.c_starting_game, quizTypeName) + " " + context.getString(R.string.c_player_turn,
            currentPlayer.id.toString(), quiz.getCurrentRoundIndex().toString())
    }

    private fun startGame() : InformationPacket {
        quiz.clearState()

        state = if(quiz.type.repeatAllowed){
            QuizState.PLAY_SONG_REPEATABLE
        }
        else{
            QuizState.PLAY_SONG
        }

        var isRepeatAllowed = ""
        if(!quiz.type.repeatAllowed){
            isRepeatAllowed = context.getString(R.string.c_not)
        }

        var infoString = ""

        // add points info
        var pointsInfo = ""
        // title points
        pointsInfo += context.getString(R.string.c_points_for_the, quiz.type.pointForTitle.toString(), context.getString(R.string.c_title))
        // conjunction if needed
        pointsInfo += if(quiz.type.difficultyCompensation){
            ", "
        } else{
            "${context.getString(R.string.c_comma_and_and)} "
        }
        // artist points
        pointsInfo += context.getString(R.string.c_points_for_the, quiz.type.pointForArtist.toString(), context.getString(R.string.c_artist))
        // difficulty compensation and conjunction
        if(quiz.type.difficultyCompensation){
            pointsInfo += "${context.getString(R.string.c_comma_and_and)} "
            pointsInfo += context.getString(R.string.c_points_for_the, quiz.type.pointForDifficulty.toString(), context.getString(R.string.c_difficulty))
        }
        // merge the points info into one string
        infoString += context.getString(R.string.c_game_type_points, pointsInfo) + " "

        infoString += context.getString(R.string.c_settings_info, quiz.type.songDurationSec.toString(), isRepeatAllowed)
        infoString = infoString.replace("  ", " ")

        val quizTypeName = context.resources.getStringArray(quiz.type.nameStringKey)[0]

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, context.getString(R.string.c_game_type_selected, quizTypeName, quiz.type.numRounds.toString()) + " " + infoString),
            InformationItem(InfoType.SPEECH, firstTurnStringBuilder()),
            InformationItem(InfoType.SOUND_URL, playlist.tracks[quiz.currentTrackIndex].previewUri)
        ), true)
    }

    private fun guessFeedbackStringBuilder() : String{
        var resultString = ""

        if(lastPlayerArtistTitlePoints > 0){
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

            var pointsString = ""
            pointsString += context.getString(R.string.c_points_for_the, lastPlayerArtistTitlePoints.toString(), forWhatString)

            var difficultyString = ""
            if(quiz.type.difficultyCompensation){
                difficultyString += "${context.getString(R.string.c_comma_and_and)} "
                difficultyString += context.getString(R.string.c_difficulty_compensation, lastPlayerDifficultyPoints.toString(), lastPlayerDifficultyPercentage.toString())
            }
            pointsString += difficultyString

            val goodGuessPrefix = context.resources.getStringArray(R.array.good_guess_prefixes).random()
            resultString = context.getString(R.string.c_player_good_guess, goodGuessPrefix, pointsString)
        }
        else{
            val badGuessPrefix = context.resources.getStringArray(R.array.failed_guess_prefixes).random()

            var pointsString = ""
            if(quiz.type.difficultyCompensation){
                pointsString += context.getString(R.string.c_difficulty_compensation, lastPlayerDifficultyPoints.toString(), lastPlayerDifficultyPercentage.toString())
                resultString = context.getString(R.string.c_player_bad_guess, badGuessPrefix, pointsString)
            }
            else{
                resultString = badGuessPrefix
            }
        }

        var songInfoString = context.getString(R.string.c_song_info, lastSongTitle, lastSongArtist) + " "
        if(extendedInfoAllowed){
            // extended info
            songInfoString += context.getString(R.string.c_song_extended_info, lastSongAlbum) + " "
        }
        songInfoString += context.getString(R.string.c_your_score, lastPlayerAllPoints.toString())

        return "$resultString $songInfoString".replace("  ", " ")
    }

    private fun playSong(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {

        state = if(quiz.type.repeatAllowed){
            QuizState.PLAY_SONG_REPEATABLE
        }
        else{
            QuizState.PLAY_SONG
        }

        if(isRepeat){
            return InformationPacket(listOf(
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_repeating_song)),
                InformationItem(InfoType.SOUND_URL, playlist.tracks[quiz.currentTrackIndex].previewUri)
            ), true)
        }
        else{
            val previousGuessString = guessFeedbackStringBuilder()

            val localSoundName = if(lastPlayerArtistTitlePoints <= 0){
                SAD_SOUND_NAME
            }
            else{
                HAPPY_SOUND_NAME
            }

            val currentPlayer = quiz.getCurrentPlayer()
            return InformationPacket(listOf(
                InformationItem(InfoType.SOUND_LOCAL_ID, localSoundName),
                InformationItem(InfoType.SPEECH, previousGuessString),
                InformationItem(InfoType.SPEECH, context.getString(R.string.c_player_turn,
                    currentPlayer.id.toString(), quiz.getCurrentRoundIndex().toString())),
                InformationItem(InfoType.SOUND_URL, playlist.tracks[quiz.currentTrackIndex].previewUri)
            ), true)
        }
    }

    private fun playFirstSong() : InformationPacket {
        state = if(quiz.type.repeatAllowed){
            QuizState.PLAY_SONG_REPEATABLE
        }
        else{
            QuizState.PLAY_SONG
        }

        return InformationPacket(listOf(
            InformationItem(InfoType.SPEECH, firstTurnStringBuilder()),
            InformationItem(InfoType.SOUND_URL, playlist.tracks[quiz.currentTrackIndex].previewUri)
        ), true)
    }

    private fun endResultStringBuilder() : String{
        val quizPlayers = quiz.players
        var resultString = ""

        var winnerId = -1
        var winnerPoints = 0

        for(player in quizPlayers){
            val currentPlayerPoints = player.getPoints(quiz.type.difficultyCompensation)
            resultString += context.getString(R.string.c_player_scored, (player.id).toString(), currentPlayerPoints.toString()) + " "
            if(currentPlayerPoints > winnerPoints){
                winnerPoints = currentPlayerPoints
                winnerId = player.id
            }
            else if(currentPlayerPoints == winnerPoints){
                winnerId = -1
            }
        }

        resultString += when {
            winnerPoints <= 0 -> {
                context.getString(R.string.c_next_time)
            }
            winnerId >= 0 -> {
                context.getString(R.string.c_winner_player, winnerId.toString())
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
            val previousGuessString = guessFeedbackStringBuilder()

            val localSoundName = if(lastPlayerArtistTitlePoints <= 0){
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

    private fun exitGame() : InformationPacket {
        clear()
        return InformationPacket(listOf(InformationItem(InfoType.EXIT_QUIZ, "")), false)
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
            QuizState.START_GAME -> parseGuess(probableSpeeches)
            QuizState.PLAY_SONG -> parseGuess(probableSpeeches)
            QuizState.PLAY_SONG_REPEATABLE -> parseGuess(probableSpeeches)
            QuizState.END_REPEAT -> parseAfterFinishedCommand(probableSpeeches)
            else -> false
        }
        return speakToUserNeeded
    }

    enum class KeyNumPlayers(val value: String){
        ONE("1"),
        TWO("2"),
        THREE("3"),
        FOUR("4")
    }

    private fun parseNumPlayers(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            KeyNumPlayers.ONE.value to context.resources.getStringArray(R.array.input_1).toList(),
            KeyNumPlayers.TWO.value to context.resources.getStringArray(R.array.input_2).toList(),
            KeyNumPlayers.THREE.value to context.resources.getStringArray(R.array.input_3).toList(),
            KeyNumPlayers.FOUR.value to context.resources.getStringArray(R.array.input_4).toList()
        )

        val numPlayers = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(numPlayers.isEmpty()){
            state = QuizState.NUM_PLAYERS_NOT_UNDERSTOOD
        }
        else{
            quiz.numPlayers = numPlayers[0].toInt()
            state = QuizState.GAME_TYPE_ASK
        }

        return true
    }

    enum class KeyGameType(val value: String){
        ONE_SHOT("one-shot"),
        SHORT("short"),
        MEDIUM("medium"),
        LONG("long")
    }

    private fun parseGameType(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            KeyGameType.ONE_SHOT.value to context.resources.getStringArray(R.array.input_oneshot).toList(),
            KeyGameType.SHORT.value to context.resources.getStringArray(R.array.input_short).toList(),
            KeyGameType.MEDIUM.value to context.resources.getStringArray(R.array.input_medium).toList(),
            KeyGameType.LONG.value to context.resources.getStringArray(R.array.input_long).toList()
        )

        val gameType = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(gameType.isNotEmpty()){
            when(gameType[0]){
                KeyGameType.ONE_SHOT.value -> {
                    quiz.type = OneShotQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed)
                }
                KeyGameType.SHORT.value -> {
                    quiz.type = ShortQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed)
                }
                KeyGameType.MEDIUM.value -> {
                    quiz.type = MediumQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed)
                }
                KeyGameType.LONG.value -> {
                    quiz.type = LongQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed)
                }
                else -> {}
            }
        }
        else{
            // Game type not caught
            state = QuizState.GAME_TYPE_NOT_UNDERSTOOD
            return true
        }

        state = if(quiz.type.numRounds * quiz.numPlayers > playlist.tracks.size){
            QuizState.GAME_TYPE_INVALID
        }
        else{
            QuizState.START_GAME
        }

        return true
    }

    enum class KeyGuess(val value: String){
        ARTIST("artist"),
        TITLE("title"),
        REPEAT("repeat")
    }

    private fun parseGuess(probableSpeeches : ArrayList<String>) : Boolean{
        val currentTrack = playlist.tracks[quiz.currentTrackIndex]

        val artistParts = mutableListOf<String>()
        for(artist in currentTrack.artists){
            artistParts.addAll(textParser.normalizeText(artist))
        }
        val titleParts = textParser.normalizeText(currentTrack.name)

        val possibleHitWords = mutableMapOf<String, List<String>>()
        possibleHitWords[KeyGuess.ARTIST.value] = artistParts
        possibleHitWords[KeyGuess.TITLE.value] = titleParts
        // extra command
        possibleHitWords[KeyGuess.REPEAT.value] = context.resources.getStringArray(R.array.input_repeat).toList()

        val playerHits = textParser.searchForWordOccurrences(probableSpeeches, possibleHitWords, false)

        // if repeat allowed
        if(quiz.type.repeatAllowed){
            // if only the extra command has been identified, execute it
            if(playerHits.size == 1 && playerHits.contains(KeyGuess.REPEAT.value)){
                state = QuizState.PLAY_SONG_REPEATABLE
                return true
            }
        }

        var artistPoint = 0
        var titlePoint = 0
        var difficultyCompensationPoint = 0

        if(playerHits.contains(KeyGuess.ARTIST.value)){
            artistPoint = quiz.type.pointForArtist
            lastSongArtistHit = true
        }
        else{
            lastSongArtistHit = false
        }

        if(playerHits.contains(KeyGuess.TITLE.value)){
            titlePoint = quiz.type.pointForTitle
            lastSongTitleHit = true
        }
        else{
            lastSongTitleHit = false
        }

        val compensationRatio = (1.0 - (currentTrack.popularity.toDouble() / 100.0))
        difficultyCompensationPoint = ((quiz.type.pointForDifficulty) * compensationRatio).roundToInt()

        lastSongTitle = currentTrack.name
        lastSongArtist = textParser.stringListToString(currentTrack.artists)
        lastSongAlbum = currentTrack.album
        lastSongPopularity = currentTrack.popularity

        lastPlayerArtistTitlePoints = artistPoint + titlePoint
        if(quiz.type.difficultyCompensation){
            lastPlayerDifficultyPoints = difficultyCompensationPoint
            lastPlayerDifficultyPercentage = (compensationRatio * 100.0).roundToInt()
        }

        val currentPlayerPoints = quiz.getCurrentPlayer().getPoints(quiz.type.difficultyCompensation)
        lastPlayerAllPoints = currentPlayerPoints
        lastPlayerAllPoints += lastPlayerArtistTitlePoints + lastPlayerDifficultyPoints

        quiz.recordResult(artistPoint, titlePoint, difficultyCompensationPoint)

        state = if(quiz.isFinished){
            QuizState.END
        }
        else{
            QuizState.PLAY_SONG
        }

        return true
    }

    enum class KeyAfterFinishedCommand(val value: String){
        RESTART("restart"),
        CONFIGURE("configure"),
        EXIT("exit")
    }

    private fun parseAfterFinishedCommand(probableSpeeches : ArrayList<String>) : Boolean{
        val possibleWords = mapOf(
            KeyAfterFinishedCommand.RESTART.value to context.resources.getStringArray(R.array.input_restart).toList(),
            KeyAfterFinishedCommand.CONFIGURE.value to context.resources.getStringArray(R.array.input_configure).toList(),
            KeyAfterFinishedCommand.EXIT.value to context.resources.getStringArray(R.array.input_exit).toList(),
        )

        val whatAsked = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(whatAsked.isEmpty()){
            return false
        }

        when(whatAsked[0]){
            KeyAfterFinishedCommand.RESTART.value -> state = QuizState.RESTART_GAME
            KeyAfterFinishedCommand.CONFIGURE.value -> state = QuizState.CONFIGURE_GAME
            KeyAfterFinishedCommand.EXIT.value -> state = QuizState.EXIT_GAME
        }

        return true
    }

}