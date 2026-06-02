package com.neo.lingxumusic.core

import androidx.compose.runtime.mutableStateListOf
import com.neo.lingxumusic.hilt.entrypoint.EntryPointFinder
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
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadFavoriteSongJob: Job? = null

    private const val FAVORITE_PLAYLIST_ID = "collection_3_1764149159_2_0"

    // 加载我喜欢的歌曲（在进入应用时就调用）
    fun loadFavoriteSongs() {
        loadFavoriteSongJob?.cancel()
        loadFavoriteSongJob = controllerScope.launch {
            try {
                val songs = withContext(Dispatchers.IO) {
                    userApi.getPlaylistSong(FAVORITE_PLAYLIST_ID)
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
}
