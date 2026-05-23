package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 登录响应
 * @param data 用户登录数据
 */
@Parcelize
class LoginResult(
    val data: LoginData? = null
) : BaseResult(), Parcelable

/**
 * 登录数据（用户信息）
 */
@Parcelize
data class LoginData(
    // ========== VIP信息 ==========
    val is_vip: Int,            // 是否VIP：0=否，1=是
    val vip_type: Int,          // VIP类型
    val vip_end_time: String,   // VIP结束时间
    val vip_begin_time: String, // VIP开始时间
    val vip_token: String,      // VIP凭证

    // ========== 用户基本信息 ==========
    val userid: Long,           // 用户ID（⭐ 重要）
    val username: String,       // 用户名
    val nickname: String,       // 昵称
    val pic: String,            // 头像URL
    val sex: Int,               // 性别：0=未知，1=男，2=女
    val birthday: String,       // 生日
    val birthday_mmdd: String,  // 生日（月日）

    // ========== Token信息 ==========
    val token: String,          // 登录凭证（⭐ 重要，后续请求需携带）
    val t1: String,             // 加密参数1
    val secu_params: String,    // 安全参数
    val totp_server_timestamp: Long,  // 服务器时间戳

    // ========== 时间信息 ==========
    val servertime: String,     // 服务器时间
    val reg_time: String,       // 注册时间
    val t_expire_time: Long,    // Token过期时间

    // ========== 账号绑定信息 ==========
    val mobile: Int,            // 是否绑定手机：1=已绑定
    val qq: Int,                // 是否绑定QQ：1=已绑定
    val wechat: Int,            // 是否绑定微信：1=已绑定

    // ========== 经验/积分 ==========
    val exp: Int,               // 经验值
    val score: Int,             // 积分

    // ========== 用户类型 ==========
    val user_type: Int,         // 用户类型
    val user_y_type: Int,       // 用户Y类型
    val y_type: Int,            // Y类型
    val m_type: Int,            // M类型
    val roam_type: Int,         // 漫游类型

    // ========== 会员相关 ==========
    val su_vip_end_time: String,   // 超级VIP结束时间
    val su_vip_y_endtime: String,  // 超级VIP年费结束时间
    val su_vip_begin_time: String, // 超级VIP开始时间
    val su_vip_clearday: String,   // 超级VIP清理天数
    val listen_type: Int,          // 试听类型
    val listen_end_time: String,   // 试听结束时间
    val listen_begin_time: String, // 试听开始时间

    // ========== 漫游相关 ==========
    val roam_end_time: String,     // 漫游结束时间
    val roam_begin_time: String,   // 漫游开始时间

    // ========== 时间相关（空字符串） ==========
    val m_end_time: String,        // M结束时间
    val m_begin_time: String,      // M开始时间

    // ========== 其他 ==========
    val bookvip_valid: Int,        // 听书VIP是否有效
    val bookvip_end_time: String,  // 听书VIP结束时间
    val arttoy_avatar: String,     // 艺术玩具头像
    val bc_code: String,           // BC码
    val m_is_old: Int              // M是否旧版
) : Parcelable