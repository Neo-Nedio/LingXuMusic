package com.neo.lingxumusic.api

import com.neo.lingxumusic.model.PlaylistResult
import retrofit2.http.GET

interface UserApi {
    @GET("user/playlist")
    suspend fun getUserPlayList(
    ): PlaylistResult
}