package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LongAudioDailyResult(
    val status: Int = 0,
    val errcode: Int = 0,
    val data: LongAudioDailyData? = null
) : Parcelable

@Parcelize
data class LongAudioDailyData(
    val is_end: Int = 0,
    val albums: List<LongAudioAlbum>? = null,
    val current_time: Long = 0
) : Parcelable

/**
 * 听书专辑
 */
@Parcelize
data class LongAudioAlbum(
    val album_id: Long = 0,                 // 专辑ID
    val album_name: String? = null,         // 专辑名称
    val intro: String? = null,              // 简介
    val sizable_cover: String? = null,      // 封面图
    val audio_total: Int = 0,               // 音频总数
    val play_count: Long = 0,               // 播放次数
    val is_pay: Int = 0,                    // 是否付费
    val tag_info: TagInfo? = null,          // 标签信息
    val rec_tag: RecTag? = null,            // 推荐标签
    val recommend_reason: String? = null,   // 推荐理由
    val publish_date: String? = null,       // 发布日期
    val subscribe_desc: String? = null      // 订阅描述
) : Parcelable

@Parcelize
data class TagInfo(
    val tag_id: Int = 0,
    val tag_name: String? = null
) : Parcelable

@Parcelize
data class RecTag(
    val album_id: String? = null,
    val tag_id: Int = 0,
    val tag_name: String? = null,
    val desc: String? = null,
    val cat_desc: String? = null
) : Parcelable