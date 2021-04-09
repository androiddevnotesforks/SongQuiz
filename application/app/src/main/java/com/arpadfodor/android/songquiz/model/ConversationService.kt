package com.arpadfodor.android.songquiz.model

object ConversationService {

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
     */
    fun getCurrentInfo() : String{
        val text = when(state){
            0 -> "Welcome to song quiz! How many player wants to play?"
            1 -> lastSaidByUser
            else -> "Looks like an error occurred."
        }
        // reset the state now for testing
        state = 0

        return text
    }

    /**
     * Update state based on user input
     */
    fun userInput(probableSpeeches : ArrayList<String>){
        // process what the user said
        // now for testing
        state = 1
        lastSaidByUser = "Looks like you said: ${probableSpeeches[0]}"
    }

}