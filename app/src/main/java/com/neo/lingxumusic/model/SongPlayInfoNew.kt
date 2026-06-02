package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongPlayInfoNew(
    val status: Int = 0,
    val error_code: Int = 0,
    val message: String? = null,
    val data: List<SongItem>? = null
) : Parcelable

@Parcelize
data class SongItem(
    val hash: String? = null,
    val album_audio_id: Long = 0,
    val status: Int = 0,           // 0=正常
    val privilege: Int = 0,        // 权限
    val pay_type: Int = 0,         // 付费类型: 3=需要购买
    val price: Int = 0,            // 价格(酷币)
    val pkg_price: Int = 1,        // 套餐价格
    val name: String? = null,      // 歌曲名
    val singername: String? = null, // 歌手名
    val album_id: String? = null,
    val albumname: String? = null,
    val quality: String? = null,   // 音质: 128/320/flac/high
    val info: SongInfo? = null,
    val relate_goods: List<SongItem>? = null  // 不同音质的版本
) : Parcelable

@Parcelize
data class SongInfo(
    val duration: Int = 0,          // 时长(毫秒)
    val bitrate: Int = 0,           // 比特率
    val filesize: Int = 0,          // 文件大小
    val extname: String? = null,    // 扩展名 mp3/flac
    val image: String? = null,      // 封面图
    val tracker_url: List<String>? = null,  // 播放URL ⭐
    val tracker_status: Int = 0,    // 1=可用,2=不可用
    val tracker_type: String? = null, // part=片段, none=无
    val climax_info: ClimaxInfo? = null, // 高潮部分
    val volume: String? = null,
    val volume_peak: String? = null,
    val volume_gain: String? = null
) : Parcelable

@Parcelize
data class ClimaxInfo(
    val url: List<String>? = null,
    val hash_offset: HashOffset? = null
) : Parcelable

@Parcelize
data class HashOffset(
    val start_byte: Int = 0,
    val end_byte: Int = 0,
    val start_ms: Int = 0,
    val end_ms: Int = 0,
    val offset_hash: String? = null,
    val file_type: Int = 0,
    val clip_hash: String? = null
) : Parcelable