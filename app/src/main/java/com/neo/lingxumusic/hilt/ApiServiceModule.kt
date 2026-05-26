package com.neo.lingxumusic.hilt

import com.neo.lingxumusic.api.LoginApi
import com.neo.lingxumusic.api.SongApi
import com.neo.lingxumusic.api.UserApi
import com.neo.lingxumusic.core.AppConfig
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
        @RetrofitClient.KuGouRetrofitClient retrofit: LibCoroutineNetwork // 注入网络客户端
    ): LoginApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(LoginApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserCenterApi(
        @RetrofitClient.KuGouRetrofitClient retrofit: LibCoroutineNetwork
    ): UserApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSongApi(
        @RetrofitClient.KuGouRetrofitClient retrofit: LibCoroutineNetwork
    ): SongApi {
        return retrofit.setBaseUrl(AppConfig.BASE_URL).create(SongApi::class.java)
    }
}