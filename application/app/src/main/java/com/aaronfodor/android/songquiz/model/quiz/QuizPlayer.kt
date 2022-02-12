package com.aaronfodor.android.songquiz.model.quiz

class QuizPlayer(
    val id: Int = 0,
    val name: String = "",
){

    private var numGuesses: Int = 0
    private var numArtistHits: Int = 0
    private var numTitleHits: Int = 0

    private var artistPoints: Int = 0
    private var titlePoints: Int = 0
    private var difficultyCompensationPoints: Int = 0

    fun recordGuess(artistPoint: Int, titlePoint: Int, difficultyCompensationPoint: Int){
        numGuesses += 1
        if(artistPoint > 0){
            numArtistHits += 1
            artistPoints += artistPoint
        }
        if(titlePoint > 0){
            numTitleHits += 1
            titlePoints += titlePoint
        }
        if(difficultyCompensationPoint >= 0){
            difficultyCompensationPoints += difficultyCompensationPoint
        }
    }

    fun getPoints(difficultyCompensationNeeded: Boolean): Int{
        var points = artistPoints + titlePoints
        if(difficultyCompensationNeeded){
            points += difficultyCompensationPoints
        }
        return points
    }

    fun resetPoints(){
        numGuesses = 0
        numArtistHits = 0
        numTitleHits = 0

        artistPoints = 0
        titlePoints = 0
        difficultyCompensationPoints = 0
    }

}