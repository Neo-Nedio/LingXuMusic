package com.neo.lingxumusic.hilt

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module                              // 告诉 Hilt 这是一个依赖提供模块
@InstallIn(SingletonComponent::class) // 全局单例，整个 App 生命周期
object RetrofitClient {

    @Qualifier                       // Dagger 用来识别依赖的标记
    @Retention(AnnotationRetention.BINARY)  // 注解保留在 class 文件中，供编译时使用
    annotation class KuGouRetrofitClient

    @Provides                        // 告诉 Hilt：这个方法提供依赖
    @Singleton                       // 单例模式，整个 App 只有一个实例
    @KuGouRetrofitClient            // 自定义标签，用于区分同类型的不同依赖
    fun provideKuGouRetrofitClient(): LibCoroutineNetwork {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.e("OkHttp", "OkHttp====:$message")
        }.apply {
            //todo 日志拦截器
            //HttpLoggingInterceptor.Level.NONE   不打印
            level = HttpLoggingInterceptor.Level.BODY // 打印请求/响应的完整信息
        }

        val builder = OkHttpClient.Builder().apply {
            writeTimeout(30, TimeUnit.SECONDS)   // 写入超时 30秒
            readTimeout(30, TimeUnit.SECONDS)    // 读取超时 30秒
            connectTimeout(30, TimeUnit.SECONDS) // 连接超时 30秒
            addInterceptor(loggingInterceptor)   // 添加日志拦截器
        }

        return LibCoroutineNetwork(
            Retrofit.Builder()
                .client(builder.build())                 // 设置 OkHttpClient
                .addConverterFactory(GsonConverterFactory.create())  // 设置 JSON 解析器
        )
    }
}