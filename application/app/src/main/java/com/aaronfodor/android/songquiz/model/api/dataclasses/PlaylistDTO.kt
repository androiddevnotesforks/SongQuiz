/**
 * Spotify API
 * Spotify API description
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package com.aaronfodor.android.songquiz.model.api.dataclasses

/**
 *
 * @param playlists
 */
data class PlaylistsResponseDTO (
    val playlists: PlaylistsDTO
)

/**
 *
 * @param href
 * @param items
 * @param next
 * @param previous
 * @param limit
 * @param offset
 * @param total
 */
data class PlaylistsDTO (
    val href: Any? = null,
    val items: Array<PlaylistDTO> = emptyArray(),
    val next: String = "",
    val previous: String = "",
    val limit: Int = 0,
    val offset: Int = 0,
    val total: Int = 0
)

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
data class PlaylistDTO (
    val collaborative: Boolean? = null,
    val description: String? = null,
    val external_urls: PlaylistExternalUrlsDTO? = null,
    val followers: PlaylistFollowersDTO? = null,
    val images: Array<ImagesDTO>? = null,
    val name: String = "",
    val owner: PlaylistOwnerDTO? = null,
    val `public`: Any? = null,
    val snapshot_id: String? = null,
    val tracks: PlaylistTracksDTO? = null,
    val type: String? = null,
    val uri: String? = null,
    val primary_color: String? = null,
    val id: String = ""
)

/**
 *
 * @param spotify
 */
data class PlaylistExternalUrlsDTO (
    val spotify: String? = null
)

/**
 *
 * @param href
 * @param total
 */
data class PlaylistFollowersDTO (
    val href: Any? = null,
    val total: Int? = null
)

/**
 *
 * @param display_name
 * @param external_urls
 * @param href
 * @param id
 * @param type
 * @param uri
 */
data class PlaylistOwnerDTO (
    val display_name: String? = null,
    val external_urls: PlaylistExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String? = null,
    val type: String? = null,
    val uri: String? = null
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
data class PlaylistTracksDTO (
    val href: String? = null,
    val items: Array<PlaylistTracksItemsDTO>? = null,
    val limit: Int? = null,
    val next: String? = null,
    val offset: Int? = null,
    val previous: Any? = null,
    val total: Int? = null
)

/**
 *
 * @param added_at
 * @param added_by
 * @param is_local
 * @param track
 */
data class PlaylistTracksItemsDTO (
    val added_at: String? = null,
    val added_by: PlaylistOwnerDTO? = null,
    val is_local: Boolean? = null,
    val track: TrackDTO? = null
)