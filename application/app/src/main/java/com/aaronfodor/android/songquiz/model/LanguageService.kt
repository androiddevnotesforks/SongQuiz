package com.aaronfodor.android.songquiz.model

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class LanguageService @Inject constructor(){

    fun getLanguageLocale() : Locale {
        val currentLocale = Locale.getDefault()
        val locale = when (currentLocale.language) {
            "en" -> {
                currentLocale
            }
            "hu" -> {
                Locale("HUN")
            }
            // fallback to English
            else -> {
                Locale.ENGLISH
            }
        }
        return locale
    }

    fun getLanguageBCP47() : String {
        val currentLocale = Locale.getDefault()
        val languageBCP47 = when (currentLocale.language) {
            "en" -> {
                "en"
            }
            "hu" -> {
                "hu"
            }
            // fallback to English
            else -> {
                "en"
            }
        }

        return languageBCP47
    }

}