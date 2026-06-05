package com.neo.lingxumusic.ui.page.playList

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.UserPlaylistController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.page.playList.component.PlaylistSelectItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.playList.AddToPlaylistViewModel

@Composable
fun AddToPlaylistPage(
    songs: List<Song>,
) {
    val viewModel: AddToPlaylistViewModel = hiltViewModel()
    //mineViewModel在UserPlaylistController初始化过，直接使用，使用依赖注入可以导致实例不是同一个
    val mineViewModel = UserPlaylistController.mineViewModel

    // 初始化数据，只执行一次
    LaunchedEffect(Unit) {
        viewModel.initData(
            songs = songs,
            favorite = mineViewModel.favoritePlayList,
            selfCreate = mineViewModel.selfCreatePlayList.orEmpty()
        )
    }

    // 监听 MineViewModel 数据变化，同步到 AddToPlaylistViewModel
    LaunchedEffect(mineViewModel.favoritePlayList, mineViewModel.selfCreatePlayList) {
        viewModel.initData(
            songs = songs,
            favorite = mineViewModel.favoritePlayList,
            selfCreate = mineViewModel.selfCreatePlayList.orEmpty()
        )
    }

    // 监听返回键
    BackHandler {
        NavController.instance.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 上部区域，点击退出页面
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable { NavController.instance.popBackStack() }
        )

        // 歌单列表和底部栏，占屏幕 0.7
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 40.cdp, topEnd = 40.cdp))
                .background(AppColorsProvider.current.background)
        ) {
            // 歌单列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 32.cdp)
            ) {
                // 我喜欢的歌单
                viewModel.favoritePlaylist?.let { playlist ->
                    item {
                        PlaylistSelectItem(
                            playlist = playlist,
                            isSelected = viewModel.selectedMap[playlist.global_collection_id] ?: false,
                            onSelectClick = {
                                playlist.global_collection_id?.let { id ->
                                    viewModel.toggleSelection(id)
                                }
                            }
                        )
                    }
                }

                // 我创建的歌单
                items(viewModel.selfCreatePlaylists) { playlist ->
                    PlaylistSelectItem(
                        playlist = playlist,
                        isSelected = viewModel.selectedMap[playlist.global_collection_id] ?: false,
                        onSelectClick = {
                            playlist.global_collection_id?.let { id ->
                                viewModel.toggleSelection(id)
                            }
                        }
                    )
                }
            }

            // 底部操作栏
            AddToPlaylistBottomBar()
        }
    }
}

// 底部操作栏
@Composable
private fun AddToPlaylistBottomBar() {
    val viewModel: AddToPlaylistViewModel = hiltViewModel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp)
            .background(
                AppColorsProvider.current.card,
                RoundedCornerShape(16.cdp)
            )
            .padding(vertical = 16.cdp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 全选
        BottomBarOptionButton(
            text = "全选",
            onClick = { viewModel.selectAll() }
        )
        // 取消全选
        BottomBarOptionButton(
            text = "取消全选",
            onClick = { viewModel.clearSelection() }
        )
        // 确定添加
        BottomBarOptionButton(
            text = "确定",
            onClick = {
                // TODO: 调用接口将歌曲添加到选中的歌单
            }
        )
    }
}

@Composable
private fun BottomBarOptionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColorsProvider.current.primary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.cdp),
        modifier = Modifier.padding(horizontal = 8.cdp)
    ) {
        Text(
            text = text,
            fontSize = 24.csp,
            fontWeight = FontWeight.Medium
        )
    }
}
