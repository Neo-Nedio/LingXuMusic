package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("user/playlist")
    suspend fun getUserPlayList(
    ): BaseResult

    @GET("playlist/track/all")
    suspend fun getPlaylistSong(
        @Query("id") id: String?,
        @Query("page") page: Int? = null,
        @Query("pagesize") pagesize: Int? = null,
    ): BaseResult
}