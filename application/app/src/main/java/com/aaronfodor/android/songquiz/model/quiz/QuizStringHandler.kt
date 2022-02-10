/*
 * Copyright (c) Aaron Fodor  - All Rights Reserved
 *
 * https://github.com/aaronfodor
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aaronfodor.android.songquiz.model.quiz

import android.content.Context
import com.aaronfodor.android.songquiz.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class QuizStringHandler @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun player(id: Int) : String{
        return context.getString(R.string.c_player, id.toString())
    }

    fun error() : String{
        return context.getString(R.string.c_error)
    }

    fun welcome() : String{
        return context.getString(R.string.c_welcome, context.getString(R.string.app_name))
    }

    fun playlistInfo(playlistName: String, numTracks: Int) : String{
        return context.getString(R.string.c_playlist_info, playlistName, numTracks.toString())
    }

    fun notEnoughTracks(minNumTracksNeeded: Int) : String{
        return context.getString(R.string.c_not_enough_tracks, minNumTracksNeeded.toString())
    }

    fun reconfigure(playlistName: String, numTracks: Int) : String{
        return context.getString(R.string.c_reconfigure, playlistName, numTracks.toString())
    }

    fun askNumPlayers() : String{
        return context.getString(R.string.c_ask_num_players)
    }

    fun notUnderstand() : String{
        return context.getString(R.string.c_not_understand)
    }

    fun numPlayersSelected(numPlayers: Int) : String{
        return context.getString(R.string.c_num_players_selected, numPlayers.toString())
    }

    fun askGameType() : String{
        return context.getString(R.string.c_ask_game_type)
    }

    fun askGameTypeInvalid(typeNameStringKey: Int, numTracks: Int, numPlayers: Int, numRounds: Int) : String{
        val quizTypeName = context.resources.getStringArray(typeNameStringKey)[0]
        return context.getString(R.string.c_ask_game_type_invalid, quizTypeName, numTracks.toString(), (numPlayers * numRounds).toString(), numPlayers.toString())
    }

    fun repeatSong() : String{
        return context.getString(R.string.c_repeating_song)
    }

    fun playerTurn(playerName: String, currentRoundIndex: Int) : String{
        return context.getString(R.string.c_player_turn, playerName, currentRoundIndex.toString())
    }

    fun endGame() : String{
        return context.getString(R.string.c_end_game)
    }

    fun playAgainPossible() : String{
        return context.getString(R.string.c_play_again_possible)
    }

    fun selectedGameAndSettingsString(extendedInfoAllowed: Boolean, repeatAllowed: Boolean, difficultyCompensation: Boolean,
                                      pointForTitle: Int, pointForArtist: Int, pointForDifficulty: Int, songDurationSec: Int, numRounds: Int, quizTypeNameStringKey: Int) : String{
        var isRepeatAllowed = ""
        if(!repeatAllowed){
            isRepeatAllowed = context.getString(R.string.c_not)
        }

        var infoString = ""

        if(extendedInfoAllowed){
            // add points info
            var pointsInfo = ""
            // title points
            pointsInfo += context.getString(R.string.c_points_for_the, pointForTitle.toString(), context.getString(R.string.c_title))
            // conjunction if needed
            pointsInfo += if(difficultyCompensation){
                ", "
            } else{
                "${context.getString(R.string.c_comma_and_and)} "
            }
            // artist points
            pointsInfo += context.getString(R.string.c_points_for_the, pointForArtist.toString(), context.getString(R.string.c_artist))
            // difficulty compensation and conjunction
            if(difficultyCompensation){
                pointsInfo += "${context.getString(R.string.c_comma_and_and)} "
                pointsInfo += context.getString(R.string.c_points_for_the, pointForDifficulty.toString(), context.getString(R.string.c_difficulty))
            }
            // merge the points info into one string
            infoString += context.getString(R.string.c_game_type_points, pointsInfo) + " "
        }

        infoString += context.getString(R.string.c_settings_info, songDurationSec.toString(), isRepeatAllowed)
        infoString = infoString.replace("  ", " ")

        val quizTypeName = context.resources.getStringArray(quizTypeNameStringKey)[0]
        return context.getString(R.string.c_game_type_selected, quizTypeName, numRounds.toString()) + " " + infoString
    }

    fun firstTurnString(currentPlayerName: String, quizTypeNameStringKey: Int, currentRoundIndex: Int) : String{
        val quizTypeName = context.resources.getStringArray(quizTypeNameStringKey)[0]
        return context.getString(R.string.c_starting_game, quizTypeName) + " " + context.getString(R.string.c_player_turn, currentPlayerName, currentRoundIndex.toString())
    }

    fun guessFeedbackString(lastPlayerArtistTitlePoints: Int, lastSongTitleHit: Boolean, lastSongArtistHit: Boolean,
                            lastPlayerDifficultyPoints: Int, lastPlayerDifficultyPercentage: Int,
                            lastSongTitle: String, lastSongArtist: String, lastSongAlbum: String,
                            extendedInfoAllowed: Boolean, lastPlayerAllPoints: Int, difficultyCompensation: Boolean) : String{
        var resultString = ""

        if(lastPlayerArtistTitlePoints > 0){
            var forWhatString = ""
            if(lastSongTitleHit){
                forWhatString += context.getString(R.string.c_title)
            }
            if(lastSongArtistHit){
                if(forWhatString.isNotEmpty()){
                    forWhatString += " ${context.getString(R.string.c_and)} "
                }
                forWhatString += context.getString(R.string.c_artist)
            }

            var pointsString = ""
            pointsString += context.getString(R.string.c_points_for_the, lastPlayerArtistTitlePoints.toString(), forWhatString)

            var difficultyString = ""
            if(difficultyCompensation){
                difficultyString += "${context.getString(R.string.c_comma_and_and)} "
                difficultyString += context.getString(R.string.c_difficulty_compensation, lastPlayerDifficultyPoints.toString(), lastPlayerDifficultyPercentage.toString())
            }
            pointsString += difficultyString

            val goodGuessPrefix = context.resources.getStringArray(R.array.good_guess_prefixes).random()
            resultString = context.getString(R.string.c_player_good_guess, goodGuessPrefix, pointsString)
        }
        else{
            val badGuessPrefix = context.resources.getStringArray(R.array.failed_guess_prefixes).random()

            var pointsString = ""
            if(difficultyCompensation){
                pointsString += context.getString(R.string.c_difficulty_compensation, lastPlayerDifficultyPoints.toString(), lastPlayerDifficultyPercentage.toString())
                resultString = context.getString(R.string.c_player_bad_guess, badGuessPrefix, pointsString)
            }
            else{
                resultString = badGuessPrefix
            }
        }

        var songInfoString = context.getString(R.string.c_song_info, lastSongTitle, lastSongArtist) + " "
        if(extendedInfoAllowed){
            // extended info
            songInfoString += context.getString(R.string.c_song_extended_info, lastSongAlbum) + " "
        }
        songInfoString += context.getString(R.string.c_your_score, lastPlayerAllPoints.toString())

        return "$resultString $songInfoString".replace("  ", " ")
    }

    fun endResultString(quizPlayers: List<QuizPlayer>, difficultyCompensation: Boolean) : String{
        var resultString = ""

        var winnerName = ""
        var winnerPoints = 0
        for(player in quizPlayers){
            val currentPlayerPoints = player.getPoints(difficultyCompensation)
            resultString += context.getString(R.string.c_player_scored, player.name, currentPlayerPoints.toString()) + " "
            if(currentPlayerPoints > winnerPoints){
                winnerPoints = currentPlayerPoints
                winnerName = player.name
            }
            else if(currentPlayerPoints == winnerPoints){
                winnerName = ""
            }
        }

        resultString += when {
            winnerPoints <= 0 -> {
                context.getString(R.string.c_next_time)
            }
            winnerName.isNotBlank() -> {
                context.getString(R.string.c_winner_player, winnerName)
            }
            else -> {
                context.getString(R.string.c_winner_tie)
            }
        }

        return resultString
    }

    fun possibleWords_1() : List<String>{ return context.resources.getStringArray(R.array.input_1).toList() }
    fun possibleWords_2() : List<String>{ return context.resources.getStringArray(R.array.input_2).toList() }
    fun possibleWords_3() : List<String>{ return context.resources.getStringArray(R.array.input_3).toList() }
    fun possibleWords_4() : List<String>{ return context.resources.getStringArray(R.array.input_4).toList() }

    fun possibleWords_oneshot() : List<String>{ return context.resources.getStringArray(R.array.input_oneshot).toList() }
    fun possibleWords_short() : List<String>{ return context.resources.getStringArray(R.array.input_short).toList() }
    fun possibleWords_medium() : List<String>{ return context.resources.getStringArray(R.array.input_medium).toList() }
    fun possibleWords_long() : List<String>{ return context.resources.getStringArray(R.array.input_long).toList() }

    fun possibleWords_repeat() : List<String>{ return context.resources.getStringArray(R.array.input_repeat).toList() }

    fun possibleWords_restart() : List<String>{ return context.resources.getStringArray(R.array.input_restart).toList() }
    fun possibleWords_configure() : List<String>{ return context.resources.getStringArray(R.array.input_configure).toList() }
    fun possibleWords_exit() : List<String>{ return context.resources.getStringArray(R.array.input_exit).toList() }

}