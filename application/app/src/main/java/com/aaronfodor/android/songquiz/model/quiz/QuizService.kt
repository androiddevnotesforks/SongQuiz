package com.aaronfodor.android.songquiz.model.quiz

import com.aaronfodor.android.songquiz.model.TextParserService
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

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
 * Injected to bind lifecycle to a ViewModel scope
 */
@ViewModelScoped
class QuizService @Inject constructor(
    stringService: QuizStringHandler,
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
    private val stringService = stringService
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

    fun getQuizState() : Quiz{
        return quiz
    }

    fun setClearQuizState(){
        lastSaidByUser = ""
        playlist = Playlist("")
        quiz = Quiz()
        state = QuizState.WELCOME
    }

    fun setConfigureQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quiz = Quiz()
        state = QuizState.WELCOME
    }

    fun setQuizPlayers(numPlayers: Int, names: List<String>){
        quiz.setPlayers(numPlayers, listOf(
            stringService.player(1),
            stringService.player(2),
            stringService.player(3),
            stringService.player(4),
        ))
    }

    fun setQuizType(type: QuizType){
        quiz.setQuizType(type)
    }

    fun setStartQuizState(){
        lastSaidByUser = ""
        playlist.tracks.shuffle()
        quiz.startState()
        state = QuizState.START_GAME
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
            QuizState.WELCOME -> welcomeAndAskNumPlayers()
            QuizState.NUM_PLAYERS_NOT_UNDERSTOOD -> welcomeAndAskNumPlayers(true, RepeatCause.NOT_UNDERSTOOD)
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
            else -> InformationPacket(listOf(Speech(stringService.error())), false)
        }
        return response
    }

    private fun welcomeAndAskNumPlayers(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(playlist.tracks.size < MIN_NUM_TRACKS){
            return InformationPacket(listOf(
                Speech(stringService.welcome()),
                Speech(stringService.playlistInfo(playlist.name, playlist.tracks.size)),
                Speech(stringService.notEnoughTracks(MIN_NUM_TRACKS))
            ), false)
        }

        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = stringService.notUnderstand()
                }
                RepeatCause.RECONFIGURE_GAME -> {
                    reasonText = stringService.reconfigure(playlist.name, playlist.tracks.size)
                }
                else -> {}
            }

            return InformationPacket(listOf(
                Speech(reasonText),
                Speech(stringService.askNumPlayers())
            ), true)
        }
        else{
            return InformationPacket(listOf(
                Speech(stringService.welcome()),
                Speech(stringService.playlistInfo(playlist.name, playlist.tracks.size)),
                Speech(stringService.askNumPlayers())
            ), true)
        }
    }

    private fun askGameType(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.INVALID_INPUT -> {
                    reasonText = stringService.askGameTypeInvalid(quiz.type.typeNameStringKey, playlist.tracks.size, quiz.numPlayers, quiz.type.numRounds)
                }
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = stringService.notUnderstand()
                }
                else -> {}
            }

            return InformationPacket(listOf(
                Speech(reasonText),
                Speech(stringService.askGameType()),
            ), true)
        }
        else{
            return InformationPacket(listOf(
                Speech(stringService.numPlayersSelected(quiz.numPlayers)),
                Speech(stringService.askGameType()),
            ), true)
        }
    }

    private fun startGame() : InformationPacket {
        setStartQuizState()

        state = if(quiz.type.repeatAllowed){
            QuizState.PLAY_SONG_REPEATABLE
        }
        else{
            QuizState.PLAY_SONG
        }

        val selectedGameAndSettingsText = stringService.selectedGameAndSettingsString(extendedInfoAllowed, quiz.type.repeatAllowed, quiz.type.difficultyCompensation,
            quiz.type.pointForTitle, quiz.type.pointForArtist, quiz.type.pointForDifficulty, quiz.type.songDurationSec, quiz.type.numRounds, quiz.type.typeNameStringKey)

        return InformationPacket(listOf(
            Speech(selectedGameAndSettingsText),
            Speech(stringService.firstTurnString(quiz.getCurrentPlayer().name, quiz.type.typeNameStringKey, quiz.getCurrentRoundIndex())),
            SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUri)
        ), true)
    }

    private fun playSong(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING) : InformationPacket {
        state = if(quiz.type.repeatAllowed){
            QuizState.PLAY_SONG_REPEATABLE
        }
        else{
            QuizState.PLAY_SONG
        }

        if(isRepeat){
            return InformationPacket(listOf(
                Speech(stringService.repeatSong()),
                SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUri)
            ), true)
        }
        else{
            val guessString = stringService.guessFeedbackString(lastPlayerArtistTitlePoints, lastSongTitleHit, lastSongArtistHit,
                lastPlayerDifficultyPoints, lastPlayerDifficultyPercentage, lastSongTitle, lastSongArtist, lastSongAlbum,
                extendedInfoAllowed, lastPlayerAllPoints, quiz.type.difficultyCompensation)

            val localSoundName = if(lastPlayerArtistTitlePoints <= 0){
                SAD_SOUND_NAME
            }
            else{
                HAPPY_SOUND_NAME
            }

            val currentPlayer = quiz.getCurrentPlayer()

            val guesses = listOf(
                GuessItem(lastSongTitle, "", lastSongTitleHit),
                GuessItem(lastSongArtist, "", lastSongArtistHit)
            )

            return InformationPacket(listOf(
                LocalSound(localSoundName),
                GuessFeedback(guessString, guesses),
                Speech(stringService.playerTurn(currentPlayer.name, quiz.getCurrentRoundIndex())),
                SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUri)
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
            Speech(stringService.firstTurnString(quiz.getCurrentPlayer().name, quiz.type.typeNameStringKey, quiz.getCurrentRoundIndex())),
            SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUri)
        ), true)
    }

    private fun endGameInformationBuilder() : List<InformationItem>{
        return listOf(
            Speech(stringService.endResultString(quiz.players, quiz.type.difficultyCompensation)),
            LocalSound(END_SOUND_NAME),
            Speech(stringService.playAgainPossible())
            )
    }

    private fun endGame(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING)
    : InformationPacket {
        state = QuizState.END_REPEAT

        if(isRepeat){
            return InformationPacket(endGameInformationBuilder(), true)
        }
        else{
            val guessString = stringService.guessFeedbackString(lastPlayerArtistTitlePoints, lastSongTitleHit, lastSongArtistHit,
                lastPlayerDifficultyPoints, lastPlayerDifficultyPercentage, lastSongTitle, lastSongArtist, lastSongAlbum,
                extendedInfoAllowed, lastPlayerAllPoints, quiz.type.difficultyCompensation)

            val localSoundName = if(lastPlayerArtistTitlePoints <= 0){
                SAD_SOUND_NAME
            }
            else{
                HAPPY_SOUND_NAME
            }

            val guesses = listOf(
                GuessItem(lastSongTitle, "", lastSongTitleHit),
                GuessItem(lastSongArtist, "", lastSongArtistHit)
            )

            val informationItems = mutableListOf(
                LocalSound(localSoundName),
                GuessFeedback(guessString, guesses),
                Speech(stringService.endGame()))
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
        return welcomeAndAskNumPlayers(true, RepeatCause.RECONFIGURE_GAME)
    }

    private fun exitGame() : InformationPacket {
        setClearQuizState()
        return InformationPacket(listOf(ExitRequest()), false)
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
            KeyNumPlayers.ONE.value to stringService.possibleWords_1(),
            KeyNumPlayers.TWO.value to stringService.possibleWords_2(),
            KeyNumPlayers.THREE.value to stringService.possibleWords_3(),
            KeyNumPlayers.FOUR.value to stringService.possibleWords_4()
        )

        val numPlayers = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(numPlayers.isEmpty()){
            state = QuizState.NUM_PLAYERS_NOT_UNDERSTOOD
        }
        else{
            setQuizPlayers(numPlayers[0].toInt(), listOf())
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
            KeyGameType.ONE_SHOT.value to stringService.possibleWords_oneshot(),
            KeyGameType.SHORT.value to stringService.possibleWords_short(),
            KeyGameType.MEDIUM.value to stringService.possibleWords_medium(),
            KeyGameType.LONG.value to stringService.possibleWords_long(),
        )

        val gameType = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(gameType.isNotEmpty()){
            when(gameType[0]){
                KeyGameType.ONE_SHOT.value -> {
                    setQuizType(OneShotQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed))
                }
                KeyGameType.SHORT.value -> {
                    setQuizType(ShortQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed))
                }
                KeyGameType.MEDIUM.value -> {
                    setQuizType(MediumQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed))
                }
                KeyGameType.LONG.value -> {
                    setQuizType(LongQuiz(songDurationSec, difficultyCompensation, repeatSongAllowed))
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
        possibleHitWords[KeyGuess.REPEAT.value] = stringService.possibleWords_repeat()

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
            KeyAfterFinishedCommand.RESTART.value to stringService.possibleWords_restart(),
            KeyAfterFinishedCommand.CONFIGURE.value to stringService.possibleWords_configure(),
            KeyAfterFinishedCommand.EXIT.value to stringService.possibleWords_exit()
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