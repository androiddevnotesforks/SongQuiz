package com.arpadfodor.android.songquiz.model.repository.dataclasses

data class Account (
        val id: String,
        val name: String = "",
        val email: String = "",
        val uri: String = "",
        val country: String = "",
        val token: String = "",
        val tokenExpireTime: Long = 0L
)