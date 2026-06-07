package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 推荐数据
 */
@Parcelize
data class SongRecommendData(
    val card_id: Int = 0,                       // 推荐卡片类型
    val rec_desc: String? = null,               // 推荐描述，如"「精选好歌随心听」"
    val song_list: List<RecommendSong>? = null, // 歌曲列表
    val song_list_size: Int = 0                 // 歌曲数量
) : Parcelable

/**
 * 推荐歌曲信息
 */
@Parcelize
data class RecommendSong(
    val songname: String? = null,               // 歌名
    val author_name: String? = null,            // 歌手名
    val album_name: String? = null,             // 专辑名
    val album_id: String? = null,               //专辑id
    val sizable_cover: String? = null,          // 封面图
    val album_sizable_cover: String? = null,    // 封面图（部分接口直接返回）
    val hash: String? = null,                   // 音频哈希（获取播放URL）
    val mixsongid: String? = null,              // 歌曲ID
    val album_audio_id: String? = null,         // 专辑音频ID
    val time_length: Double = 0.0,              // 时长（秒）
    val publish_date: String? = null,           // 发行日期
    val language: String? = null,               // 语言
    val trans_param: RecommendTransParam? = null, // 猜你喜欢封面在 union_cover
    val info: RecommendSongInfo? = null,         // 猜你喜欢封面在 image
    val privilege:Int = 0 //判断是否需要vip
) : Parcelable

@Parcelize
data class RecommendTransParam(
    val union_cover: String? = null
) : Parcelable

@Parcelize
data class RecommendSongInfo(
    val image: String? = null
) : Parcelable

fun RecommendSong.displayTitle(): String {
    val name = songname.orEmpty()
    val author = author_name.orEmpty()
    return when {
        name.isNotBlank() && author.isNotBlank() -> "$name - $author"
        name.isNotBlank() -> name
        else -> author
    }
}

fun RecommendSong.coverUrl(): String? {
    return album_sizable_cover?.takeIf { it.isNotBlank() }
        ?: sizable_cover?.takeIf { it.isNotBlank() }
        ?: trans_param?.union_cover?.takeIf { it.isNotBlank() }
        ?: info?.image?.takeIf { it.isNotBlank() }
}

/**
 * 将推荐歌曲转换为 Song
 */
fun RecommendSong.toSong(): Song {
    val songName = songname.orEmpty()
    val author = author_name.orEmpty()
    return Song(
        hash = hash,
        songname = songname,
        name = when {
            songName.isNotBlank() && author.isNotBlank() -> "$author - $songName"
            songName.isNotBlank() -> songName
            else -> author
        },
        album_id = album_id,
        cover = coverUrl(),
        mixsongid = mixsongid?.toLongOrNull() ?: album_audio_id?.toLongOrNull() ?: 0,
        privilege = privilege
    )
}

/**
 * 批量转换
 */
fun List<RecommendSong>.toSongList(): List<Song> {
    return map { it.toSong() }
}
