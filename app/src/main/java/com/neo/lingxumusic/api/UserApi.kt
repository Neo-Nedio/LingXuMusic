package com.neo.lingxumusic.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET

interface UserApi {
    @GET("user/playlist")
    suspend fun getUserPlayList(
    ): BaseResult
}