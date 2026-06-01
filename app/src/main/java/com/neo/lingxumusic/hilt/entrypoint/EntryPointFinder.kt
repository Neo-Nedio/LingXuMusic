package com.neo.lingxumusic.hilt.entrypoint


import com.neo.lingxumusic.http.api.LoginApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.core.LingxuApplication
import com.neo.lingxumusic.http.api.RecommendApi
import com.neo.lingxumusic.http.api.VideoApi
import dagger.hilt.EntryPoints


object EntryPointFinder {
    val entryPoint = EntryPoints.get(
        LingxuApplication.getAppContext(),  // 1. 传入 Application 上下文
        ApiEntryPoint::class.java           // 2. 传入 EntryPoint 接口类型
    )

    fun getLoginApi(): LoginApi {
        return entryPoint.getLoginApi()
    }
    fun getUserApi(): UserApi {
        return entryPoint.getUserApi()
    }
    fun getSongApi(): SongApi {
        return entryPoint.getSongApi()
    }
    fun getVideoApi(): VideoApi {
        return entryPoint.getVideoApi()
    }
    fun getRecommendApi(): RecommendApi {
        return entryPoint.getRecommendApi()
    }
}

