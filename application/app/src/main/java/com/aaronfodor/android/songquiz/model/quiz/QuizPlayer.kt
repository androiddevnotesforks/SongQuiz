package com.aaronfodor.android.songquiz.model.quiz

import com.aaronfodor.android.songquiz.model.repository.dataclasses.Track
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

sealed class QuizPlayer(
    val id: Int,
    val name: String,
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

class QuizPlayerLocal(
    id: Int,
    name: String
) : QuizPlayer(id, name)

class QuizPlayerGenerated(
    id: Int,
    name: String,
    val avgDifficulty: Int = 33,
    val avgTitleHitRatio: Double = 0.5,
    val avgArtistHitRatio: Double = 0.5,
) : QuizPlayer(id, name){

    private val minHitProbability = 0.1
    private val maxHitProbability = 0.9

    fun calculateGuess(track: Track) : List<String> {
        val playerHits = mutableListOf<String>()
        val diffWeight = avgDifficulty.toDouble() / (100 - track.popularity).toDouble()

        var titleHitProbability = avgTitleHitRatio * diffWeight
        // give a slight change on very unfamiliar tracks
        titleHitProbability = max(titleHitProbability, minHitProbability)
        // do not assure that a very popular song is always a hit
        titleHitProbability = min(titleHitProbability, maxHitProbability)

        var artistHitProbability = avgArtistHitRatio * diffWeight
        // give a slight change on very unfamiliar tracks
        artistHitProbability = max(artistHitProbability, minHitProbability)
        // do not assure that a very popular song is always a hit
        artistHitProbability = min(artistHitProbability, maxHitProbability)

        // title hit
        if(Random.nextFloat() <= titleHitProbability){
            playerHits.add(QuizService.KeyGuess.TITLE.value)
        }
        // artist hit
        if(Random.nextFloat() <= artistHitProbability){
            playerHits.add(QuizService.KeyGuess.ARTIST.value)
        }

        return playerHits
    }

}