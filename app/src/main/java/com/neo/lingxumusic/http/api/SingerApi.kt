package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query


interface SingerApi {
    @GET("/artist/detail")
    suspend fun getArtistDetail(
        @Query("id") id: Int,
    ): BaseResult

    @GET("/artist/audios")
    suspend fun getArtistAudios(
        @Query("id") id: Int,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30",
        @Query("sort") sort: String,
    ): BaseResult
}
