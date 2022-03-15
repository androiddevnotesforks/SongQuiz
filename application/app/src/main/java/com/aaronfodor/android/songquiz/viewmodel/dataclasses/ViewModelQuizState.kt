package com.aaronfodor.android.songquiz.viewmodel.dataclasses

import com.aaronfodor.android.songquiz.model.quiz.Quiz
import com.aaronfodor.android.songquiz.model.quiz.QuizPlayer

class ViewModelQuizPlayer (
    var id: Int,
    val name: String = "",
    val points: Int = 0
)

fun QuizPlayer.toViewModelQuizPlayer(difficultyCompensationNeeded: Boolean) : ViewModelQuizPlayer {
    return ViewModelQuizPlayer(
        id = this.id,
        name = this.name,
        points = this.getPoints(difficultyCompensationNeeded),
    )
}

class ViewModelQuizState (
    val currentRound: Int,
    val numRounds: Int,
    val currentPlayerIdx: Int,
    val isFinished: Boolean,
    val players: List<ViewModelQuizPlayer>
)

fun Quiz.toViewModelQuizState() : ViewModelQuizState{
    val convertedPlayers = mutableListOf<ViewModelQuizPlayer>()
    for(player in this.players){
        convertedPlayers.add(player.toViewModelQuizPlayer(this.type.difficultyCompensation))
    }

    return ViewModelQuizState(
        currentRound = this.currentRound,
        numRounds = this.type.numRounds,
        currentPlayerIdx = this.currentPlayerIdx,
        isFinished = this.isFinished,
        players = convertedPlayers
    )
}