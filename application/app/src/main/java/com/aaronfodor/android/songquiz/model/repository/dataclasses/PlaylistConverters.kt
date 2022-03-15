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

package com.aaronfodor.android.songquiz.model.repository.dataclasses

import androidx.core.text.HtmlCompat
import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistDTO
import com.aaronfodor.android.songquiz.model.api.dataclasses.PlaylistsDTO
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbPlaylist

fun Playlist.toDbPlaylist(accountId: String, timestampUTC: String) : DbPlaylist {
    return DbPlaylist(
        id = this.id,
        accountId = accountId,
        timestampUTC = timestampUTC,
        name = this.name,
        description = this.description,
        owner = this.owner,
        imageUri = this.imageUri,
        uri = this.uri
    )
}

fun DbPlaylist.toPlaylist() : Playlist {
    return Playlist(
        id = this.id,
        name = this.name,
        description = this.description,
        owner = this.owner,
        imageUri = this.imageUri,
        uri = this.uri,
        tracks = arrayListOf()
    )
}

fun PlaylistDTO.toPlaylist() : Playlist {
    val tracks = mutableListOf<Track>()

    this.tracks?.items?.let {
        for(apiTrackWrapper in it){
            apiTrackWrapper.track?.preview_url?.let { previewUrl ->
                if(previewUrl.isNotBlank()){
                    val currentTrack = apiTrackWrapper.track.toTrack()
                    tracks.add(currentTrack)
                }
            }
        }
    }

    val imageUri = this.images?.getProperImageUri() ?: ""

    return Playlist(
        id = this.id,
        name = this.name,
        description = HtmlCompat.fromHtml(this.description ?: "", 0).toString(),
        owner = this.owner?.display_name ?: "",
        imageUri = imageUri,
        followers = this.followers?.total ?: 0,
        type = this.type ?: "",
        uri = this.uri ?: "",
        primary_color = this.primary_color ?: "",
        tracks = tracks
    )
}

fun PlaylistsDTO.toSearchResult(searchExpression: String) : PlaylistSearchResult{
    val playlists = mutableListOf<Playlist>()
    for(item in this.items){
        playlists.add(item.toPlaylist())
    }

    return PlaylistSearchResult(
        items = playlists,
        searchExpression = searchExpression,
        limit = this.limit,
        offset = this.offset,
        total = this.total
    )
}