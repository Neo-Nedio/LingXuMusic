package com.neo.lingxumusic.hilt.entrypoint

import com.neo.lingxumusic.http.api.LoginApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.http.api.UserApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent



@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiEntryPoint {
    fun getLoginApi(): LoginApi
    fun getUserApi(): UserApi
    fun getSongApi(): SongApi

}