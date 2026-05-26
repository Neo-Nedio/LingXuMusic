package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌单详情数据
 * 对应 JSON 中 data 数组内的对象
 */
@Parcelize
data class PlaylistDetail(
    // ========== 基础信息 ==========
    val listid: Int = 0,                    // 歌单 ID
    val name: String? = null,               // 歌单名称
    val pic: String? = null,                // 封面图片 URL（含 {size} 占位符）
    val intro: String? = null,              // 歌单简介
    val tags: String? = null,               // 标签（逗号分隔，如 "ACG,国语,夜晚"）

    // ========== 数量相关 ==========
    val count: Int = 0,                     // 歌曲数量
    val collect_total: Int = 0,             // 收藏总数
    val heat: Int = 0,                      // 热度
    val number: Int = 0,                    // 编号

    // ========== 创建者信息 ==========
    val list_create_username: String? = null,   // 创建者昵称
    val list_create_userid: Long = 0,           // 创建者 ID
    val create_user_pic: String? = null,        // 创建者头像
    val create_user_gender: Int = 0,            // 创建者性别（0=未知,1=男,2=女）

    // ========== 时间相关 ==========
    val create_time: Long = 0,              // 创建时间戳
    val update_time: Long = 0,              // 更新时间戳
    val publish_date: String? = null,       // 发布日期（如 "2020-10-18"）

    // ========== 状态标志 ==========
    val status: Int = 0,                    // 状态（1=正常）
    val code: Int = 0,                      // 状态码（1=正常）
    val is_def: Int = 0,                    // 是否默认歌单
    val is_pri: Int = 0,                    // 是否私有
    val is_per: Int = 0,                    // 是否个人
    val is_mine: Int = 0,                   // 是否是我的
    val is_publish: Int = 0,                // 是否已发布
    val is_drop: Int = 0,                   // 是否丢弃
    val is_edit: Int = 0,                   // 是否可编辑
    val is_featured: Int = 0,               // 是否精选
    val is_custom_pic: Int = 0,             // 是否自定义封面

    // ========== 来源相关 ==========
    val source: Int = 0,                    // 来源（1=用户创建,2=收藏）
    val type: Int = 0,                      // 类型（0=普通,1=特色）
    val pub_type: Int = 0,                  // 发布类型
    val pub_new: Int = 0,                   // 是否新发布

    // ========== 标签相关 ==========
    val musiclib_tags: List<MusicTag>? = null,  // 标签对象列表
    val musiclib_id: Int = 0,               // 音乐库 ID

    // ========== ID 相关 ==========
    val list_create_listid: Int = 0,        // 创建者歌单 ID
    val radio_id: Int = 0,                  // 电台 ID
    val global_collection_id: String? = null,       // 全局收藏 ID
    val parent_global_collection_id: String? = null, // 父级全局收藏 ID
    val list_create_gid: String? = null,    // 创建者全局 ID

    // ========== 其他 ==========
    val trans_param: TransParam? = null,    // 转换参数
    val list_ver: Int = 0,                  // 版本号
    val sound_quality: String? = null,      // 音质
    val kq_talent: Int = 0,                 // 酷狗才艺值
    val per_num: Int = 0,                   // 权限数
    val per_count: Int = 0,                 // 权限计数
    val sort: Int = 0                       // 排序
) : Parcelable

/**
 * 音乐标签
 * 对应 JSON 中 musiclib_tags 数组内的对象
 */
@Parcelize
data class MusicTag(
    val tag_id: Int = 0,        // 标签 ID
    val parent_id: Int = 0,     // 父级标签 ID
    val tag_name: String? = null // 标签名称（如 "ACG"、"国语"）
) : Parcelable

/**
 * 转换参数
 * 对应 JSON 中 trans_param 对象
 */
@Parcelize
data class TransParam(
    val iden: Int = 0           // 标识
) : Parcelable