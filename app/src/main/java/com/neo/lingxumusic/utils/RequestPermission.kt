package com.neo.lingxumusic.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android 13+ 首次进入应用时申请通知权限，用于显示音乐播放前台通知。
 */
@Composable
fun RequestNotificationPermission() {
    // 1. Android 13 以下直接返回（不需要请求）
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current

    // 2. 创建权限请求启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission() //请求单个权限的 contract
    ) { isGranted ->
        // 权限请求结果回调（这里为空）
    }

    // 3. 组件首次加载时检查并请求权限
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //申请权限
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
