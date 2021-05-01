package com.arpadfodor.android.songquiz.model.quiz

class QuizStanding(){
    var numPlayers: Int = 0
    var numRounds: Int = 0
    var currentPlayer: Int = 0
    var currentRound: Int = 0
    var scores: MutableList<Int> = mutableListOf()
    var currentTrackIndex: Int = 0
    var isFinished = false

    fun resetGame(){
        currentPlayer = 0
        currentRound = 0
        scores = mutableListOf()
        for(i in 0 until numPlayers){
            scores.add(0)
        }
        isFinished = false
    }

    fun recordResult(points: Int){
        currentTrackIndex++
        scores[currentPlayer] += points
        currentPlayer++
        if(currentPlayer >= numPlayers){
            currentPlayer = 0
            currentRound++
            if(currentRound >= numRounds){
                isFinished = true
                currentRound--
            }
        }
    }

    fun getCurrentPlayerIndex() : Int{
        return (currentPlayer+1)
    }

    fun getCurrentRoundIndex() : Int{
        return (currentRound+1)
    }

    fun getCurrentPlayerPoints() : Int{
        return scores[currentPlayer]
    }

}