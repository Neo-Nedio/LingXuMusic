package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


/**
 * 场景推荐数据
 */
@Parcelize
data class SceneRecommendData(
    val has_next: Int = 0,                      // 是否有下一页
    val session: String? = null,                // 会话ID
    val special_list: List<ScenePlaylist>? = null, // 歌单列表
    val show_time: Int = 0,                     // 展示时间（秒）
    val refresh_time: Int = 0                   // 刷新时间（秒）
) : Parcelable

/**
 * 场景歌单信息
 */
@Parcelize
data class ScenePlaylist(
    // ========== 基础信息 ==========
    val specialid: Int = 0,                     // 歌单ID
    val specialname: String? = null,            // 歌单名称
    val intro: String? = null,                  // 歌单简介
    val imgurl: String? = null,                 // 封面图（可替换size）
    val flexible_cover: String? = null,         // 备用封面图

    // ========== 统计信息 ==========
    val play_count: Long = 0,                   // 播放次数
    val collectcount: Int = 0,                  // 收藏数
    val songcount: @RawValue Any = 0,           // 歌曲数量（接口可能为数字或字符串）

    // ========== 创建者信息 ==========
    val nickname: String? = null,               // 创建者昵称
    val pic: String? = null,                    // 创建者头像
    val suid: Long = 0,                         // 创建者ID

    // ========== 标签 ==========
    val tags: List<SceneTag>? = null,           // 标签列表
    val abtags: List<SceneTag>? = null,         // 标签列表（另一种格式）

    // ========== 其他 ==========
    val type: Int = 0,                          // 类型（3=普通歌单，6=精选歌单）
    val slid: Int = 0,                          // 滑动ID
    val publishtime: String? = null,            // 发布时间
    val global_collection_id: String? = null,   // 全局收藏ID
    val songs: List<SceneSong>? = null,         // 歌曲列表（部分接口会返回）

    // ========== 自定义显示字段 ==========
    val show: String? = null                    // 推荐标签如"热门精选"
) : Parcelable

/**
 * 场景标签
 */
@Parcelize
data class SceneTag(
    val tag_id: Int = 0,
    val tag_name: String? = null
) : Parcelable

/**
 * 场景歌单中的歌曲（部分接口会返回）
 */
@Parcelize
data class SceneSong(
    val hash: String? = null,                   // 音频哈希
    val songname: String? = null,               // 歌名
    val filename: String? = null,               // 文件名（含歌手）
    val duration: Int = 0,                      // 时长（秒）
    val album_id: Long = 0,                     // 专辑ID
    val album_audio_id: String? = null,         // 专辑音频ID
    val bitrate: Int = 0,                       // 码率
    val filesize: Int = 0,                      // 文件大小
    val privilege: Int = 0,                     // 权限
    val pay_type: Int = 0                       // 付费类型
) : Parcelable






/**
 * 将场景歌单 ScenePlaylist 转换为通用 Playlist
 */
private fun Any?.toIntValue(): Int = when (this) {
    is Int -> this
    is Number -> toInt()
    is String -> toIntOrNull() ?: 0
    else -> 0
}

fun ScenePlaylist.songCountValue(): Int {
    return songcount.toIntValue().takeIf { it > 0 } ?: songs?.size ?: 0
}

fun ScenePlaylist.toPlaylist(): Playlist {
    return Playlist(
        name = specialname,
        intro = intro,
        pic = flexible_cover ?: imgurl,
        count = songCountValue(),
        list_create_username = nickname,
        list_create_userid = suid,
        create_user_pic = pic,
        global_collection_id = global_collection_id,
        listid = specialid,
        list_create_listid = specialid
    )
}

/**
 * 批量转换
 */
fun List<ScenePlaylist>.toPlaylistList(): List<Playlist> {
    return map { it.toPlaylist() }
}
