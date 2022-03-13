package com.aaronfodor.android.songquiz.model

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
        const val SHOW_INTERSTITIAL_AD_EVENT = "show interstitial ad"

        const val PARAM_ACCOUNT_ID = "account id"
        const val PARAM_PLAYLIST_ID = "playlist id"
        const val PARAM_TRACK_ID = "track id"
        const val PARAM_SONG_URI = "song uri"
        const val PARAM_SEARCH_EXPRESSION = "search expression"
    }

    fun logStartGame(playlistId: String){
        Firebase.analytics.logEvent(START_GAME_EVENT){
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logGamePlayTrack(soundUri: String){
        Firebase.analytics.logEvent(GAME_PLAY_TRACK_EVENT){
            param(PARAM_SONG_URI, soundUri)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logEndGame(playlistId: String){
        Firebase.analytics.logEvent(END_GAME_EVENT){
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logShowQuizScreen(playlistId: String){
        Firebase.analytics.logEvent(SHOW_QUIZ_SCREEN_EVENT){
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logSearchPlaylist(searchExpression: String){
        Firebase.analytics.logEvent(SEARCH_PLAYLIST_EVENT){
            param(PARAM_SEARCH_EXPRESSION, searchExpression)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logAddPlaylist(playlistId: String){
        Firebase.analytics.logEvent(ADD_PLAYLIST_EVENT){
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeletePlaylist(playlistId: String){
        Firebase.analytics.logEvent(DELETE_PLAYLIST_EVENT){
            param(PARAM_PLAYLIST_ID, playlistId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logAddTrack(trackId: String){
        Firebase.analytics.logEvent(ADD_TRACK_EVENT){
            param(PARAM_TRACK_ID, trackId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteTrack(trackId: String){
        Firebase.analytics.logEvent(DELETE_TRACK_EVENT){
            param(PARAM_TRACK_ID, trackId)
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteProfileStats(){
        Firebase.analytics.logEvent(DELETE_PROFILE_STATS_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeletePlaylists(){
        Firebase.analytics.logEvent(DELETE_PLAYLISTS_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logDeleteFavourites(){
        Firebase.analytics.logEvent(DELETE_FAVOURITES_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logRestoreDefaultPlaylists(){
        Firebase.analytics.logEvent(RESTORE_DEFAULTS_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logLogout(){
        Firebase.analytics.logEvent(LOGOUT_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logTokenRefresh(){
        Firebase.analytics.logEvent(TOKEN_REFRESH_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

    fun logShowInterstitialAd(){
        Firebase.analytics.logEvent(SHOW_INTERSTITIAL_AD_EVENT){
            param(PARAM_ACCOUNT_ID, accountService.getAccountId())
        }
    }

}