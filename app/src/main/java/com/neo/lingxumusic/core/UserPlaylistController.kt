package com.neo.lingxumusic.core

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object UserPlaylistController {

    var userPlaylistIds = mutableStateListOf<String>() // 用户所有歌单的 global_collection_id

    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 判断某个歌单是否在用户歌单列表中
    fun hasPlaylist(globalCollectionId: String): Boolean {
        return globalCollectionId.isNotBlank() && userPlaylistIds.contains(globalCollectionId)
    }

    // 添加歌单到用户歌单列表
    fun addPlaylist(globalCollectionId: String) {
        if (globalCollectionId.isBlank() || hasPlaylist(globalCollectionId)) {
            return
        }
        controllerScope.launch {
            try {
                // TODO: 调用接口添加歌单
                userPlaylistIds.add(globalCollectionId)
            } catch (_: Exception) {
            }
        }
    }

    // 从用户歌单列表中移除歌单
    fun removePlaylist(globalCollectionId: String) {
        if (globalCollectionId.isBlank() || !hasPlaylist(globalCollectionId)) {
            return
        }
        controllerScope.launch {
            try {
                // TODO: 调用接口移除歌单
                userPlaylistIds.remove(globalCollectionId)
            } catch (_: Exception) {
            }
        }
    }
}
