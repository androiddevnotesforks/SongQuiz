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
 * @param album 
 * @param artists 
 * @param available_markets
 * @param disc_number
 * @param duration_ms
 * @param explicit 
 * @param external_ids
 * @param external_urls
 * @param href 
 * @param id 
 * @param name 
 * @param popularity 
 * @param preview_url
 * @param track_number
 * @param type 
 * @param uri 
 */
data class TrackDTO (
    val album: AlbumDTO? = null,
    val artists: Array<ArtistDTO>? = null,
    val available_markets: Array<String>? = null,
    val disc_number: Int? = null,
    val duration_ms: Int = 0,
    val explicit: Boolean? = null,
    val external_ids: TrackExternalIdsDTO? = null,
    val external_urls: PlaylistExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String = "",
    val name: String = "",
    val popularity: Int? = null,
    val preview_url: String? = null,
    val track_number: Int? = null,
    val type: String? = null,
    val uri: String? = null
)

/**
 *
 * @param isrc
 */
data class TrackExternalIdsDTO (
    val isrc: String? = null
)

/**
 *
 * @param album_type
 * @param available_markets
 * @param external_urls
 * @param href
 * @param id
 * @param images
 * @param name
 * @param type
 * @param uri
 */
data class AlbumDTO (
    val album_type: String? = null,
    val available_markets: Array<String>? = null,
    val external_urls: PlaylistExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String = "",
    val images: Array<ImagesDTO>? = null,
    val name: String = "",
    val type: String? = null,
    val uri: String? = null
)

/**
 *
 * @param external_urls
 * @param href
 * @param id
 * @param name
 * @param type
 * @param uri
 */
data class ArtistDTO (
    val external_urls: PlaylistExternalUrlsDTO? = null,
    val href: String? = null,
    val id: String = "",
    val name: String = "",
    val type: String? = null,
    val uri: String? = null
)