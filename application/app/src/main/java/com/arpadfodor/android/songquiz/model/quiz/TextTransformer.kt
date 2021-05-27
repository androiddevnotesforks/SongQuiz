package com.arpadfodor.android.songquiz.model.quiz

import java.text.Normalizer
import java.util.*

class TextTransformer {

    companion object{
        val CHARS_TO_SEPARATE_BY = listOf(" ", "-", ",", ";", "?", "!", ".", "(", ")", "/", "_", "+", "=", "&", "@", ":").toTypedArray()
    }

    private val UNACCENT_REGEX = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private fun String.unaccent(): String {
        val tempText = Normalizer.normalize(this, Normalizer.Form.NFD)
        return UNACCENT_REGEX.replace(tempText, "")
    }

    /**
     * Split the input among CHARS_TO_SEPARATE_BY, then lowercase the text, then replaces problematic characters
     *
     * @param input         String to normalize
     * @return List<String> normalized text blocks
     */
    fun normalizeText(input: String) : List<String>{
        val normalizedText = input.toLowerCase(Locale.ROOT).unaccent()
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

}