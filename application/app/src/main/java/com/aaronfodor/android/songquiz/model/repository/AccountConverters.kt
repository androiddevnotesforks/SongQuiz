package com.aaronfodor.android.songquiz.model.repository

import com.aaronfodor.android.songquiz.model.api.dataclasses.AccountDTO
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Account

fun AccountDTO.toAccount() : Account {
    return Account(
        id = this.id,
        name = this.display_name,
        email = this.email,
        uri = this.uri,
        country = this.country,
        token = "",
        tokenExpireTime = 0L
    )
}