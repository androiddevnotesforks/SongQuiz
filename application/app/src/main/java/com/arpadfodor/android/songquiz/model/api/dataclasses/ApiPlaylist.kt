/**
 * Spofity API
 * Spotify API description
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package com.arpadfodor.android.songquiz.model.api.dataclasses

/**
 * 
 * @param collaborative 
 * @param description 
 * @param external_urls
 * @param followers 
 * @param images 
 * @param name 
 * @param owner 
 * @param &#x60;public&#x60; 
 * @param snapshot_id
 * @param tracks 
 * @param type 
 * @param uri
 * @param primary_color
 * @param id
 */
data class ApiPlaylist (
    val collaborative: Boolean? = null,
    val description: String? = null,
    val external_urls: PlaylistExternalUrls? = null,
    val followers: PlaylistFollowers? = null,
    val images: Array<PlaylistImages>? = null,
    val name: String,
    val owner: PlaylistOwner? = null,
    val `public`: Any? = null,
    val snapshot_id: String? = null,
    val tracks: PlaylistTracks? = null,
    val type: String? = null,
    val uri: String? = null,
    val primary_color: String? = null,
    val id: String
)

/**
 *
 * @param spotify
 */
data class PlaylistExternalUrls (
    val spotify: kotlin.String? = null
)

/**
 *
 * @param href
 * @param total
 */
data class PlaylistFollowers (
    val href: kotlin.Any? = null,
    val total: kotlin.Int? = null
)

/**
 *
 * @param url
 */
data class PlaylistImages (
    val url: kotlin.String? = null
)

/**
 *
 * @param external_urls
 * @param href
 * @param id
 * @param type
 * @param uri
 */
data class PlaylistOwner (
    val external_urls: PlaylistExternalUrls? = null,
    val href: kotlin.String? = null,
    val id: kotlin.String? = null,
    val type: kotlin.String? = null,
    val uri: kotlin.String? = null
)

/**
 *
 * @param href
 * @param items
 * @param limit
 * @param next
 * @param offset
 * @param previous
 * @param total
 */
data class PlaylistTracks (
    val href: kotlin.String? = null,
    val items: kotlin.Array<PlaylistTracksItems>? = null,
    val limit: kotlin.Int? = null,
    val next: kotlin.String? = null,
    val offset: kotlin.Int? = null,
    val previous: kotlin.Any? = null,
    val total: kotlin.Int? = null
)

/**
 *
 * @param added_at
 * @param added_by
 * @param is_local
 * @param track
 */
data class PlaylistTracksItems (
    val added_at: kotlin.String? = null,
    val added_by: PlaylistOwner? = null,
    val is_local: kotlin.Boolean? = null,
    val track: ApiTrack
)