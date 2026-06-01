package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET

interface VideoApi {
    @GET("/brush")
    suspend fun getBrushVideo(): BaseResult
}