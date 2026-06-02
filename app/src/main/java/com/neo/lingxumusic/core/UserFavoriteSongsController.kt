package com.neo.lingxumusic.core

import androidx.compose.runtime.mutableStateListOf
import com.neo.lingxumusic.hilt.entrypoint.EntryPointFinder
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UserFavoriteSongsController {

    var favoriteSongList = mutableStateListOf<Song>() // 我喜欢的歌曲

    private val userApi: UserApi = EntryPointFinder.getUserApi()
    private val songApi: SongApi = EntryPointFinder.getSongApi()
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadFavoriteSongJob: Job? = null

    // 加载我喜欢的歌曲（在进入应用时就调用）
    fun loadFavoriteSongs() {
        val playlistId = AppGlobalData.favoritePlaylistGlobalCollectionId
        if (playlistId.isNullOrBlank()) {
            return
        }
        loadFavoriteSongJob?.cancel()
        loadFavoriteSongJob = controllerScope.launch {
            try {
                val songs = withContext(Dispatchers.IO) {
                    userApi.getPlaylistSong(playlistId)
                        .dataAs<PlaylistDetailData>()
                        ?.songs
                        .orEmpty()
                }
                favoriteSongList.clear()
                favoriteSongList.addAll(songs)
            } catch (_: Exception) {
            }
        }
    }

    // 判断某个歌曲是否在我喜欢的歌曲列表中
    fun isFavoriteSong(song: Song): Boolean {
        val hash = song.hash
        if (!hash.isNullOrBlank()) {
            return favoriteSongList.any { it.hash == hash }
        }
        return song.mixsongid > 0 && favoriteSongList.any { it.mixsongid == song.mixsongid }
    }

    // 添加歌曲到我喜欢的歌单
    fun addFavoriteSong(song: Song) {
        val listid = AppGlobalData.favoritePlaylistListId
        val data = buildData(song)
        if (listid <= 0 || data.isNullOrBlank()) {
            return
        }
        controllerScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    songApi.addSongToPlaylist(listid, data)
                }
                if (!isFavoriteSong(song)) {
                    favoriteSongList.add(0, song)
                }
            } catch (_: Exception) {
            }
        }
    }

    // 从我喜欢歌单中删除歌曲
    fun removeFavoriteSong(song: Song) {
        val hash = song.hash?.takeIf { it.isNotBlank() } ?: return
        val favoriteSong = favoriteSongList.find { it.hash == hash } ?: return
        val fileid = favoriteSong.fileid
        if (fileid <= 0) {
            return
        }
        val listid = AppGlobalData.favoritePlaylistListId
        if (listid <= 0) {
            return
        }
        controllerScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    songApi.delSongToPlaylist(listid, fileid)
                }
                favoriteSongList.removeAll { it.hash == hash }
            } catch (_: Exception) {
            }
        }
    }

    //构建添加歌单需要的data
    private fun buildData(song: Song): String? {
        val songname = song.songname?.takeIf { it.isNotBlank() }
            ?: song.name?.substringAfter(" - ", missingDelimiterValue = song.name.orEmpty())
                ?.takeIf { it.isNotBlank() }
            ?: song.name?.takeIf { it.isNotBlank() }
        val hash = song.hash?.takeIf { it.isNotBlank() } ?: return null
        if (songname.isNullOrBlank()) {
            return null
        }
        val base = "$songname|$hash"
        val albumId = song.album_id?.takeIf { it.isNotBlank() }
        val mixsongid = song.mixsongid.takeIf { it > 0 }
        return when {
            albumId != null && mixsongid != null -> "$base|$albumId|$mixsongid"
            albumId != null -> "$base|$albumId"
            else -> base
        }
    }
}
