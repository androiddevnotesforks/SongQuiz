package com.arpadfodor.android.songquiz.model.repository

import com.arpadfodor.android.songquiz.model.api.dataclasses.AccountDTO
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Account

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