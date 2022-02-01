package com.aaronfodor.android.songquiz.model.quiz

import com.aaronfodor.android.songquiz.R

sealed class QuizType(
    val nameStringKey: Int,
    val songDurationSec: Int,
    val difficultyCompensation: Boolean,
    val repeatAllowed: Boolean
){
    abstract val numRounds: Int
    val pointForArtist: Int = 10
    val pointForTitle: Int = 10
}

class UndefinedQuiz():
    QuizType(R.array.input_undefined,0, false, false) {
    override val numRounds = 0
}

class OneShotQuiz(songDurationSec: Int, difficultyCompensation: Boolean, repeatAllowed: Boolean):
    QuizType(R.array.input_oneshot, songDurationSec, difficultyCompensation, repeatAllowed) {
    override val numRounds = 1
}

class ShortQuiz(songDurationSec: Int, difficultyCompensation: Boolean, repeatAllowed: Boolean):
    QuizType(R.array.input_short, songDurationSec, difficultyCompensation, repeatAllowed) {
    override val numRounds = 3
}

class MediumQuiz(songDurationSec: Int, difficultyCompensation: Boolean, repeatAllowed: Boolean):
    QuizType(R.array.input_medium, songDurationSec, difficultyCompensation, repeatAllowed) {
    override val numRounds = 5
}

class LongQuiz(songDurationSec: Int, difficultyCompensation: Boolean, repeatAllowed: Boolean):
    QuizType(R.array.input_long, songDurationSec, difficultyCompensation, repeatAllowed) {
    override val numRounds = 7
}