package com.neo.lingxumusic.hilt.moudle

import com.neo.lingxumusic.http.api.LoginApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.http.api.VideoApi
import com.neo.lingxumusic.core.AppConfig
import com.neo.lingxumusic.hilt.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Provides
    @Singleton
    fun provideLoginApi(
        @RetrofitClientModule.KuGouRetrofitClient retrofit: RetrofitClient // 注入网络客户端
    ): LoginApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(LoginApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserCenterApi(
        @RetrofitClientModule.KuGouRetrofitClient retrofit: RetrofitClient
    ): UserApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSongApi(
        @RetrofitClientModule.KuGouRetrofitClient retrofit: RetrofitClient
    ): SongApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(SongApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVideoApi(
        @RetrofitClientModule.KuGouRetrofitClient retrofit: RetrofitClient
    ): VideoApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(VideoApi::class.java)
    }
}