package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌手详情
 */
@Parcelize
data class ArtistDetail(
    // ========== 基本信息 ==========
    val author_id: String? = null,           // 歌手ID
    val author_name: String? = null,         // 歌手名称
    val pinyin_initial: String? = null,      // 拼音首字母（如 "HOYO-MIX"）
    val area_id: String? = null,             // 地区ID（1=内地）
    val is_publish: Int = 0,                 // 是否发布

    // ========== 图片 ==========
    val sizable_avatar: String? = null,      // 头像URL

    // ========== 统计数据 ==========
    val album_count: Int = 0,                // 专辑数量
    val song_count: Int = 0,                 // 歌曲数量
    val mv_count: Int = 0,                   // MV数量
    val fansnums: Int = 0,                   // 粉丝数

    // ========== 介绍 ==========
    val intro: String? = null,               // 简介
    val long_intro: List<ArtistIntro>? = null, // 详细介绍（分段）

    // ========== 其他 ==========
    val birthday: String? = null,            // 生日
    val user_status: Int = 0                 // 用户状态
) : Parcelable

/**
 * 歌手详细介绍（分段）
 */
@Parcelize
data class ArtistIntro(
    val title: String? = null,      // 段落标题（如"简介"、"基本资料"、"演艺经历"）
    val content: String? = null     // 段落内容
) : Parcelable