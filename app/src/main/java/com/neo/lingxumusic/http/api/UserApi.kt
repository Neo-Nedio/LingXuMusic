package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("user/playlist")
    suspend fun getUserPlayList(
        //使用时间戳，防止okhttp缓存数据
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): BaseResult


    @GET("/user/detail")
    suspend fun getUserDetail(
    ): BaseResult

    @GET("/user/follow")
    suspend fun getUserFollow(
    ): BaseResult
}