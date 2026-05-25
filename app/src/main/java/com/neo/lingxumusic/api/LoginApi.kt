package com.neo.lingxumusic.api

import com.neo.lingxumusic.model.AuthData
import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginApi {

    @GET("/captcha/sent")
    suspend fun sent(
        @Query("mobile") phone: String,
    ): BaseResult

    @GET("login/cellphone")
    suspend fun login(
        @Query("mobile") phone: String,
        @Query("code") code: String,
        @Query("userid") userid : String?
    ): BaseResult

    @GET("/login/token")
    suspend fun refreshToken(
        @Query("token") token: String,
        @Query("userid") userid : String
    ): BaseResult

    @GET("/register/dev")
    suspend fun AuthData(
    ):AuthData

}