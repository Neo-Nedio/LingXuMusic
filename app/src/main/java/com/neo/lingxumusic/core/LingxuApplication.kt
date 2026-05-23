package com.neo.lingxumusic.core

import android.app.Application
import android.content.Context
import com.neo.lingxumusic.utils.KVCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LingxuApplication : Application() {

    companion object {
        private lateinit var instance: LingxuApplication

        fun getInstance(): LingxuApplication = instance

        // 提供全局 Context
        fun getAppContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        KVCache.init(this)
    }
}