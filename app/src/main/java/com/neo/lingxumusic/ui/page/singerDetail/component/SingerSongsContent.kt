package com.neo.lingxumusic.ui.page.singerDetail.component


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.model.SingerSongItem
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.toSong
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.page.playMusic.component.SongItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.viewmodel.singerDetail.SingerDetailViewModel
import kotlinx.coroutines.launch


//歌曲
fun LazyListScope.singerSongListItems(
    viewModel: SingerDetailViewModel,
    songList: LazyPagingItems<SingerSongItem>
) {
    // 歌曲列表
    items(
        count = songList.itemCount,
        key = songList.itemKey { it.hash ?: it.audio_name ?: it.toString() }
    ) { index ->
        val item = songList[index]
        if (item != null) {
            val song = item.toSong()
            SongItem(
                index = index,
                song = song,
                isSelectionMode = viewModel.isSelectionMode,
                isSelected = viewModel.selectedMap[index] ?: false,
                onSelectClick = { idx ->
                    viewModel.toggleSongSelection(idx)
                },
                onClick = {
                    if (viewModel.isSelectionMode) {
                        viewModel.toggleSongSelection(index)
                    } else {
                        MusicPlayController.addSong(song)
                    }
                }
            )
        }
    }
}

//头部可选择框
@Composable
fun SingerSongsHeader(
    viewModel: SingerDetailViewModel
) {
    val colors = AppColorsProvider.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.cdp)
                .padding(horizontal = 24.cdp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (!viewModel.isSelectionMode) {
                SortSelector(
                    currentSort = viewModel.sortType,
                    showDropdown = viewModel.showSortDropdown,
                    onToggleDropdown = { viewModel.showSortDropdown = !viewModel.showSortDropdown }
                )
                Spacer(modifier = Modifier.padding(horizontal = 16.cdp))
            }
            CommonIcon(
                resId = R.drawable.ic_drawer_toggle,
                tint = colors.firstIcon,
                modifier = Modifier
                    .size(32.cdp)
                    .clickable { viewModel.toggleSelectionMode() }
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.cdp,
            color = colors.divider
        )

        // 排序下拉选项
        AnimatedVisibility(
            visible = viewModel.showSortDropdown,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.cdp)
                    .background(colors.card, RoundedCornerShape(12.cdp))
            ) {
                SortOptionItem("最热", viewModel.sortType == 1) { viewModel.changeSortType(1) }
                SortOptionItem("最新", viewModel.sortType == 2) { viewModel.changeSortType(2) }
            }
        }
    }
}


//底部弹窗
@Composable
fun SingerSelectionBottomBar(
    songList: LazyPagingItems<SingerSongItem>? = null
) {
    val viewModel: SingerDetailViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    // 优先使用外部传入的 songList，否则从 viewModel 获取
    val effectiveSongList = songList ?: viewModel.songListFlow?.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorsProvider.current.card, RoundedCornerShape(16.cdp))
                .padding(vertical = 16.cdp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 全选 / 取消全选
            BottomBarOptionItem(
                text = if (viewModel.isAllSelected) "取消全选" else "全选",
                onClick = {
                    if (viewModel.isAllSelected) viewModel.clearSongSelection()
                    else viewModel.selectAll()
                }
            )
            // 播放
            BottomBarOptionItem(
                text = "播放",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            viewModel, effectiveSongList, "没有可播放的歌曲"
                        ) ?: return@launch
                        MusicPlayController.songList.clear()
                        MusicPlayController.setDataSource(
                            selectedSongs,
                            selectedSongs.firstOrNull()?.hash
                        )
                        MusicPlayController.showBottomMusicPlay = false
                        MusicPlayController.showPlayMusicSheet = true
                        viewModel.clearSelection()
                    }
                }
            )
            // 添加到歌单
            BottomBarOptionItem(
                text = "添加到歌单",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            viewModel, effectiveSongList, "没有可添加的歌曲"
                        ) ?: return@launch
                        viewModel.songsToAdd = selectedSongs
                        viewModel.showAddToPlaylistSheet = true
                    }
                }
            )
        }
    }
}


@Composable
private fun BottomBarOptionItem(text: String, onClick: () -> Unit) {
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
    viewModel: SingerDetailViewModel,
    songList: LazyPagingItems<SingerSongItem>?,
    emptyTip: String,
): List<Song>? {
    // 过滤出已经选择的歌曲索引
    val selectedIndices = viewModel.selectedMap.filter { it.value }.keys.sorted()
    if (selectedIndices.isEmpty()) {
        showToast("请先选择歌曲")
        return null
    }
    // 判断歌曲是否加载完全
    val maxIndex = selectedIndices.last()
    var songs = songList?.toSongList().orEmpty()
    if (maxIndex >= songs.size) {
        songs = viewModel.loadAllSongs()
    }
    if (songs.isEmpty()) {
        showToast("网络请求失败，请稍后重试")
        viewModel.clearSelection()
        return null
    }
    // 选出歌曲
    val selectedSongs = selectedIndices.mapNotNull { songs.getOrNull(it) }
    if (selectedSongs.isEmpty()) {
        showToast(emptyTip)
        viewModel.clearSelection()
        return null
    }
    return selectedSongs
}


private fun LazyPagingItems<SingerSongItem>.toSongList(): List<Song> {
    return (0 until itemCount).mapNotNull { get(it)?.toSong() }
}


@Composable
private fun SortSelector(
    currentSort: Int,
    showDropdown: Boolean,
    onToggleDropdown: () -> Unit
) {
    val colors = AppColorsProvider.current
    val sortText = if (currentSort == 1) "最热" else "最新"

    val rotation by animateFloatAsState(
        targetValue = if (showDropdown) 180f else 0f,
        label = ""
    )

    Row(
        modifier = Modifier.clickable { onToggleDropdown() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.cdp)
    ) {
        Text(text = sortText, color = colors.firstText, fontSize = 28.csp)
        CommonIcon(
            resId = R.drawable.ic_arrow_down,
            tint = colors.firstIcon,
            modifier = Modifier.size(24.cdp).rotate(rotation)
        )
    }
}

@Composable
private fun SortOptionItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = AppColorsProvider.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.cdp, vertical = 16.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.primary else colors.firstText,
            fontSize = 28.csp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        if (isSelected) {
            CommonIcon(
                resId = R.drawable.ic_checked,
                tint = colors.primary,
                modifier = Modifier.size(28.cdp)
            )
        }
    }
}
