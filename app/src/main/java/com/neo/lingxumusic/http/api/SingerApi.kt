package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.ArtistAlbumsResult
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

    @GET("/artist/videos")
    suspend fun getArtistVideos(
        @Query("id") id: Int,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30",
        @Query("tag") tag: String,//official: 官方版本，live：现场版本，fan：饭制版本，artist: 歌手发布, all: 获取全部，默认为获取全部
    ): BaseResult

    @GET("/artist/albums")
    suspend fun getArtistAlbums(
        @Query("id") id: Int,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30",
        @Query("sort") sort: String,
    ): ArtistAlbumsResult

    @GET("/album/songs")
    suspend fun getAudiosSongs(
        @Query("id") id: Int,
        @Query("page") page: String = "1",
        @Query("pagesize") pagesize: String = "30",
    ): BaseResult
}
