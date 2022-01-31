package com.aaronfodor.android.songquiz.model.quiz

data class QuizType(
    val name: String = "",
    val numRounds: Int = 0,
    val pointForArtist: Int = 0,
    val pointForTitle: Int = 0,
    val pointForAlbum: Int = 0,
    val repeatAllowed: Boolean = false,
    val songDurationSec: Int = 0
)