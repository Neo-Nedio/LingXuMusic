package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.FloorCommentResult
import com.neo.lingxumusic.model.LyricResult
import com.neo.lingxumusic.model.LyricSearchResult
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
        @Query("pagesize") pagesize: String = "30",
        @Query("show_hotword_list") show_hotword_list : String = "1"
    ): SongCommentResult

    @GET("/comment/music/hotword")
    suspend fun getSongCommentByHotWord(
        @Query("mixsongid") mixsongid: String,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30",
        @Query("hot_word") hot_word : String
    ): SongCommentResult

    @GET("/comment/floor")
    suspend fun getCommentFloor(
        @Query("special_id") specialId: String,      // 评论下的 special_child_id
        @Query("mixsongid") mixsongid: String,        // 歌曲的 mixsongid
        @Query("tid") tid: String,                    // 主评论 ID
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30"
    ): FloorCommentResult

    @GET("/search/lyric")
    suspend fun getLyricSearch(@Query("hash") hash: String): LyricSearchResult

    @GET("/lyric")
    suspend fun getLyric(
        @Query("id") id: String, //可以从 /search/lyric 接口中获取
        @Query("accesskey") accesskey: String, //可以从 /search/lyric 接口中获取
        @Query("fmt") fmt: String = "krc",
        @Query("decode") page: Boolean = true,
    ): LyricResult
}