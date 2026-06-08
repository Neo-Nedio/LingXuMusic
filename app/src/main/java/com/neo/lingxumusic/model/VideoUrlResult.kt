package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 视频播放URL响应
 * 对应接口: /video/url
 */
@Parcelize
data class VideoUrlResult(
    val status: Int = 0,                       // 状态码，1表示成功
    val privileges: Map<String, Int>? = null,  // 视频hash -> 权限（0=不可用）
    val data: Map<String, VideoUrlInfo>? = null // 视频hash -> 播放信息
) : Parcelable

/**
 * 视频播放信息
 */
@Parcelize
data class VideoUrlInfo(
    val downurl: String? = null,               // 播放地址
    val backupdownurl: List<String>? = null,   // 备用播放地址列表
    val filesize: String? = null               // 文件大小（字节）
) : Parcelable {

    /**
     * 获取可用的播放地址
     */
    fun getPlayUrl(): String? {
        return downurl?.takeIf { it.isNotBlank() }
            ?: backupdownurl?.firstOrNull()
    }
}