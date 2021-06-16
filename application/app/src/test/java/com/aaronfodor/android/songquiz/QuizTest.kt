package com.aaronfodor.android.songquiz

import com.aaronfodor.android.songquiz.model.quiz.QuizStanding
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QuizStandingTest{

    private lateinit var quizStanding: QuizStanding

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun quizStandingFinishedTest() {
        // Given
        val numPlayers = 4
        val numRounds = 3
        quizStanding = QuizStanding()
        quizStanding.numPlayers = numPlayers
        quizStanding.numRounds = numRounds
        quizStanding.clearState()
        // When
        for(i in 0 until (numPlayers*numRounds)){
            quizStanding.recordResult(0)
        }
        // Then
        assertThat(quizStanding.isFinished, `is`(true))
        assertThat(quizStanding.getCurrentPlayerIndex(), `is`(1))
        assertThat(quizStanding.getCurrentRoundIndex(), `is`(numRounds))
    }

    @Test
    fun quizStandingNotFinishedTest() {
        // Given
        val numPlayers = 2
        val numRounds = 3
        quizStanding = QuizStanding()
        quizStanding.numPlayers = numPlayers
        quizStanding.numRounds = numRounds
        quizStanding.clearState()
        // When
        for(i in 0 until (numPlayers*(numRounds-1))){
            quizStanding.recordResult(0)
        }
        // Then
        assertThat(quizStanding.isFinished, `is`(false))
        assertThat(quizStanding.getCurrentPlayerIndex(), `is`(1))
        assertThat(quizStanding.getCurrentRoundIndex(), `is`(numRounds))
    }

    @Test
    fun quizStandingCountsWellTest() {
        // Given
        val numPlayers = 4
        val numRounds = 5
        quizStanding = QuizStanding()
        quizStanding.numPlayers = numPlayers
        quizStanding.numRounds = numRounds
        quizStanding.clearState()
        // When
        for(i in 0 until (numPlayers*numRounds)){
            if(quizStanding.currentPlayer % 2 == 0){
                quizStanding.recordResult(20)
            }
            else{
                quizStanding.recordResult(0)
            }
        }
        // Then
        assertThat(quizStanding.scores[0], `is`(numRounds*20))
        assertThat(quizStanding.scores[1], `is`(0))
    }

}