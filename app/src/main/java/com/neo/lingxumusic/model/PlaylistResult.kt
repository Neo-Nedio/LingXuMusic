package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

// 歌单数据
@Parcelize
data class PlaylistData(
    val info: List<Playlist>? = null,   // 歌单列表（包括我的+收藏的）
) : Parcelable

/**
 * 歌单实体类
 */
@Parcelize
data class Playlist(
    val name: String? = null,                   // 歌单名称
    val is_def: Int = 1,  // 1=默认收藏, 2=我喜欢, 0=普通歌单
    val type: Int = 1,    // 0=用户创建, 1=用户收藏
    val intro: String? = null,                  // 歌单简介
    val pic: String? = null,                    // 封面图片URL
    val count: @RawValue Any = 0,              // 歌曲数
    val list_create_username: String? = null,   // 创建者昵称
    val list_create_userid: Long = 0,           // 创建者ID
    val list_create_listid: Int = 0,    // 歌单ID（用于收藏）
    val create_user_pic: String? = null,        // 创建者头像
    val global_collection_id: String? = null,  // 全局收藏ID
    val listid: Int = 0, //歌单id（用于请求歌曲列表）
    val list_create_gid: String? = null,  // 原歌单全局ID（用于获取原歌单的歌曲列表）
) : Parcelable {

    fun getCount(): Int = count.toIntValue()

    private fun Any?.toIntValue(): Int = when (this) {
        is Int -> this
        is Number -> toInt()
        is String -> toIntOrNull() ?: 0
        else -> 0
    }
}

/**
 * 歌单详情页需要的歌单简要信息
 */
@Parcelize
data class PlaylistBrief(
    val global_collection_id: String? = null, // 全局收藏ID（用于请求歌曲列表）
    val name: String? = null,                 // 歌单名称
    val is_def: Int = 1,  // 1=收藏, 2=我喜欢, 0=创建
    val type: Int = 0,    // 0=用户创建, 1=用户收藏
    val intro: String? = null,                // 歌单简介
    val pic: String? = null,                  // 封面图片URL
    val count: Int = 0,                       // 歌曲数
    val list_create_username: String? = null, // 创建者昵称
    val create_user_pic: String? = null,       // 创建者头像
    val listid: Int = 0, //歌单id（用于请求歌曲列表）
    val list_create_userid: Long = 0,   // 创建者ID（用于收藏）
    val list_create_listid: Int = 0,    // 歌单ID（用于收藏）
    val list_create_gid: String? = null,  // 原歌单全局ID（用于获取原歌单的歌曲列表）
) : Parcelable

fun Playlist.toBrief(): PlaylistBrief {
    return PlaylistBrief(
        global_collection_id = global_collection_id,
        listid = listid, //歌单id（用于请求歌曲列表）
        name = name,
        intro = intro,
        pic = pic,
        count = getCount(),
        list_create_username = list_create_username,
        create_user_pic = create_user_pic,
        list_create_userid = list_create_userid,
        list_create_listid = list_create_listid,
        list_create_gid = list_create_gid,
        is_def = is_def,
        type = type
    )
}

/**
 * 音乐标签
 */
@Parcelize
data class MusicTag(
    val tag_id: Int = 0,        // 标签ID
    val parent_id: Int = 0,     // 父标签ID
    val tag_name: String? = null // 标签名（如"ACG"、"国语"）
) : Parcelable

/**
 * 转换参数
 */
@Parcelize
data class TransParam(
    val iden: Int = 0           // 标识
) : Parcelable
