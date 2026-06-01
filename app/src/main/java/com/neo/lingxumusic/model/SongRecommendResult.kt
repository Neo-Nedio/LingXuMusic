package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 推荐数据
 */
@Parcelize
data class SongRecommendData(
    val card_id: Int = 0,                       // 推荐卡片类型
    val rec_desc: String? = null,               // 推荐描述，如"「精选好歌随心听」"
    val song_list: List<RecommendSong>? = null, // 歌曲列表
    val song_list_size: Int = 0                 // 歌曲数量
) : Parcelable

/**
 * 推荐歌曲信息
 */
@Parcelize
data class RecommendSong(
    val songname: String? = null,               // 歌名
    val author_name: String? = null,            // 歌手名
    val album_name: String? = null,             // 专辑名
    val sizable_cover: String? = null,          // 封面图
    val hash: String? = null,                   // 音频哈希（获取播放URL）
    val mixsongid: String? = null,              // 歌曲ID
    val album_audio_id: String? = null,         // 专辑音频ID
    val time_length: Double = 0.0,              // 时长（秒）
    val publish_date: String? = null,           // 发行日期
    val language: String? = null                // 语言
) : Parcelable