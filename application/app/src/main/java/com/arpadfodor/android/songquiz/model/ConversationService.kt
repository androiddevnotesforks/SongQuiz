package com.arpadfodor.android.songquiz.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * Injected to bind lifecycle to a ViewModel scope
 */
@ViewModelScoped
class ConversationService @Inject constructor(
    @ApplicationContext val context: Context
){

    var state = 0
    var lastSaidByUser = ""

    /**
     * Initialize user info
     */
    fun reset(){
        state = 0
    }

    /**
     * Returns the current info to the user
     *
     * @return a Pair<String, Boolean> where first: text to the user, second: whether immediate answer is required
     */
    fun getCurrentInfo() : Pair<String, Boolean> {

        val response = when(state){
            0 -> Pair("Welcome to song quiz! How many player wants to play?", true)
            1 -> Pair(lastSaidByUser, false)
            else -> Pair("Looks like an error occurred.", false)
        }

        // reset the state now for testing
        state = 0

        return response
    }

    /**
     * Update state based on user input
     *
     * @param probableSpeeches      ArrayList<String> of probable user inputs
     */
    fun userInput(probableSpeeches : ArrayList<String>) : Boolean{
        // process what the user said
        // now for testing
        state = 1
        lastSaidByUser = "Looks like you said: ${probableSpeeches[0]}"
        return true
    }

}