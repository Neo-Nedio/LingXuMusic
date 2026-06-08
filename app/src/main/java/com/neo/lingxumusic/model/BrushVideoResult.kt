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
) : Parcelable

/**
 * 视频条目（只保留需要的字段）
 */
@Parcelize
data class BrushVideo(
    val song: BrushSong? = null,           // 歌曲信息
    val hp_mv_info: HpMvInfo? = null,      // 视频信息
) : Parcelable

/**
 * 歌曲信息
 */
@Parcelize
data class BrushSong(
    val songname: String? = null,          // 歌名
    val author_name: String? = null,       // 歌手
    val author_cover: String? = null,      // 歌手头像
    val album_cover: String? = null,       // 封面图
) : Parcelable

/**
 * MV信息
 */
@Parcelize
data class HpMvInfo(
    val play_info: PlayInfo? = null,       // 播放信息
    val mv_info: MvSimpleInfo? = null,     // MV信息
) : Parcelable

/**
 * 播放信息
 */
@Parcelize
data class PlayInfo(
    val src_file: SrcFile? = null,         // 视频文件
) : Parcelable

/**
 * 视频文件
 */
@Parcelize
data class SrcFile(
    val h264: VideoUrl? = null,            // H264格式视频（一般用这个）
) : Parcelable

/**
 * 视频URL
 */
@Parcelize
data class VideoUrl(
    val play_url: String? = null,          // 播放地址
) : Parcelable

/**
 * MV简要信息
 */
@Parcelize
data class MvSimpleInfo(
    val cover: String? = null,             // 封面图
    val video_list: List<MvVideo>? = null, // 视频片段列表
    //刷刷接口没有宽高，其他类使用
    val video_width: Int = 0,              // 视频原始宽度
    val video_height: Int = 0,             // 视频原始高度
) : Parcelable

@Parcelize
data class MvVideo(
    val video_bss_img: String? = null,     // 封面图
) : Parcelable

val BrushVideo.displayTitle: String
    get() = song?.songname.orEmpty()

val BrushVideo.displayAuthor: String
    get() = song?.author_name.orEmpty()

val BrushVideo.displayCover: String?
    get() = hp_mv_info?.mv_info?.video_list?.firstOrNull()?.video_bss_img?.replaceSize()
        ?: hp_mv_info?.mv_info?.cover?.replaceSize()
        ?: song?.album_cover?.replaceSize()

val BrushVideo.displayAuthorAvatar: String?
    get() = song?.author_cover?.replaceSize()

val BrushVideo.displayPlayUrl: String?
    get() = hp_mv_info?.play_info?.src_file?.h264?.play_url
