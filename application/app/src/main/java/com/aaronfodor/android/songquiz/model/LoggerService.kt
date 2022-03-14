package com.aaronfodor.android.songquiz.model

import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class LoggerService  @Inject constructor(
    private val accountService: AccountService
){

    companion object{
        const val SHOW_QUIZ_SCREEN_EVENT = "show quiz screen"
        const val START_GAME_EVENT = "start game"
        const val GAME_PLAY_TRACK_EVENT = "game play track"
        const val END_GAME_EVENT = "end game"
        const val SEARCH_PLAYLIST_EVENT = "search playlist"
        const val ADD_PLAYLIST_EVENT = "add playlist"
        const val DELETE_PLAYLIST_EVENT = "delete playlist"
        const val ADD_TRACK_EVENT = "add track"
        const val DELETE_TRACK_EVENT = "delete track"
        const val DELETE_PROFILE_STATS_EVENT = "delete profile stats"
        const val DELETE_PLAYLISTS_EVENT = "delete playlists"
        const val DELETE_FAVOURITES_EVENT = "delete favourites"
        const val RESTORE_DEFAULTS_EVENT = "restore defaults"
        const val LOGOUT_EVENT = "logout"
        const val TOKEN_REFRESH_EVENT = "token refresh"
        const val SHOW_REWARDED_INTERSTITIAL_AD_EVENT = "show rewarded interstitial ad"

        const val PARAM_ACCOUNT_ID = "account id"
        const val PARAM_PLAYLIST_ID = "playlist id"
        const val PARAM_TRACK_ID = "track id"
        const val PARAM_SONG_URI = "song uri"
        const val PARAM_SEARCH_EXPRESSION = "search expression"
        const val PARAM_TAG = "tag class"
    }

    fun d(tag: String?, msg: String?){
        Log.println(Log.DEBUG, tag, msg ?: "")
    }

    fun logStartGame(tag: String?, playlistId: String){
        d(tag, "logStartGame")
        Firebase.analytics.logEvent(START_GAME_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logGamePlayTrack(tag: String?, soundUri: String){
        d(tag, "logGamePlayTrack")
        Firebase.analytics.logEvent(GAME_PLAY_TRACK_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_SONG_URI, soundUri)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logEndGame(tag: String?, playlistId: String){
        d(tag, "logEndGame")
        Firebase.analytics.logEvent(END_GAME_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logShowQuizScreen(tag: String?, playlistId: String){
        d(tag, "logShowQuizScreen")
        Firebase.analytics.logEvent(SHOW_QUIZ_SCREEN_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logSearchPlaylist(tag: String?, searchExpression: String){
        d(tag, "logSearchPlaylist")
        Firebase.analytics.logEvent(SEARCH_PLAYLIST_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_SEARCH_EXPRESSION, searchExpression)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logAddPlaylist(tag: String?, playlistId: String){
        d(tag, "logAddPlaylist")
        Firebase.analytics.logEvent(ADD_PLAYLIST_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeletePlaylist(tag: String?, playlistId: String){
        d(tag, "logDeletePlaylist")
        Firebase.analytics.logEvent(DELETE_PLAYLIST_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logAddTrack(tag: String?, trackId: String){
        d(tag, "logAddTrack")
        Firebase.analytics.logEvent(ADD_TRACK_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_TRACK_ID, trackId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteTrack(tag: String?, trackId: String){
        d(tag, "logDeleteTrack")
        Firebase.analytics.logEvent(DELETE_TRACK_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_TRACK_ID, trackId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteProfileStats(tag: String?){
        d(tag, "logDeleteProfileStats")
        Firebase.analytics.logEvent(DELETE_PROFILE_STATS_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeletePlaylists(tag: String?){
        d(tag, "logDeletePlaylists")
        Firebase.analytics.logEvent(DELETE_PLAYLISTS_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteFavourites(tag: String?){
        d(tag, "logDeleteFavourites")
        Firebase.analytics.logEvent(DELETE_FAVOURITES_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logRestoreDefaultPlaylists(tag: String?){
        d(tag, "logRestoreDefaultPlaylists")
        Firebase.analytics.logEvent(RESTORE_DEFAULTS_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logLogout(tag: String?){
        d(tag, "logLogout")
        Firebase.analytics.logEvent(LOGOUT_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logTokenRefresh(tag: String?){
        d(tag, "logTokenRefresh")
        Firebase.analytics.logEvent(TOKEN_REFRESH_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logShowRewardedInterstitialAd(tag: String?){
        d(tag, "logShowRewardedInterstitialAd")
        Firebase.analytics.logEvent(SHOW_REWARDED_INTERSTITIAL_AD_EVENT){
            tag?.let { param(PARAM_TAG, it) }
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

}