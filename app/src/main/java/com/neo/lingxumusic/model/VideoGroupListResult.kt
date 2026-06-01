package com.neo.lingxumusic.model

import android.os.Parcelable
import com.neo.lingxumusic.utils.replaceSize
import kotlinx.parcelize.Parcelize

/**
 * 刷刷数据
 */
@Parcelize
data class BrushData(
    val list: List<BrushVideo>? = null,
    val has_next: Int = 0,
    val next_page: Int = 0
) : Parcelable

/**
 * 视频条目（只保留需要的字段）
 */
@Parcelize
data class BrushVideo(
    val song: BrushSong? = null,           // 歌曲信息
    val hp_mv_info: HpMvInfo? = null,      // 视频信息
    val id: String? = null,                 // 视频ID
    val g_user: GUser? = null              // ✅ 新增：作者信息（包含头像）
) : Parcelable

/**
 * 作者信息
 */
@Parcelize
data class GUser(
    val userid: Long = 0,
    val nickname: String? = null,
    val avatar: String? = null             // 作者头像
) : Parcelable

/**
 * 歌曲信息
 */
@Parcelize
data class BrushSong(
    val songname: String? = null,          // 歌名
    val author_name: String? = null,       // 歌手
    val album_cover: String? = null        // 封面图
) : Parcelable

/**
 * MV信息
 */
@Parcelize
data class HpMvInfo(
    val play_info: PlayInfo? = null,       // 播放信息
    val mv_info: MvSimpleInfo? = null      // MV信息
) : Parcelable

/**
 * 播放信息
 */
@Parcelize
data class PlayInfo(
    val src_file: SrcFile? = null          // 视频文件
) : Parcelable

/**
 * 视频文件
 */
@Parcelize
data class SrcFile(
    val h264: VideoUrl? = null             // H264格式视频（一般用这个）
) : Parcelable

/**
 * 视频URL
 */
@Parcelize
data class VideoUrl(
    val play_url: String? = null           // 播放地址
) : Parcelable

/**
 * MV简要信息
 */
@Parcelize
data class MvSimpleInfo(
    val likes: Long = 0,                   // 点赞数
    val views: Long = 0,                    // 播放数
    val cover: String? = null,              // 封面图
    val video_list: List<MvVideo>? = null   // 视频片段列表
) : Parcelable

@Parcelize
data class MvVideo(
    val video_bss_img: String? = null,      // 封面图
    val video_id: String? = null
) : Parcelable

val BrushVideo.displayTitle: String
    get() = song?.songname.orEmpty()

val BrushVideo.displayAuthor: String
    get() = song?.author_name.orEmpty()

val BrushVideo.displayCover: String?
    get() = hp_mv_info?.mv_info?.video_list?.firstOrNull()?.video_bss_img?.replaceSize()
        ?: hp_mv_info?.mv_info?.cover?.replaceSize()
        ?: song?.album_cover?.replaceSize()

val BrushVideo.displayLikes: Long
    get() = hp_mv_info?.mv_info?.likes ?: 0

val BrushVideo.displayAuthorAvatar: String?
    get() = g_user?.avatar?.replaceSize()
