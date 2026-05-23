package com.neo.lingxumusic.utils

import android.app.Activity
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.transparentStatusBar() {
    //让状态栏变成完全透明，App 内容延伸到状态栏下面（全屏沉浸效果）
    WindowCompat.setDecorFitsSystemWindows(window, false)

    //让内容全展示，状态栏和导航栏都变为透明
    (this as? ComponentActivity)?.enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    )
}

//控制状态栏上的图标颜色
fun Activity.setAndroidNativeLightStatusBar(isLight: Boolean = true) {
    //创建一个控制器，用于控制系统栏的外观（颜色、图标等）
    WindowInsetsControllerCompat(window, window.decorView)
        //设置颜色
        .isAppearanceLightStatusBars = isLight
}