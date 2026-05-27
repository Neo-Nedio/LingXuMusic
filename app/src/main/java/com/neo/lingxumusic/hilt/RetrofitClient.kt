package com.neo.lingxumusic.hilt

import retrofit2.Retrofit

/*

┌─────────────────────────────────────────────────────────────────────┐
│                            App 启动                                  │
└─────────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────────┐
│                     RetrofitClientModule                            │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │ • 创建 OkHttpClient（超时、拦截器）                              ││
│  │ • 创建 RetrofitClient 实例                                      ││
│  │ • 提供 @KuGouRetrofitClient 标记的依赖                          ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────────┐
│                       ApiServiceModule                              │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │ • 注入 RetrofitClient                                           ││
│  │ • 创建 LoginApi / UserApi / SongApi                            ││
│  │ • 提供具体的 API 实例                                           ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
│
┌───────────────┼───────────────┐
▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  LoginApi   │  │  UserApi    │  │  SongApi    │
└─────────────┘  └─────────────┘  └─────────────┘
│               │               │
└───────────────┼───────────────┘
▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ViewModel (@HiltViewModel)                     │
│  • @Inject constructor(api: UserApi)  ← 自动注入                    │
└─────────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────────┐
│                      EntryPoint + Finder                            │
│  • 供非 Hilt 组件（工具类、ContentProvider 等）手动获取 API          │
└─────────────────────────────────────────────────────────────────────┘
*/

class RetrofitClient constructor(private val builder: Retrofit.Builder) {

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