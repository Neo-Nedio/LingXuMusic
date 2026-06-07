package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserFollowData(
    val total: Int = 0,                    // 关注总数
    val list_ver: Int = 0,
    val lists: List<UserFollow>? = null    // 关注列表
) : Parcelable

@Parcelize
data class UserFollow(
    val userid: Long = 0,                  // 用户/歌手 ID
    val nickname: String? = null,          // 昵称
    val pic: String? = null,               // 头像 URL
    val gender: Int = 0,                   // 性别: 0=未知,1=男,2=女
    val vip_type: Int = 0,                 // VIP类型
    val kq_talent: Int = 0,                // 酷狗达人等级
    val identity: Int = 0,                 // 身份: 15=歌手
    val addtime: Long = 0,                 // 关注时间戳
    val is_friend: Int = 0                 // 是否互关
) : Parcelable