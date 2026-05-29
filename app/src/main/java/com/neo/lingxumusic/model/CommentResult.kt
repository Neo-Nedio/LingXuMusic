package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 评论列表响应
 */
@Parcelize
data class SongCommentResult(
    val status: Int = 0,                    // 状态码
    val err_code: Int = 0,                  // 错误码
    val msg: String? = null,                // 消息
    val count: Int = 0,                     // 评论总数
    val list: List<SongCommentItem>? = null,    // 评论列表
    val current_page: Int = 1               // 当前页码
) : Parcelable

/**
 * 单条评论
 */
@Parcelize
data class SongCommentItem(
    val id: Long = 0,                       // 评论 ID
    val content: String? = null,            // 评论内容
    val addtime: String? = null,            // 评论时间
    val reply_num: Int = 0,                 // 回复数量（可选）
    val user_id: Long = 0,                  // 用户 ID
    val user_name: String? = null,          // 用户昵称
    val user_pic: String? = null,           // 用户头像
    val user_sex: Int = 0,                  // 用户性别
    val like: LikeInfo? = null,             // 点赞信息
    val images: List<CommentImage>? = null  // 新增：评论图片列表v
) : Parcelable

/**
 * 点赞信息
 */
@Parcelize
data class LikeInfo(
    val count: Int = 0,                     // 点赞数
    val haslike: Boolean = false            // 是否已点赞
) : Parcelable

/**
 * 评论图片信息
 */
@Parcelize
data class CommentImage(
    val url: String? = null,                // 图片 URL
    val width: Int = 0,                     // 图片宽度（可选）
    val height: Int = 0                     // 图片高度（可选）
) : Parcelable
