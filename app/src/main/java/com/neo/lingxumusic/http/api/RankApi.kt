package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface RankApi {
    @GET("/rank/list")
    suspend fun getRankList(): BaseResult

    @GET("/rank/audio")
    suspend fun getRankSong(
        @Query("rankid") rankid: Int,
        @Query("page") page: Int,
        @Query("pagesize") pagesize: Int,
    )
    : BaseResult
}