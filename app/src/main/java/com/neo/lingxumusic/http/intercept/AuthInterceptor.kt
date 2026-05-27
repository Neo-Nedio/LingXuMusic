package com.neo.lingxumusic.http.intercept

import okhttp3.Interceptor
import okhttp3.Response
import com.neo.lingxumusic.core.AppGlobalData

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = AppGlobalData.token
        val userId = AppGlobalData.userId

        val request = if (token.isNotEmpty()) {
            chain.request().newBuilder()
                .header("Authorization", "token=$token;userid=$userId")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}