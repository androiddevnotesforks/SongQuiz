package com.arpadfodor.android.songquiz.model.api.dataclasses

/**
 *
 * @param href
 * @param display_name
 * @param email
 * @param id
 * @param uri
 * @param country
 */
data class AccountDTO (
    val href: Any? = null,
    val display_name: String = "",
    val email: String = "",
    val id: String = "",
    val uri: String = "",
    val country: String = ""
)