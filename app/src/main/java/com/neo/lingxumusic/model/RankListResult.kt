package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 排行榜列表数据
 */
@Parcelize
data class RankListData(
    val total: Int = 0,                    // 排行榜总数
    val timestamp: Long = 0,               // 服务器时间戳
    val info: List<RankInfo>? = null       // 排行榜列表
) : Parcelable

/**
 * 单个排行榜信息
 */
@Parcelize
data class RankInfo(
    // ========== 基础信息 ==========
    val rankid: Int = 0,                   // 排行榜ID，如 8888(TOP500)、82831(网络热歌榜)
    val rank_cid: Int = 0,                 // 期数ID，用于获取完整榜单
    val rankname: String? = null,          // 排行榜名称，如 "TOP500"、"网络热歌榜"
    val play_times: Long = 0,              // 播放量/热度值
    val update_frequency: String? = null,  // 更新频率，如 "每天"、"周四"
    val intro: String? = null,             // 排行榜说明

    // ========== 分类 ==========
    val classify: Int = 0,                 // 分类: 1=星耀榜, 2=地区榜, 3=特色榜, 5=曲风榜

    // ========== 封面图 ==========
    val cover: String? = null,             // 排行榜封面图（图标）
    val songCover: String? = null,         // 歌曲封面（榜单第一首歌的专辑封面）

    // ========== 歌曲列表 ==========
    val songinfo: List<RankSong>? = null,  // 前3首歌曲（预览）

    // ========== 额外统计 ==========
    val extra: RankExtra? = null           // 榜单统计信息（总歌曲数、新歌数等）
) : Parcelable {

    /**
     * 获取分类名称
     */
    fun getCategoryName(): String {
        return when (classify) {
            1 -> "星耀榜"
            2 -> "地区榜"
            3 -> "特色榜"
            4 -> "全球榜"
            5 -> "曲风榜"
            else -> "未分类"
        }
    }
}

/**
 * 预览歌曲信息（前3首）
 */
@Parcelize
data class RankSong(
    val songname: String? = null,          // 歌曲名，格式如 "歌手 - 歌名"
    val author: String? = null,            // 歌手名
    val album_audio_id: Long = 0           // 歌曲ID，用于获取播放URL
) : Parcelable

/**
 * 额外统计信息
 */
@Parcelize
data class RankExtra(
    val resp: RankExtraResp? = null
) : Parcelable

/**
 * 额外统计详情
 */
@Parcelize
data class RankExtraResp(
    val all_total: Int = 0,                // 榜单总歌曲数，如 500
    val new_total: Int = 0,                // 本期新歌数，如 12
    val rank_tag: List<RankTag>? = null    // 榜单标签，如 [{"desc":"有12首上新","type":3}]
) : Parcelable

/**
 * 榜单标签
 */
@Parcelize
data class RankTag(
    val desc: String? = null,              // 标签描述，如 "有12首上新"
    val type: Int = 0                      // 标签类型，3表示上新
) : Parcelable