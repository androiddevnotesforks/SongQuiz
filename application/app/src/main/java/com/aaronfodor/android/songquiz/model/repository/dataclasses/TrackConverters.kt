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

import com.aaronfodor.android.songquiz.model.api.dataclasses.TrackDTO
import com.aaronfodor.android.songquiz.model.database.dataclasses.DbTrack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun Track.toDbTrack(accountId: String, timestampUTC: String) : DbTrack {
    val encodedArtists = Gson().toJson(this.artists)

    return DbTrack(
        id = this.id,
        accountId = accountId,
        timestampUTC = timestampUTC,
        name = this.name,
        artists = encodedArtists,
        album = this.album,
        imageUri = this.imageUri,
        durationMs = this.durationMs,
        popularity = this.popularity,
        uri = this.uri,
        previewUrl = this.previewUrl
    )
}

fun DbTrack.toTrack() : Track {
    val decodedArtists = Gson().fromJson<List<String>>(this.artists, object : TypeToken<List<String>>() {}.type)

    return Track(
        id = this.id,
        name = this.name,
        artists = decodedArtists,
        album = this.album,
        imageUri = this.imageUri,
        durationMs = this.durationMs,
        popularity = this.popularity,
        uri = this.uri,
        previewUrl = this.previewUrl
    )
}

fun TrackDTO.toTrack() : Track{
    val artists = mutableListOf<String>()
    this.artists?.let {
        for(artist in it){
            artists.add(artist.name)
        }
    }

    val imageUri = this.album?.images?.getProperImageUri() ?: ""

    return Track(
        id = this.id,
        name = this.name,
        artists = artists,
        album = this.album?.name?: "",
        imageUri = imageUri,
        durationMs = this.duration_ms,
        uri = this.uri ?: "",
        popularity = this.popularity ?: 0,
        previewUrl = this.preview_url ?: ""
    )
}