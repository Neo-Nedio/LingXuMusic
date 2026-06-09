package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AlbumSongsData(
    val songs: List<AlbumSong>? = null
) : Parcelable

@Parcelize
data class AlbumSong(
    val base: AlbumSongBase? = null,
    val album_info: AlbumSongAlbumInfo? = null,
    val audio_info: AlbumSongAudioInfo? = null,
    val copyright: AlbumSongCopyright? = null
) : Parcelable

@Parcelize
data class AlbumSongBase(
    val album_id: Long = 0,
    val author_name: String? = null,
    val audio_name: String? = null,
    val album_audio_id: Long = 0
) : Parcelable

@Parcelize
data class AlbumSongAlbumInfo(
    val cover: String? = null
) : Parcelable

@Parcelize
data class AlbumSongAudioInfo(
    val hash_128: String? = null,
    val hash: String? = null
) : Parcelable

@Parcelize
data class AlbumSongCopyright(
    val privilege_128: Int = 0
) : Parcelable

fun AlbumSong.toSong(): Song {
    return Song(
        hash = audio_info?.hash_128 ?: audio_info?.hash,
        songname = base?.audio_name,
        name = "${base?.author_name} - ${base?.audio_name}",
        album_id = base?.album_id.toString(),
        cover = album_info?.cover,
        mixsongid = base?.album_audio_id ?: 0,
        fileid = 0,
        privilege = copyright?.privilege_128 ?: 0
    )
}

fun List<AlbumSong>.toSongList(): List<Song> {
    return map { it.toSong() }
}