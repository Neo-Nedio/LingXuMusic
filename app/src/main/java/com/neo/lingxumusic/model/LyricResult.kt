package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌词搜索结果响应（/search/lyric 接口）
 */
@Parcelize
data class LyricSearchResult(
    val status: Int = 0,                    // 状态码，200 表示成功
    val info: String? = null,               // 状态信息，"OK"
    val errcode: Int = 0,                   // 错误码，200 表示成功
    val errmsg: String? = null,             // 错误信息
    val keyword: String? = null,            // 搜索关键词
    val proposal: String? = null,           // 建议的歌词 ID
    val has_complete_right: Int = 0,        // 是否有完整权限
    val expire: Int = 0,                    // 过期时间（秒）
    val candidates: List<LyricCandidate>? = null,    // 候选歌词列表
) : Parcelable

/**
 * 候选歌词项
 */
@Parcelize
data class LyricCandidate(
    val id: String? = null,                 // 歌词 ID（⭐ 用于获取歌词）
    val accesskey: String? = null,          // 访问密钥（⭐ 用于获取歌词）
) : Parcelable



/**
 * 歌词响应（/lyric 接口）
 */
@Parcelize
data class LyricResult(
    val info: String? = null,               // 状态信息，"OK"
    val fmt: String? = null,                // 歌词格式：lrc 或 krc
    val id: String? = null,                 // 歌词 ID
    val decodeContent: String? = null       // 解码后的明文歌词（需要 decode=true 时返回）
) : BaseResult(),Parcelable
