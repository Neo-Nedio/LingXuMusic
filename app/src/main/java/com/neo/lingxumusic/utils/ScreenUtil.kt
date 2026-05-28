package com.neo.lingxumusic.utils

import com.neo.lingxumusic.core.LingxuApplication

object ScreenUtil {
    /**
     * 获取屏幕宽度（px）
     */
    fun getScreenWidth(): Int {
        val context = LingxuApplication.getAppContext()
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（px）
     */
    fun getScreenHeight(): Int {
        val context = LingxuApplication.getAppContext()
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

    /**
     * 获取屏幕宽度（dp）
     */
    fun getScreenWidthDp(): Int {
        val context = LingxuApplication.getAppContext()
        val displayMetrics = context.resources.displayMetrics
        return (displayMetrics.widthPixels / displayMetrics.density).toInt()
    }

    /**
     * 获取屏幕高度（dp）
     */
    fun getScreenHeightDp(): Int {
        val context = LingxuApplication.getAppContext()
        val displayMetrics = context.resources.displayMetrics
        return (displayMetrics.heightPixels / displayMetrics.density).toInt()
    }
}