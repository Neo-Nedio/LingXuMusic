package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌曲播放信息响应
 */
@Parcelize
data class SongPlayInfo(
    val extName: String? = null,           // 扩展名（如 mp3）
    val status: Int = 0,                   // 状态（1=正常）
    val volume: Double = 0.0,              // 音量
    val std_hash_time: Int = 0,            // 标准hash时间（毫秒）
    val backupUrl: List<String>? = null,   // 备用播放地址列表
    val url: List<String>? = null,         // 播放地址列表
    val std_hash: String? = null,          // 标准hash
    val tracker_through: TrackerThrough? = null, // 追踪信息
    val trans_param: TransParam? = null,   // 传输参数
    val fileHead: Int = 0,                 // 文件头
    val timeLength: Int = 0,               // 时长（秒）
    val bitRate: Int = 0,                  // 比特率（bps）
    val priv_status: Int = 0,              // 私有状态
    val volume_peak: Double = 0.0,         // 音量峰值
    val volume_gain: Int = 0,              // 音量增益
    val q: Int = 0,                        // 质量标识
    val fileName: String? = null,          // 文件名
    val fileSize: Int = 0,                 // 文件大小（字节）
    val hash: String? = null               // 文件hash
) : Parcelable

/**
 * 追踪信息
 */
@Parcelize
data class TrackerThrough(
    val identity_block: Int = 0,
    val cpy_grade: Int = 0,
    val musicpack_advance: Int = 0,
    val all_quality_free: Int = 0,
    val cpy_level: Int = 0
) : Parcelable

/**
 * 传输参数
 */
@Parcelize
data class SongPlayTransParam(
    val pay_block_tpl: Int = 0,
    val union_cover: String? = null,
    val language: String? = null,
    val qualitymap: Map<String, String>? = null,
    val cpy_attr0: Long = 0,
    val hash_multitrack: String? = null,
    val ipmap: Map<String, Long>? = null,
    val classmap: Map<String, Long>? = null,
    val display: Int = 0,
    val display_rate: Int = 0
) : Parcelable