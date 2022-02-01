package com.aaronfodor.android.songquiz.model.quiz

class Quiz {

    var type: QuizType = UndefinedQuiz()
    var numPlayers: Int = 0
    // gameplay specific, should be reset to start new game
    var currentRound: Int = 1
    var currentPlayerIdx: Int = 0
    var currentTrackIndex: Int = 0
    var players: MutableList<QuizPlayer> = mutableListOf()
    var isFinished = false

    fun clearState(){
        currentRound = 1
        currentPlayerIdx = 0
        currentTrackIndex = 0
        players = mutableListOf()
        for(i in 1..numPlayers){
            players.add(QuizPlayer(i, ""))
        }
        isFinished = false
    }

    fun recordResult(artistPoint: Int, titlePoint: Int, difficultyCompensationPoint: Int){
        currentTrackIndex++
        players[currentPlayerIdx].recordGuess(artistPoint, titlePoint, difficultyCompensationPoint)
        currentPlayerIdx++
        if(currentPlayerIdx >= numPlayers){
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