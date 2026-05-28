package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.SongCommentResult
import com.neo.lingxumusic.model.SongPlayInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface SongApi {
    @GET("song/url")
    suspend fun getSongUrl(@Query("hash") hash: String): SongPlayInfo

    @GET("/comment/music")
    suspend fun getSongComment(
        @Query("mixsongid") mixsongid: String,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30"
    ): SongCommentResult
}