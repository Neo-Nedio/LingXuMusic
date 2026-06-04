package com.neo.lingxumusic.hilt.entrypoint

import com.neo.lingxumusic.http.api.LoginApi
import com.neo.lingxumusic.http.api.PlaylistApi
import com.neo.lingxumusic.http.api.RankApi
import com.neo.lingxumusic.http.api.RecommendApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.http.api.VideoApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent



@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiEntryPoint {
    fun getLoginApi(): LoginApi
    fun getUserApi(): UserApi
    fun getSongApi(): SongApi
    fun getVideoApi(): VideoApi
    fun getRecommendApi(): RecommendApi

    fun getRankApi(): RankApi

    fun getPlaylistApi(): PlaylistApi
}