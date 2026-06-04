package com.neo.lingxumusic.core

import androidx.compose.runtime.mutableStateListOf
import com.neo.lingxumusic.hilt.entrypoint.EntryPointFinder
import com.neo.lingxumusic.http.api.PlaylistApi
import com.neo.lingxumusic.viewmodel.mine.MineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UserPlaylistController {

    var userPlaylistIds = mutableStateListOf<String>() // 用户所有歌单的 global_collection_id

    lateinit var mineViewModel : MineViewModel

    private val playlistApi: PlaylistApi = EntryPointFinder.getPlaylistApi()
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 判断某个歌单是否在用户歌单列表中
    fun hasPlaylist(globalCollectionId: String): Boolean {
        return globalCollectionId.isNotBlank() && userPlaylistIds.contains(globalCollectionId)
    }

    // 收藏歌单
    fun addPlaylist(
        name: String?,
        listCreateUserid: Long,
        listCreateListid: Int,
        globalCollectionId: String,
        listCreateGid: String? = null,
    ) {
        if (listCreateUserid <= 0 || listCreateListid <= 0 || globalCollectionId.isBlank()) {
            return
        }
        controllerScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    playlistApi.addPlaylist(
                        name = name,
                        listCreateUserid = listCreateUserid,
                        listCreateListid = listCreateListid,
                        type = 1, // 收藏歌单
                        listCreateGid = listCreateGid ?: globalCollectionId,
                    )
                }
                if (result.status == 1 && !userPlaylistIds.contains(globalCollectionId)) {
                    userPlaylistIds.add(globalCollectionId)
                    mineViewModel.getUserPlayList()
                }
            } catch (_: Exception) {
            }
        }
    }

    // 取消收藏歌单/删除歌单
    fun removePlaylist(listid: Int, globalCollectionId: String? = null) {
        if (listid <= 0) {
            return
        }
        controllerScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    playlistApi.delPlaylist(listid = listid)
                }
                if (result.status == 1) {
                    globalCollectionId?.let { userPlaylistIds.remove(it) }
                    mineViewModel.getUserPlayList()
                }
            } catch (_: Exception) {
            }
        }
    }
}
