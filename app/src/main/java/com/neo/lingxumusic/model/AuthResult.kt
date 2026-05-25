package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * 发送验证码数据
 */
@Parcelize
data class AuthData(
    val dfid: String,      // 设备标识ID
    val scheme: Int        // 方案标识（0=正常）
) : Parcelable