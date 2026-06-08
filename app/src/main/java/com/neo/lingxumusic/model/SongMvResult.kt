package com.neo.lingxumusic.model

import android.os.Parcelable
import com.neo.lingxumusic.utils.replaceSize
import kotlinx.parcelize.Parcelize


/**
 * 歌曲MV信息
 */
@Parcelize
data class SongMv(
    val video_id: Int = 0,                  // 视频ID
    val mv_name: String? = null,            // MV名称
    val singer: String? = null,             // 歌手名
    val duration: Int = 0,                  // 时长（毫秒）
    val play_times: Int = 0,                // 播放次数
    val collection_total: Int = 0,          // 收藏数
    val hdpic: String? = null,              // 高清封面图
    val authors: List<MvAuthor>? = null,    // 作者列表
    val mkv: MkvInfo? = null                // MKV视频信息（含hash）
) : Parcelable {

    /**
     * 获取视频hash（用于获取播放URL）
     */
    fun getVideoHash(): String? = mkv?.qhd_hash ?: mkv?.sd_hash

    /**
     * 获取封面图
     */
    fun getCoverUrl(size: Int = 400): String? {
        return hdpic?.replaceSize(size)
    }
}

/**
 * MV作者信息
 */
@Parcelize
data class MvAuthor(
    val author_id: Long = 0,
    val author_name: String? = null,
    val sizable_avatar: String? = null
) : Parcelable

/**
 * MKV视频信息
 */
@Parcelize
data class MkvInfo(
    val sd_hash: String? = null,            // 标清视频hash
    val qhd_hash: String? = null            // 高清视频hash
) : Parcelable