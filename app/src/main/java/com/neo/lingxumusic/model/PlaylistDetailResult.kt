package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

// 歌单响应
@Parcelize
class PlaylistResult(
    val data: PlaylistData? = null
) : BaseResult(), Parcelable

// 歌单数据
@Parcelize
data class PlaylistData(
    val info: List<Playlist>? = null,   // 歌单列表（包括我的+收藏的）
    val list_count: Int = 0,            // 歌单总数
    val collect_count: Int = 0,         // 收藏的歌单数
    val userid: Long = 0,               // 当前用户ID
    val album_count: Int = 0,           // 专辑数
    val phone_flag: Int = 0,            // 手机标识
    val total_ver: Int = 0              // 总版本
) : Parcelable

/**
 * 歌单实体类
 * @property source 来源：1=我创建的，2=我收藏的
 * @property listid 歌单ID（1=默认收藏，2=我喜欢）
 * @property name 歌单名称
 * @property count 歌曲数量（可能为Int或String）
 * @property list_create_username 创建者名称
 * @property list_create_userid 创建者ID
 */
@Parcelize
data class Playlist(
    // ========== 基础信息 ==========
    val listid: Int = 0,                // 歌单ID（1=默认收藏，2=我喜欢）
    val name: String? = null,           // 歌单名称
    val intro: String? = null,          // 歌单简介
    val pic: String? = null,            // 封面图片URL

    // ========== 数量相关 ==========
    val count: @RawValue Any = 0,                // 歌曲数
    val m_count: @RawValue Any = 0,             // 歌曲数

    // ========== 来源相关 ==========
    val source: Int = 0,                // ⭐ 1=我创建的歌单，2=我收藏的歌单
    val type: Int = 0,                  // 类型：0=普通，1=特色

    // ========== 创建者信息 ==========
    val list_create_username: String? = null,   // 创建者昵称
    val list_create_userid: Long = 0,           // 创建者ID
    val create_user_pic: String? = null,        // 创建者头像

    // ========== 时间相关 ==========
    val create_time: Long = 0,          // 创建时间戳
    val update_time: Long = 0,          // 更新时间戳

    // ========== 状态标志 ==========
    val status: Int = 0,                // 状态：1=正常
    val is_del: Int = 0,                // 是否删除：0=否
    val is_publish: Int = 0,            // 是否发布：1=已发布
    val is_featured: Int = 0,           // 是否精选：1=精选
    val is_mine: Int = 0,               // 是否是我的：0=否
    val is_pri: Int = 0,                // 是否私有：0=否
    val is_per: Int = 0,                // 是否个人：0=否

    // ========== 标签相关 ==========
    val tags: String? = null,           // 标签字符串（逗号分隔）
    val musiclib_tags: List<MusicTag>? = null,  // 标签对象列表

    // ========== 其他 ==========
    val sort: Int = 0,                  // 排序
    val list_ver: Int = 0,              // 版本号
    val kq_talent: Int = 0,             // 酷狗才艺值
    val trans_param: TransParam? = null, // 转换参数
    val global_collection_id: String? = null,  // 全局收藏ID
    val list_create_gid: String? = null,       // 创建者全局ID

    // ========== 以下为收藏歌单特有 ==========
    val authors: List<Author>? = null,  // 作者列表（收藏的音乐人专辑）
    val from_listid: Int = 0,           // 来源歌单ID
    val musiclib_id: Int = 0,           // 音乐库ID
    val cutd: Int = 0,                  // 切割标识

    // ========== 以下为默认值，不需要关注 ==========
    val per_num: Int = 0,               // 权限数
    val per_count: Int = 0,             // 权限计数
    val pub_new: Int = 0,               // 发布新
    val pub_time: Long = 0,             // 发布时间
    val pub_type: Int = 0,              // 发布类型
    val incr_sync: Int = 0,             // 增量同步
    val radio_id: Int = 0,              // 电台ID
    val is_drop: Int = 0,               // 是否丢弃
    val is_edit: Int = 0,               // 是否可编辑
    val is_def: Int = 0,                // 是否默认
    val sound_quality: String? = null,  // 音质
    val is_custom_pic: Int = 0,         // 是否自定义封面
    val list_create_listid: Int = 0     // 创建列表ID
) : Parcelable {

    fun getCount(): Int = count.toIntValue()

    fun getMCount(): Int = m_count.toIntValue()

    private fun Any?.toIntValue(): Int = when (this) {
        is Int -> this
        is Number -> toInt()
        is String -> toIntOrNull() ?: 0
        else -> 0
    }
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

/**
 * 作者信息（收藏的歌单/专辑使用）
 */
@Parcelize
data class Author(
    val author_name: String? = null,   // 作者名（如"HOYO-MiX"）
    val author_id: Int = 0             // 作者ID
) : Parcelable