package com.aaronfodor.android.songquiz

import com.aaronfodor.android.songquiz.model.quiz.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QuizTest{

    private lateinit var quiz: Quiz

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun quizFinishedTest() {
        // Given
        val numPlayers = 4
        val quizType = ShortQuiz(0, false, false)
        quiz = Quiz()
        quiz.type = quizType

        val players = mutableListOf<QuizPlayer>()
        for(i in 0 until numPlayers){
            players.add(QuizPlayerLocal(i, i.toString()))
        }
        quiz.setQuizPlayers(players)

        quiz.startState()
        // When
        for(i in 0 until (numPlayers*quiz.type.numRounds)){
            quiz.recordResult(0, 0, 0)
        }
        // Then
        assertThat(quiz.isFinished, `is`(true))
        assertThat(quiz.getCurrentPlayer().id, `is`(1))
        assertThat(quiz.getCurrentRoundIndex(), `is`(quiz.type.numRounds))
    }

    @Test
    fun quizStandingNotFinishedTest() {
        // Given
        val numPlayers = 2
        val quizType = ShortQuiz(0, false, false)
        quiz = Quiz()

        val players = mutableListOf<QuizPlayer>()
        for(i in 0 until numPlayers){
            players.add(QuizPlayerLocal(i, i.toString()))
        }
        quiz.setQuizPlayers(players)

        quiz.type = quizType
        quiz.startState()
        // When
        for(i in 0 until (numPlayers*(quiz.type.numRounds-1))){
            quiz.recordResult(0, 0, 0)
        }
        // Then
        assertThat(quiz.isFinished, `is`(false))
        assertThat(quiz.getCurrentPlayer().id, `is`(1))
        assertThat(quiz.getCurrentRoundIndex(), `is`(quiz.type.numRounds))
    }

    @Test
    fun quizStandingCountsWellTest() {
        // Given
        val numPlayers = 4
        val quizType = MediumQuiz(0, false, false)
        quiz = Quiz()

        val players = mutableListOf<QuizPlayer>()
        for(i in 0 until numPlayers){
            players.add(QuizPlayerLocal(i, i.toString()))
        }
        quiz.setQuizPlayers(players)

        quiz.type = quizType
        quiz.startState()
        // When
        for(i in 0 until (numPlayers*quiz.type.numRounds)){
            if(quiz.currentPlayerIdx % 2 == 0){
                quiz.recordResult(10, 10, 0)
                quiz.toNextTurn()
            }
            else{
                quiz.recordResult(0, 0, 0)
                quiz.toNextTurn()
            }
        }
        // Then
        assertThat(quiz.players[0].getPoints(quiz.type.difficultyCompensation), `is`(quiz.type.numRounds*20))
        assertThat(quiz.players[1].getPoints(quiz.type.difficultyCompensation), `is`(0))
    }

}