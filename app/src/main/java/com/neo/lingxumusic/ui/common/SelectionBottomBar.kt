package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.selection.SelectionState
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.showToast
import kotlinx.coroutines.launch

/**
 * 选择模式底部操作栏
 *
 * @param selectionState 选择状态管理
 * @param getSelectedSongs 根据已选索引获取歌曲列表的挂起函数
 * @param showDelete 是否显示删除按钮
 * @param onDelete 删除按钮回调
 */
@Composable
fun SelectionBottomBar(
    selectionState: SelectionState,
    getSelectedSongs: suspend (selectedIndices: List<Int>) -> List<Song>?,
    showDelete: Boolean = false,
    onDelete: ((List<Song>) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val colors = AppColorsProvider.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, RoundedCornerShape(16.cdp))
                .padding(vertical = 16.cdp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 全选 / 取消全选
            SelectionBottomBarItem(
                text = if (selectionState.isAllSelected) "取消全选" else "全选",
                onClick = {
                    if (selectionState.isAllSelected) {
                        selectionState.clearSongSelection()
                    } else {
                        selectionState.selectAll()
                    }
                }
            )
            // 播放
            SelectionBottomBarItem(
                text = "播放",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            selectionState, getSelectedSongs, "没有可播放的歌曲"
                        ) ?: return@launch
                        MusicPlayController.songList.clear()
                        MusicPlayController.setDataSource(
                            selectedSongs,
                            selectedSongs.firstOrNull()?.hash
                        )
                        MusicPlayController.showBottomMusicPlay = false
                        MusicPlayController.showPlayMusicSheet = true
                        selectionState.clearSelection()
                    }
                }
            )
            // 添加到歌单
            SelectionBottomBarItem(
                text = "添加到歌单",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            selectionState, getSelectedSongs, "没有可添加的歌曲"
                        ) ?: return@launch
                        selectionState.songsToAdd = selectedSongs
                        selectionState.showAddToPlaylistSheet = true
                        selectionState.clearSelection()
                    }
                }
            )
            // 删除（可选）
            if (showDelete && onDelete != null) {
                SelectionBottomBarItem(
                    text = "删除",
                    onClick = {
                        scope.launch {
                            val selectedSongs = extractSelectedSongs(
                                selectionState, getSelectedSongs, "没有可删除的歌曲"
                            ) ?: return@launch
                            onDelete(selectedSongs)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectionBottomBarItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 22.csp,
        color = AppColorsProvider.current.firstText,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.cdp, vertical = 8.cdp)
    )
}

private suspend fun extractSelectedSongs(
    selectionState: SelectionState,
    getSelectedSongs: suspend (List<Int>) -> List<Song>?,
    emptyTip: String,
): List<Song>? {
    val selectedIndices = selectionState.getSelectedIndices()
    if (selectedIndices.isEmpty()) {
        showToast("请先选择歌曲")
        return null
    }
    val songs = getSelectedSongs(selectedIndices)
    if (songs == null || songs.isEmpty()) {
        showToast(emptyTip)
        selectionState.clearSelection()
        return null
    }
    return songs
}
