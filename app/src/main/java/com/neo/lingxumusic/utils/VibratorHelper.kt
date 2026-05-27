package com.neo.lingxumusic.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.neo.lingxumusic.core.LingxuApplication

object VibratorHelper {
    // 获取 Vibrator 服务的方式保持不变
    val context = LingxuApplication.getAppContext()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (API 31) 及以上，推荐使用 VibratorManager
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        // Android 12 以下，沿用旧方法
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    //震动
    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Android 8.0 以下，虽然弃用但还能用
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
}