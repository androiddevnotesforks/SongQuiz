package com.arpadfodor.android.songquiz.model.api

import okhttp3.Interceptor
import okhttp3.Credentials
import okhttp3.Response
import java.io.IOException

class EncoderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder().build()
        return chain.proceed(authenticatedRequest)
    }

}