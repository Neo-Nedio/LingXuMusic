package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaylistApi {
    @GET("playlist/track/all")
    suspend fun getPlaylistSong(
        @Query("id") id: String?,
        @Query("page") page: Int? = null,
        @Query("pagesize") pagesize: Int? = null,
        //使用时间戳，防止okhttp缓存数据
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): BaseResult

    @GET("/playlist/detail")
    suspend fun getPlaylistDetail(
        @Query("ids") id: String?,
    ): BaseResult

    /** 收藏歌单/新建歌单 */
    @GET("/playlist/add")
    suspend fun addPlaylist(
        @Query("name") name: String?,
        @Query("list_create_userid") listCreateUserid: Long,
        @Query("list_create_listid") listCreateListid: Int,
        @Query("type") type: Int = 1,
        @Query("is_pri") isPri: Int? = null,
        @Query("list_create_gid") listCreateGid: String? = null,
    ): BaseResult

    /** 取消收藏歌单/删除歌单 */
    @GET("/playlist/del")
    suspend fun delPlaylist(
        @Query("listid") listid: Int,
    ): BaseResult
}
