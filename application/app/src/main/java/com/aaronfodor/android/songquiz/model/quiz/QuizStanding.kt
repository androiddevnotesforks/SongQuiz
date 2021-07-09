package com.aaronfodor.android.songquiz.model.quiz

class QuizStanding {
    var numPlayers: Int = 0
    var numRounds: Int = 0
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
            players.add(QuizPlayer(i, "", 0))
        }
        isFinished = false
    }

    fun recordResult(points: Int){
        currentTrackIndex++
        players[currentPlayerIdx].points += points
        currentPlayerIdx++
        if(currentPlayerIdx >= numPlayers){
            currentPlayerIdx = 0
            currentRound++
            if(currentRound > numRounds){
                isFinished = true
                currentRound = numRounds
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