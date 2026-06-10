package com.neo.lingxumusic.core.viewState.selection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.model.Song

/**
 * 选择模式状态管理
 * 提取自多个 ViewModel 中重复的选择模式逻辑
 */
class SelectionState {

    // 是否处于选择模式
    var isSelectionMode by mutableStateOf(false)
        private set

    // 记录进入选择模式前底部播放弹窗的状态
    var lastBottomPlayState by mutableStateOf(false)

    // 歌曲选择状态 Map<index, Boolean>
    val selectedMap = mutableStateMapOf<Int, Boolean>()

    // 是否全部选中
    var isAllSelected by mutableStateOf(false)

    // 歌曲总数（用于全选）
    var songCount by mutableIntStateOf(0)

    // 是否显示添加到歌单弹窗
    var showAddToPlaylistSheet by mutableStateOf(false)

    // 待添加的歌曲列表
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    // 初始化选中状态
    fun initSelectedMap(count: Int) {
        songCount = count
        selectedMap.clear()
        repeat(count) { selectedMap[it] = false }
    }

    // 切换选择模式
    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            selectedMap.keys.forEach { selectedMap[it] = false }
        }
    }

    // 退出选择模式并清空选择
    fun clearSelection() {
        isSelectionMode = false
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    // 切换指定索引的选中状态
    fun toggleSelect(index: Int) {
        selectedMap[index] = !(selectedMap[index] ?: false)
    }

    // 全选
    fun selectAll() {
        repeat(songCount) { selectedMap[it] = true }
        isAllSelected = true
    }

    // 取消全选（保留选择模式）
    fun clearSongSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    // 获取已选中的索引列表（排序后）
    fun getSelectedIndices(): List<Int> {
        return selectedMap.filter { it.value }.keys.sorted()
    }
}
