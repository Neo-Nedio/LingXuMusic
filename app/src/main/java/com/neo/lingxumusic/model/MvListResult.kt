package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * MV列表响应
 * 对应接口: /mv/list
 */
@Parcelize
data class MvListResult(
    val status: Int = 0,                    // 状态码，1表示成功
    val error_code: Int = 0,                // 错误码
    val errcode: Int = 0,                   // 错误码
    val errmsg: String? = null,             // 错误信息
    val total: Int = 0,                     // 总记录数
    val data: List<MvInfo>? = null,         // MV数据列表
    val extra: MvListExtra? = null          // 额外信息
) : Parcelable

/**
 * MV列表分页信息
 */
@Parcelize
data class MvListExtra(
    val page_total: Int = 0                 // 总页数
) : Parcelable

/**
 * MV详细信息
 */
@Parcelize
data class MvInfo(
    val audio_bitrate: Int = 0,             // 音频比特率
    val publish_date: String? = null,       // 发布日期
    val mkv_sd_hash: String? = null,        // MKV标清版hash
    val track: Int = 0,                     // 音轨
    val album_audio_id: Long = 0,           // 专辑音频ID
    val mkv_qhd_hash: String? = null,       // MKV高清版hash
    val hdpic: String? = null,              // 高清图片URL
    val history_heat: Int = 0,              // 历史热度
    val heat: Int = 0,                      // 当前热度
    val user_name: String? = null,          // 用户名
    val audio_filesize: Long = 0,           // 音频文件大小
    val cover: String? = null,              // 封面图URL
    val user_id: String? = null,            // 用户ID
    val topic: String? = null,              // 主题
    val remark: String? = null,             // 备注
    val extern: String? = null,             // 扩展字段
    val timelength: Int = 0,                // 视频时长(毫秒)
    val audio_hash: String? = null,         // 音频hash
    val pic: String? = null,                // 图片文件名
    val mkv_sd_filesize: String? = null,    // MKV标清版文件大小
    val video_name: String? = null,         // 视频名称
    val video_id: Long = 0,                 // 视频ID
    val intro: String? = null,              // 简介
    val audio_timelength: String? = null,   // 音频时长
    val author_name: String? = null,        // 作者名称
    val h264: H264Info? = null,             // H264编码各清晰度信息
    val type: Int = 0,                      // 类型
    val is_short: Int = 0                   // 是否为短视频 0:否 4:是
) : Parcelable

/**
 * H264编码信息
 */
@Parcelize
data class H264Info(
    val ld_hash: String? = null,            // 流畅版hash
    val ld_filesize: Long = 0,              // 流畅版文件大小
    val ld_bitrate: Long = 0,               // 流畅版比特率
    val sd_hash: String? = null,            // 标清版hash
    val sd_filesize: Long = 0,              // 标清版文件大小
    val sd_bitrate: Long = 0,               // 标清版比特率
    val qhd_hash: String? = null,           // 高清版hash
    val qhd_filesize: Long = 0,             // 高清版文件大小
    val qhd_bitrate: Long = 0,              // 高清版比特率
    val hd_hash: String? = null,            // 超清版hash
    val hd_filesize: Long = 0,              // 超清版文件大小
    val hd_bitrate: Long = 0,               // 超清版比特率
    val fhd_hash: String? = null,           // 全高清版hash
    val fhd_filesize: Long = 0,             // 全高清版文件大小
    val fhd_bitrate: Long = 0               // 全高清版比特率
) : Parcelable