package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** 歌单详情数据 */
@Parcelize
data class PlaylistDetailData(
    val songs: List<Song>? = null,
) : Parcelable

/** 歌曲信息 */
@Parcelize
data class Song(
    val hash: String? = null,
    val songname: String? = null,
    val name: String? = null,
    val album_id: String? = null,
    val cover: String? = null,
    val mixsongid: Long = 0,
    val fileid: Int = 0,           // 用于歌单内删除歌曲删除歌曲（非歌单不用管）
    val privilege: Int = 0,
) : Parcelable

//构建添加歌单需要的data
fun Song.buildData(): String? {
    val songname = songname?.takeIf { it.isNotBlank() }
        ?: name?.substringAfter(" - ", missingDelimiterValue = name)
            ?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
    val hash = hash?.takeIf { it.isNotBlank() } ?: return null
    if (songname.isNullOrBlank()) {
        return null
    }
    val base = "$songname|$hash"
    val albumId = album_id?.takeIf { it.isNotBlank() }
    val mixsongid = mixsongid.takeIf { it > 0 }
    return when {
        albumId != null && mixsongid != null -> "$base|$albumId|$mixsongid"
        albumId != null -> "$base|$albumId"
        else -> base
    }
}
