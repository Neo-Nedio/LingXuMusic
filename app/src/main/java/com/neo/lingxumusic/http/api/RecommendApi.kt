package com.neo.lingxumusic.http.api

import com.neo.lingxumusic.model.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendApi {
    @GET("/top/card")
    suspend fun getSongRecommend(@Query("card_id") card_id: String)
    //1：精选好歌随心听，2：经典怀旧金曲，3：热门好歌精选，4： 小众宝藏佳作，5：未知，6. vip 专属推荐
    : BaseResult

    @GET("/everyday/recommend")
    //每日推荐，可复用上面的歌曲推荐结构
    suspend fun getEveryDayRecommend(@Query("platform") platform: String = "android"): BaseResult

    @GET("/top/playlist")
    //返回的data是场景歌单，而不是普通歌单
    suspend fun getPlaylistRecommend(@Query("category_id") category_id: String = "0"): BaseResult

    @GET("/personal/fm")
    //猜你喜欢 对应PersonalFmData
    suspend fun guessYourLike(): BaseResult
}