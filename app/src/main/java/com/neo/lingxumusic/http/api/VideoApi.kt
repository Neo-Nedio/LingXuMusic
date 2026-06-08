package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.VideoUrlResult
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoApi {
    @GET("/brush")
    suspend fun getBrushVideo(): BaseResult

    /**
     * 获取歌曲MV信息
     */
    @GET("/kmr/audio/mv")
    suspend fun getSongMv(
        @Query("album_audio_id") albumAudioId: Long,
        @Query("fields") fields: String = "mkv,tags,authors"
    ): BaseResult

    /**
     * 获取视频播放URL
     */
    @GET("/video/url")
    suspend fun getVideoUrl(
        @Query("hash") hash: String
    ): VideoUrlResult
}