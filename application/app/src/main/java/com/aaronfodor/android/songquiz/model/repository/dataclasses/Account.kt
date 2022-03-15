package com.aaronfodor.android.songquiz.model.repository.dataclasses

data class Account (
    val id: String,
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val uri: String = "",
    val imageUri: String = "",
    val token: String = "",
    val tokenExpireTime: Long = 0L,
    var isFirstLoadAfterLogin: Boolean = true
)

data class PublicAccountInfo(
    val id: String,
    val name: String,
    val email: String,
    val country: String,
    val uri: String,
    val imageUri: String,
    val isFirstLoadAfterLogin: Boolean
)

fun Account.toPublicAccountInfo() : PublicAccountInfo{
    return PublicAccountInfo(
        id = this.id,
        name = this.name,
        email = this.email,
        country = this.country,
        uri = this.uri,
        imageUri = this.imageUri,
        isFirstLoadAfterLogin = this.isFirstLoadAfterLogin
    )
}