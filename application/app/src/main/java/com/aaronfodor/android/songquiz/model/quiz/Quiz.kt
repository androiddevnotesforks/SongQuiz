package com.aaronfodor.android.songquiz.model.quiz

class Quiz {

    var type: QuizType = UndefinedQuiz()
    // gameplay specific, should be reset to start new game
    var currentRound: Int = 0
    var currentPlayerIdx: Int = 0
    var currentTrackIndex: Int = 0
    var players: MutableList<QuizPlayer> = mutableListOf()
    var isFinished = false

    fun setQuizPlayers(names: List<String>){
        players = mutableListOf()
        var id = 1
        for(name in names){
            players.add(QuizPlayer(id, name))
            id++
        }
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
        currentTrackIndex++
        players[currentPlayerIdx].recordGuess(artistPoint, titlePoint, difficultyCompensationPoint)
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