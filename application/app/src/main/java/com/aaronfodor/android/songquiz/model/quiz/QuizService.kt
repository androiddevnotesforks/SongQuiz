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
    START_GAME_GENERATED_PLAYER_INFO,
    START_GAME_FIRST_TURN,
    PLAY_SONG,
    GUESS_FEEDBACK,
    PARSE_GUESS,
    NEXT_TURN,
    REPEAT_SONG,
    END,
    REPEAT_END,
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
        const val DUEL_SOUND_NAME = "duel.wav"
    }

    var state = QuizState.WELCOME
    var lastSaidByUser = ""
    var playlist = Playlist("")
    var quiz = Quiz()
    private val stringHandler = stringService
    private val textParser = textParserService

    var songDurationSec = 0
    var difficultyCompensation = true
    var repeatSongAllowed = true
    var extendedInfoAllowed = false

    var doesGeneratedPlayerPlay = false
    var generatedPlayerName = ""
    var generatedPlayerLastPoints = 0

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

    // generated players are placeholders only
    private fun setQuizPlayers(numPlayers: Int){
        doesGeneratedPlayerPlay = false
        generatedPlayerName = stringHandler.unknown()
        generatedPlayerLastPoints = 0

        val players = mutableListOf<QuizPlayer>()
        // if only one player is added, add a generated player
        if(numPlayers == 1){
            doesGeneratedPlayerPlay = true
            generatedPlayerLastPoints = 0

            players.add(QuizPlayerLocal(1, stringHandler.you()))
            players.add(QuizPlayerGenerated(2, generatedPlayerName))
        }
        // if multiple local players are needed, just add them
        else{
            for(i in 1 .. numPlayers){
                players.add(QuizPlayerLocal(i, stringHandler.player(i)))
            }
        }
        quiz.setQuizPlayers(players)
    }

    private fun setQuizType(type: QuizType){
        quiz.setQuizType(type)
    }

    // must be called to actually setup generated players
    private fun initGeneratedPlayers(){
        val players = mutableListOf<QuizPlayer>()
        quiz.players.forEach {
            if(it is QuizPlayerGenerated){
                generatedPlayerName = stringHandler.generatePlayerName()
                players.add(QuizPlayerGenerated(it.id, generatedPlayerName))
            }
            else{
                players.add(it)
            }
        }
        quiz.players = players
    }

    private fun setupStartQuizState(){
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
            QuizState.START_GAME -> gameSetupThanNotify()
            QuizState.START_GAME_GENERATED_PLAYER_INFO -> generatedPlayerInfoThanNotify()
            QuizState.START_GAME_FIRST_TURN -> firstTurnThanNotify()
            QuizState.PLAY_SONG -> playSong()
            QuizState.GUESS_FEEDBACK -> guessFeedbackThanNotify()
            QuizState.PARSE_GUESS -> repeatSong()
            QuizState.NEXT_TURN -> nextTurnThanNotify()
            QuizState.REPEAT_SONG -> repeatSong()
            QuizState.END -> endGame()
            QuizState.REPEAT_END -> endGame(true)
            QuizState.RESTART_GAME -> restartGame()
            QuizState.CONFIGURE_GAME -> configureGame()
            QuizState.EXIT_GAME -> exitGame()
            else -> InformationPacket(listOf(Speech(stringHandler.error())), false)
        }
        return response
    }

    private fun welcomeAndAskNumPlayers(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING) : InformationPacket {
        if(playlist.tracks.size < MIN_NUM_TRACKS){
            return InformationPacket(listOf(
                Speech(stringHandler.welcome()),
                Speech(stringHandler.playlistInfo(playlist.name, playlist.tracks.size)),
                Speech(stringHandler.notEnoughTracks(MIN_NUM_TRACKS))
            ), false)
        }

        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = stringHandler.notUnderstand()
                }
                RepeatCause.RECONFIGURE_GAME -> {
                    reasonText = stringHandler.reconfigure(playlist.name, playlist.tracks.size)
                }
                else -> {}
            }

            return InformationPacket(listOf(
                Speech(reasonText),
                Speech(stringHandler.askNumPlayers())
            ), true)
        }
        else{
            return InformationPacket(listOf(
                Speech(stringHandler.welcome()),
                Speech(stringHandler.playlistInfo(playlist.name, playlist.tracks.size)),
                Speech(stringHandler.askNumPlayers())
            ), true)
        }
    }

    private fun askGameType(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING) : InformationPacket {
        if(isRepeat){
            var reasonText = ""

            when(cause){
                RepeatCause.INVALID_INPUT -> {
                    reasonText = stringHandler.askGameTypeInvalid(quiz.type.typeNameStringKey, playlist.tracks.size, quiz.getNumPlayersLocal(), quiz.type.numRounds)
                }
                RepeatCause.NOT_UNDERSTOOD -> {
                    reasonText = stringHandler.notUnderstand()
                }
                else -> {}
            }

            return InformationPacket(listOf(
                Speech(reasonText),
                Speech(stringHandler.askGameType()),
            ), true)
        }
        else{
            val info = mutableListOf<InformationItem>()
            if(doesGeneratedPlayerPlay){
                info.add(Speech(stringHandler.duelSelected(quiz.getNumPlayersLocal())))
            }
            else{
                info.add(Speech(stringHandler.numPlayersSelected(quiz.getNumPlayersLocal())))
            }
            info.add(Speech(stringHandler.askGameType()))
            return InformationPacket(info, true)
        }
    }

    private fun gameSetupInfo() : List<InformationItem> {
        val info = mutableListOf<InformationItem>()
        val selectedGameAndSettingsText = stringHandler.selectedGameAndSettingsString(extendedInfoAllowed, quiz.type.repeatAllowed, quiz.type.difficultyCompensation,
            quiz.type.pointForTitle, quiz.type.pointForArtist, quiz.type.pointForDifficulty, quiz.type.songDurationSec, quiz.type.numRounds, quiz.type.typeNameStringKey)
        info.add(Speech(selectedGameAndSettingsText))
        return info
    }

    private fun gameSetupThanNotify() : InformationPacket {
        state = QuizState.START_GAME_GENERATED_PLAYER_INFO

        val info = mutableListOf<InformationItem>()
        info.addAll(gameSetupInfo())
        info.add(NotifyGetNextInfo())
        return InformationPacket(info, false)
    }

    private fun generatedPlayerInfoThanNotify() : InformationPacket {
        state = QuizState.START_GAME_FIRST_TURN

        val info = mutableListOf<InformationItem>()
        if(doesGeneratedPlayerPlay){
            initGeneratedPlayers()
            val generatedPlayerText = stringHandler.generatedPlayerInfo(generatedPlayerName)
            info.add(Speech(generatedPlayerText))
            info.add(LocalSound(DUEL_SOUND_NAME))
        }
        info.add(NotifyGetNextInfo())
        return InformationPacket(info, false)
    }

    private fun firstTurnThanNotify() : InformationPacket {
        setupStartQuizState()
        state = QuizState.PLAY_SONG

        val info = mutableListOf<InformationItem>()
        info.add(Speech(stringHandler.firstTurnString(quiz.type.typeNameStringKey)))
        info.add(NotifyGetNextInfo())
        return InformationPacket(info, true)
    }

    private fun guessFeedbackThanNotify() : InformationPacket {
        state = QuizState.NEXT_TURN

        val info = mutableListOf<InformationItem>()
        info.addAll(guessInformationBuilder())
        info.add(NotifyGetNextInfo())
        return InformationPacket(info, false)
    }

    private fun nextTurnThanNotify() : InformationPacket {
        if(!doesGeneratedPlayerPlay){
            quiz.toNextTurn()
        }

        state = if(quiz.isFinished){
            QuizState.END
        }
        else{
            QuizState.PLAY_SONG
        }

        val info = mutableListOf<InformationItem>()
        info.add(NotifyGetNextInfo())
        return InformationPacket(info, false)
    }

    private fun playSong() : InformationPacket {
        val currentPlayer = quiz.getCurrentPlayer()
        state = QuizState.PARSE_GUESS

        val info = mutableListOf<InformationItem>()
        if(doesGeneratedPlayerPlay){
            info.add(Speech(stringHandler.yourTurn(quiz.getCurrentRoundIndex())))
        }
        else{
            info.add(Speech(stringHandler.playerTurn(currentPlayer.name, quiz.getCurrentRoundIndex())))
        }
        info.add(SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUrl))
        return InformationPacket(info, true)
    }

    private fun repeatSong() : InformationPacket {
        return InformationPacket(listOf(
            Speech(stringHandler.repeatSong()),
            SoundURL(playlist.tracks[quiz.currentTrackIndex].previewUrl)
        ), true)
    }

    private fun guessInformationBuilder() : List<InformationItem>{
        val info = mutableListOf<InformationItem>()

        val localSoundName = if(lastPlayerArtistTitlePoints <= 0){
            SAD_SOUND_NAME
        }
        else{
            HAPPY_SOUND_NAME
        }
        info.add(LocalSound(localSoundName))

        val guessString = stringHandler.guessFeedbackString(lastPlayerArtistTitlePoints, lastSongTitleHit, lastSongArtistHit,
            lastPlayerDifficultyPoints, lastPlayerDifficultyPercentage, lastSongTitle, lastSongArtist, lastSongAlbum,
            extendedInfoAllowed, lastPlayerAllPoints, quiz.type.difficultyCompensation)
        val guesses = listOf(
            GuessItem(lastSongTitle, "", lastSongTitleHit),
            GuessItem(lastSongArtist, "", lastSongArtistHit)
        )
        info.add(GuessFeedback(guessString, guesses))

        if(doesGeneratedPlayerPlay){
            val lastPlayerPoints = lastPlayerArtistTitlePoints + if(quiz.type.difficultyCompensation){ lastPlayerDifficultyPoints } else{ 0 }

            if(generatedPlayerLastPoints == lastPlayerPoints){
                info.add(Speech(stringHandler.generatedPlayerSameGuessInfo(generatedPlayerName, generatedPlayerLastPoints)))
            }
            else{
                info.add(Speech(stringHandler.generatedPlayerGuessInfo(generatedPlayerName, generatedPlayerLastPoints)))
            }
        }

        return info
    }

    private fun endGameInformationBuilder() : List<InformationItem>{
        var resultText = ""
        var winnerName = ""
        var winnerPoints = 0
        var numWinners = 0
        for(player in quiz.players){
            val currentPlayerPoints = player.getPoints(quiz.type.difficultyCompensation)
            resultText += stringHandler.playerScored(player.name, currentPlayerPoints) + " "

            if(currentPlayerPoints > winnerPoints){
                winnerPoints = currentPlayerPoints
                winnerName = player.name
                numWinners = 1
            }
            else if(currentPlayerPoints == winnerPoints){
                winnerName = ""
                numWinners += 1
            }

        }

        resultText += when {
            winnerPoints <= 0 -> {
                stringHandler.nextTime()
            }
            winnerName.isNotBlank() -> {

                if(doesGeneratedPlayerPlay){
                    if(winnerName == generatedPlayerName){
                        stringHandler.winnerGeneratedPlayer(winnerName)
                    }
                    else{
                        stringHandler.winnerIsYou()
                    }
                }
                else{
                    stringHandler.winnerPlayer(winnerName)
                }

            }
            else -> {
                stringHandler.winnerTie()
            }
        }

        var winnerNames = ""
        var numSemicolons = 0
        // if actually there is a winner, find out their names
        if(winnerPoints > 0){
            for(player in quiz.players){
                if(player.getPoints(quiz.type.difficultyCompensation) == winnerPoints){
                    winnerNames += player.name
                    if(numSemicolons+1 < numWinners){
                        winnerNames += ", "
                        numSemicolons += 1
                    }
                }
            }
        }

        return listOf(
            EndFeedback(resultText, winnerNames, numWinners),
            LocalSound(END_SOUND_NAME),
            Speech(stringHandler.playAgainPossible())
        )
    }

    private fun endGame(isRepeat: Boolean = false, cause: RepeatCause = RepeatCause.NOTHING) : InformationPacket {
        state = QuizState.REPEAT_END

        return if(isRepeat){
            InformationPacket(endGameInformationBuilder(), true)
        }
        else{
            val info = mutableListOf<InformationItem>()
            info.add(Speech(stringHandler.endGame()))
            info.addAll(endGameInformationBuilder())
            InformationPacket(info, true)
        }
    }

    private fun restartGame() : InformationPacket {
        return generatedPlayerInfoThanNotify()
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
            QuizState.PARSE_GUESS -> parseGuess(probableSpeeches)
            QuizState.REPEAT_SONG -> parseGuess(probableSpeeches)
            QuizState.REPEAT_END -> parseAfterFinishedCommand(probableSpeeches)
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
            KeyNumPlayers.ONE.value to stringHandler.possibleWords_1(),
            KeyNumPlayers.TWO.value to stringHandler.possibleWords_2(),
            KeyNumPlayers.THREE.value to stringHandler.possibleWords_3(),
            KeyNumPlayers.FOUR.value to stringHandler.possibleWords_4()
        )

        val numPlayers = textParser.searchForWordOccurrences(probableSpeeches, possibleWords, true)

        if(numPlayers.isEmpty()){
            state = QuizState.NUM_PLAYERS_NOT_UNDERSTOOD
        }
        else{
            setQuizPlayers(numPlayers[0].toInt())
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
            KeyGameType.ONE_SHOT.value to stringHandler.possibleWords_oneshot(),
            KeyGameType.SHORT.value to stringHandler.possibleWords_short(),
            KeyGameType.MEDIUM.value to stringHandler.possibleWords_medium(),
            KeyGameType.LONG.value to stringHandler.possibleWords_long(),
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

        state = if(quiz.type.numRounds * quiz.getNumPlayersLocal() > playlist.tracks.size){
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

    private fun calculateCompensationRatio(trackPopularity: Int) : Double{
        return (1.0 - (trackPopularity.toDouble() / 100.0))
    }

    private fun calculateDifficultyCompensation(trackPopularity: Int, pointForDifficulty: Int) : Int{
        val compensationRatio = calculateCompensationRatio(trackPopularity)
        val difficultyCompensationPoint = ((pointForDifficulty) * compensationRatio).roundToInt()
        return difficultyCompensationPoint
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
        possibleHitWords[KeyGuess.REPEAT.value] = stringHandler.possibleWords_repeat()

        val playerHits = textParser.searchForWordOccurrences(probableSpeeches, possibleHitWords, false)

        // by default, after the parsed guess, proceed to next turn
        state = QuizState.GUESS_FEEDBACK
        // if repeat allowed
        if(quiz.type.repeatAllowed){
            // if only the extra command has been identified, execute it
            if(playerHits.size == 1 && playerHits.contains(KeyGuess.REPEAT.value)){
                state = QuizState.REPEAT_SONG
                return true
            }
        }

        var titlePoint = 0
        var artistPoint = 0
        var difficultyCompensationPoint = 0

        if(playerHits.contains(KeyGuess.TITLE.value)){
            titlePoint = quiz.type.pointForTitle
            lastSongTitleHit = true
        }
        else{
            lastSongTitleHit = false
        }

        if(playerHits.contains(KeyGuess.ARTIST.value)){
            artistPoint = quiz.type.pointForArtist
            lastSongArtistHit = true
        }
        else{
            lastSongArtistHit = false
        }

        difficultyCompensationPoint = calculateDifficultyCompensation(currentTrack.popularity, quiz.type.pointForDifficulty)

        lastSongTitle = currentTrack.name
        lastSongArtist = textParser.stringListToString(currentTrack.artists)
        lastSongAlbum = currentTrack.album
        lastSongPopularity = currentTrack.popularity

        lastPlayerArtistTitlePoints = artistPoint + titlePoint
        if(quiz.type.difficultyCompensation){
            lastPlayerDifficultyPoints = difficultyCompensationPoint
            lastPlayerDifficultyPercentage = (calculateCompensationRatio(currentTrack.popularity) * 100.0).roundToInt()
        }

        val currentPlayerPoints = quiz.getCurrentPlayer().getPoints(quiz.type.difficultyCompensation)
        lastPlayerAllPoints = currentPlayerPoints
        lastPlayerAllPoints += lastPlayerArtistTitlePoints + lastPlayerDifficultyPoints

        quiz.recordResult(artistPoint, titlePoint, difficultyCompensationPoint)
        if(doesGeneratedPlayerPlay){
            quiz.toNextTurn()
        }

        // generated player guessing
        while(doesGeneratedPlayerPlay && (quiz.getCurrentPlayer() is QuizPlayerGenerated)){

            val currentGeneratedPlayer = quiz.getCurrentPlayer() as QuizPlayerGenerated
            generatedPlayerName = currentGeneratedPlayer.name
            generatedPlayerLastPoints = 0

            val generatedPlayerHits = currentGeneratedPlayer.calculateGuess(currentTrack)

            var generatedTitlePoint = 0
            var generatedArtistPoint = 0

            if(generatedPlayerHits.contains(KeyGuess.TITLE.value)){
                generatedTitlePoint = quiz.type.pointForTitle
            }
            if(generatedPlayerHits.contains(KeyGuess.ARTIST.value)){
                generatedArtistPoint = quiz.type.pointForArtist
            }

            quiz.recordResult(generatedArtistPoint, generatedTitlePoint, difficultyCompensationPoint)
            quiz.toNextTurn()

            generatedPlayerLastPoints = generatedTitlePoint + generatedArtistPoint
            if(quiz.type.difficultyCompensation){
                generatedPlayerLastPoints += difficultyCompensationPoint
            }

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
            KeyAfterFinishedCommand.RESTART.value to stringHandler.possibleWords_restart(),
            KeyAfterFinishedCommand.CONFIGURE.value to stringHandler.possibleWords_configure(),
            KeyAfterFinishedCommand.EXIT.value to stringHandler.possibleWords_exit()
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