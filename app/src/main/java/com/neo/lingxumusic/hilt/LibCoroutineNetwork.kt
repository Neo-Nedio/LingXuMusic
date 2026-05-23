package com.neo.lingxumusic.hilt

import retrofit2.Retrofit

class LibCoroutineNetwork constructor(private val builder: Retrofit.Builder) {

    // 缓存：key = baseUrl, value = 对应的 Retrofit 实例
    private val retrofitCache = hashMapOf<String, Retrofit>()

    // 根据 baseUrl 获取（或创建）Retrofit 实例
    fun setBaseUrl(baseUrl: String): Retrofit {
        val cacheRetrofit = retrofitCache[baseUrl]
        return if (cacheRetrofit != null) {
            cacheRetrofit
        } else {
            val retrofit = builder
                .baseUrl(baseUrl)
                .build()
            retrofitCache[baseUrl] = retrofit
            retrofit
        }
    }
}