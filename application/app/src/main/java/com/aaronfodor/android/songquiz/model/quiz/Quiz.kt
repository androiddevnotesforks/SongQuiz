package com.aaronfodor.android.songquiz.model.quiz

class Quiz {

    var type: QuizType = UndefinedQuiz()
    var numPlayers: Int = 0
    // gameplay specific, should be reset to start new game
    var currentRound: Int = 0
    var currentPlayerIdx: Int = 0
    var currentTrackIndex: Int = 0
    var players: MutableList<QuizPlayer> = mutableListOf()
    var isFinished = false

    fun setPlayers(numPlayers: Int, names: List<String>){
        this.numPlayers = numPlayers

        players = mutableListOf()
        var id = 1
        for(i in 0 until this.numPlayers){
            val name = if(i < names.size){
                names[i]
            }
            else{
                ""
            }
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