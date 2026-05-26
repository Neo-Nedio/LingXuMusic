package com.neo.lingxumusic.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface SongApi {
    @GET("song/detail")
    suspend fun getSongDetail(@Query("ids") ids: String): BaseResult
}