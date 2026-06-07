package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 用户详细信息
 */
@Parcelize
data class UserDetail(
    // ========== 基本信息 ==========
    val nickname: String? = null,      // 用户昵称
    val gender: Int = 0,               // 性别: 0=未知, 1=男, 2=女
    val city: String? = null,          // 所在城市
    val province: String? = null,      // 所在省份
    val loc: String? = null,           // 当前位置（可能是实时定位）
    val birthday: String? = null,      // 生日
    val occupation: String? = null,    // 职业
    val hobby: String? = null,         // 爱好
    val descri: String? = null,        // 个人简介

    // ========== 头像/图片 ==========
    val pic: String? = null,           // 头像URL（酷狗）
    val k_pic: String? = null,         // 头像URL（酷狗备用）
    val fx_pic: String? = null,        // 头像URL（分享用）
    val bg_pic: String? = null,        // 背景图URL

    // ========== 统计数据 ==========
    val follows: Int = 0,              // 关注数
    val fans: Int = 0,                 // 粉丝数
    val friends: Int = 0,              // 好友数
    val visitors: Int = 0,             // 访客数
    val p_grade: Int = 0,              // 平台等级

    // ========== VIP信息 ==========
    val vip_type: Int = 0,             // VIP类型: 0=非会员
    val m_type: Int = 0,               // 会员类型
    val y_type: Int = 0,               // 音乐包类型
    val svip_level: Int = 0,           // 超级VIP等级
    val svip_score: Int = 0,           // 超级VIP积分
    val su_vip_begin_time: String? = null,  // VIP开始时间
    val su_vip_end_time: String? = null,    // VIP结束时间

    // ========== 时间戳 ==========
    val logintime: Long = 0,           // 最后登录时间戳
    val servertime: Long = 0,          // 服务器时间戳
    val create_time: Long = 0,         // 账号创建时间戳

    // ========== 其他 ==========
    val relation: Int = 0,             // 与当前用户关系（0=自己）
    val auth_info: String? = null,     // 认证信息
    val singer_status: Int = 0,        // 歌手状态: 0=不是歌手
    val star_status: Int = 0,          // 明星状态
    val actor_status: Int = 0,         // 演员状态
    val is_star: Int = -1,             // 是否是明星: -1=否
    val user_type: Int = 0,            // 用户类型
    val user_y_type: Int = 0,          // 用户音乐包类型

    // ========== 可见性设置 ==========
    val info_visible: Int = 1,         // 信息可见性
    val follow_visible: Int = 1,       // 关注列表可见性
    val fanslist_visible: Int = 1,     // 粉丝列表可见性
    val listen_visible: Int = 1,       // 听歌记录可见性
    val collectlist_visible: Int = 1   // 收藏列表可见性
) : Parcelable