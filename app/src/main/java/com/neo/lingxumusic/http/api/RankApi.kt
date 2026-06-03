package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET

interface RankApi {
    @GET("/rank/list")
    suspend fun getRankList(): BaseResult
}