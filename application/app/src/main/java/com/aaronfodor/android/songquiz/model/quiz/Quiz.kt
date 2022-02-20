package com.aaronfodor.android.songquiz.model.quiz

class Quiz {

    var type: QuizType = UndefinedQuiz()
    // gameplay specific, should be reset to start new game
    var currentRound: Int = 0
    var currentPlayerIdx: Int = -1
    var currentTrackIndex: Int = 0
    var players: List<QuizPlayer> = listOf()
    var isFinished = false

    fun getNumPlayersLocal() : Int{
        var numLocalPlayers = 0
        players.forEach { if(it is QuizPlayerLocal) numLocalPlayers += 1 }
        return numLocalPlayers
    }

    fun setQuizPlayers(players: List<QuizPlayer>){
        this.players = players
    }

    fun setQuizType(type: QuizType){
        this.type = type
    }

    fun startState(){
        currentRound = 1
        currentPlayerIdx = 0
        currentTrackIndex = 0
        isFinished = false
        players.forEach { it.resetPoints() }
    }

    fun recordResult(artistPoint: Int, titlePoint: Int, difficultyCompensationPoint: Int){
        players[currentPlayerIdx].recordGuess(artistPoint, titlePoint, difficultyCompensationPoint)
        currentTrackIndex++
    }

    fun toNextTurn(){
        currentPlayerIdx++
        if(currentPlayerIdx >= players.size){
            currentPlayerIdx = 0
            currentRound++
            if(currentRound > type.numRounds){
                isFinished = true
                currentRound = type.numRounds
            }
        }
    }

    fun getCurrentPlayer() : QuizPlayer{
        return players[currentPlayerIdx]
    }

    fun getCurrentRoundIndex() : Int{
        return currentRound
    }

}