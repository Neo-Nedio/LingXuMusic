package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌手歌曲接口返回数据
 */
@Parcelize
data class SingerSongResult(
    val data: List<SingerSongItem> ? = null
) : Parcelable

/**
 * 歌手歌曲项（只保留转换需要的字段）
 */
@Parcelize
data class SingerSongItem(
    val hash: String? = null,
    val audio_name: String? = null,        // 歌曲名
    val author_name: String? = null,       // 歌手名
    val album_id: Long = 0,                // 专辑ID
    val mixsongid: Long = 0,               // 混音ID（用于播放）
    val trans_param: SingerSongTransParam? = null,
    val privilege: Int = 0,
) : Parcelable

/**
 * 传输参数（只保留封面）
 */
@Parcelize
data class SingerSongTransParam(
    val union_cover: String? = null        // 封面URL
) : Parcelable

/**
 * 将 SingerSongItem 转换为 Song
 */
fun SingerSongItem.toSong(): Song {
    val coverUrl = trans_param?.union_cover?.replace("{size}", "480")

    val displayName = if (!author_name.isNullOrBlank() && !audio_name.isNullOrBlank()) {
        "${author_name} - ${audio_name}"
    } else {
        audio_name ?: author_name ?: "未知歌曲"
    }

    return Song(
        hash = hash,
        songname = audio_name,
        name = displayName,
        album_id = album_id.toString(),
        cover = coverUrl,
        mixsongid = mixsongid,
        privilege = privilege
    )
}

/**
 * 将 List<SingerSongItem> 转换为 List<Song>
 */
fun List<SingerSongItem>.toSongList(): List<Song> {
    return mapNotNull { it.toSong() }
}