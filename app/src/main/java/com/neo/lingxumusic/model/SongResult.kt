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
) : Parcelable
