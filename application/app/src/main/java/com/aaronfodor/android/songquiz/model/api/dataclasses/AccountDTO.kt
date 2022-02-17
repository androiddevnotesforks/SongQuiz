package com.aaronfodor.android.songquiz.model.api.dataclasses

/**
 *
 * @param href
 * @param display_name
 * @param email
 * @param id
 * @param images
 * @param uri
 * @param country
 */
data class AccountDTO (
    val href: Any? = null,
    val display_name: String = "",
    val email: String = "",
    val id: String = "",
    val images: Array<AccountImagesDTO>? = null,
    val uri: String = "",
    val country: String = ""
)

/**
 *
 * @param url
 */
data class AccountImagesDTO (
    val height: Int = 0,
    val width: Int = 0,
    val url: String = ""
)