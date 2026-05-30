package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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
    val current_page: Int = 1  ,             // 当前页码
    val hot_word_list: List<HotWordItem>? = null  //  新增：热词列表
) : Parcelable

/**
 * 楼中楼评论列表响应（复用 SongCommentItem）
 */
@Parcelize
data class FloorCommentResult(
    val status: Int = 0,
    val err_code: Int = 0,
    val msg: String? = null,
    val childrenid: String? = null,         // 子评论ID
    val tid: String? = null,                // 父评论ID
    val comments_num: Int = 0,              // 总回复数
    val current_page: Int = 1,
    val list: List<SongCommentItem>? = null, //  复用 SongCommentItem
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
    val images: @RawValue Any? = null,      // 评论图片（可能为数组或 ""）

    //楼中楼专用
    val pid: Long = 0,                      // 被回复的评论ID
    val puser_id: String? = null,           // 被回复的用户ID（用于显示"回复 @XXX"）
    val is_reply: Int = 0,                 // 是否为回复
    val special_child_id: String? = null    // 用于楼中楼接口
) : Parcelable {

    //获取图片
    fun getImages(): List<CommentImage> = images.toCommentImages()
    private fun Any?.toCommentImages(): List<CommentImage> = when (this) {
        null, is String -> emptyList()
        is List<*> -> mapNotNull { it.toCommentImage() }
        else -> emptyList()
    }
    private fun Any?.toCommentImage(): CommentImage? = when (this) {
        is CommentImage -> this
        is Map<*, *> -> CommentImage(
            url = this["url"] as? String,
            width = this["width"].toIntValue(),
            height = this["height"].toIntValue()
        )
        else -> null
    }
    private fun Any?.toIntValue(): Int = when (this) {
        is Int -> this
        is Number -> toInt()
        is String -> toIntOrNull() ?: 0
        else -> 0
    }
}

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

/**
 * 热词项
 */
@Parcelize
data class HotWordItem(
    val content: String = "",               // 热词内容
    val count: Int = 0                      // 出现次数
) : Parcelable

