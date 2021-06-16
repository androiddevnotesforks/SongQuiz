package com.aaronfodor.android.songquiz.model

import java.text.Normalizer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class TextParserService @Inject constructor() {

    companion object{
        val CHARS_TO_SEPARATE_BY = listOf(" ", "-", ",", ";", "?", "!", ".", "(", ")", "/", "_", "+", "=", "&", "@", ":").toTypedArray()
    }

    private val removeAccentRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private fun String.removeAccents(): String {
        val tempText = Normalizer.normalize(this, Normalizer.Form.NFD)
        return removeAccentRegex.replace(tempText, "")
    }

    /**
     * Split the input among CHARS_TO_SEPARATE_BY, then lowercase the text, then replaces problematic characters
     *
     * @param input         String to normalize
     * @return List<String> normalized text blocks
     */
    fun normalizeText(input: String) : List<String>{
        val normalizedText = input.lowercase(Locale.ROOT).removeAccents()
        return normalizedText.split(*CHARS_TO_SEPARATE_BY)
    }

    /**
     * Split the input among CHARS_TO_SEPARATE_BY, then lowercase the text, then replaces problematic characters
     *
     * @param inputList     List<String> of texts
     * @return String representation of the whole list in a nice format
     */
    fun stringListToString(inputList: List<String>) : String{
        return inputList.toString().replace("[", "").replace("]", "")
    }

    /**
     * Search for word occurrences
     *
     * @param probableSpeeches      ArrayList<String> of probable user inputs
     * @param searchedWords         Map<String, List<String>> of the searched word and the accepted variants
     * @param onlyOneNeeded         Whether only one match needed
     * @return List<String> containing the found words from the searched ones
     */
    fun searchForWordOccurrences(probableSpeeches : ArrayList<String>,
                                 searchedWords: Map<String, List<String>>,
                                 onlyOneNeeded: Boolean) : List<String>{
        val wordsFound = mutableListOf<String>()
        for(speech in probableSpeeches) {
            val speechWords = normalizeText(speech)
            for(speechWord in speechWords){
                for(searchedWord in searchedWords){
                    for(acceptedWordForm in searchedWord.value){
                        if(acceptedWordForm == speechWord){
                            wordsFound.add(searchedWord.key)
                            if(onlyOneNeeded){
                                return wordsFound
                            }
                        }
                    }
                }
            }
        }
        return wordsFound.distinct()
    }

}