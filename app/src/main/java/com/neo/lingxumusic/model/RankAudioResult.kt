package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * 排行榜歌曲数据
 */
@Parcelize
data class RankAudioData(
    val total: Int = 0,                 // 歌曲总数
    val songlist: List<RankAudioSong>? = null  // 歌曲列表
) : Parcelable

/**
 * 排行榜歌曲信息
 */
@Parcelize
data class RankAudioSong(
    // ========== 基本信息 ==========
    val songname: String? = null,       // 歌曲名
    val author_name: String? = null,    // 歌手名
    val album_audio_id: Long = 0,       // 歌曲ID（用于获取播放URL）
    val album_id: Long = 0,             // 专辑ID

    // ========== 封面图 ==========
    val album_info: AlbumInfo? = null,  // 专辑信息（含封面）

    // ========== 音频信息 ==========
    val audio_info: AudioInfo? = null,  // 各音质信息

    // ========== 付费信息 ==========
    val deprecated: DeprecatedInfo? = null  // 付费信息
) : Parcelable

/**
 * 专辑信息
 */
@Parcelize
data class AlbumInfo(
    val album_name: String? = null,     // 专辑名
    val sizable_cover: String? = null   // 专辑封面URL（需替换{size}）
) : Parcelable

/**
 * 音频信息（各音质）
 */
@Parcelize
data class AudioInfo(
    // 128k
    val bitrate: Int = 0,               // 比特率
    val duration_128: Int = 0,          // 时长（毫秒）
    val filesize_128: Int = 0,          // 文件大小（字节）
    val hash_128: String? = null,       // 128k hash
    val extname: String? = null,        // 扩展名，如 "mp3"

    // 320k
    val duration_320: Int = 0,
    val filesize_320: Int = 0,
    val hash_320: String? = null,

    // flac
    val duration_flac: Int = 0,
    val filesize_flac: Int = 0,
    val hash_flac: String? = null
) : Parcelable

/**
 * 付费信息
 */
@Parcelize
data class DeprecatedInfo(
    val pay_type: Int = 0,              // 付费类型: 0=免费, 1=试听, 3=付费
    val price: Int = 0,                 // 价格（酷币）
    val pkg_price: Int = 1              // 套餐价格
) : Parcelable



fun RankAudioSong.toSong(): Song {
    return Song(
        hash = audio_info?.hash_128,
        songname = songname,
        name = "$author_name - $songname",
        album_id = album_id.toString(),
        cover = album_info?.sizable_cover,
    )
}



