package com.neo.lingxumusic.ui.page.singerDetail.albumDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.page.playMusic.BottomMusicPlayPadding
import com.neo.lingxumusic.ui.page.playMusic.component.SongItem
import com.neo.lingxumusic.ui.page.playList.AddToPlaylistPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.viewmodel.singerDetail.albumDetail.AlbumDetailViewModel
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun AlbumDetailPage(album: ArtistAlbum) {
    val viewModel: AlbumDetailViewModel = hiltViewModel()
    viewModel.initAlbum(album)

    // 选择模式状态
    // 记录进入选择模式前底部播放弹窗的状态
    // 监听选择模式变化，控制底部播放弹窗显示
    if (viewModel.isSelectionMode) {
        if (MusicPlayController.showBottomMusicPlay) {
            viewModel.lastBottomPlayState = true
            MusicPlayController.showBottomMusicPlay = false
        }
    } else {
        if (viewModel.lastBottomPlayState) {
            MusicPlayController.showBottomMusicPlay = true
            viewModel.lastBottomPlayState = false
        }
    }

    // 歌曲选择状态 Map<index, Boolean>，根据歌曲数量初始化
    if (viewModel.selectedMap.isEmpty()) {
        viewModel.initSelectedMap(album.sum_ownercount)
    }

    // 监听返回键，退出选择模式时恢复底部播放栏状态
    BackHandler(enabled = viewModel.isSelectionMode) {
        viewModel.clearSelection()
        if (viewModel.lastBottomPlayState) {
            MusicPlayController.showBottomMusicPlay = true
            viewModel.lastBottomPlayState = false
        }
    }

    //底部边距（当播放条出来时上移，可以看到所有音乐）
    val paddingBottom = if (MusicPlayController.showBottomMusicPlay) {
        BottomMusicPlayPadding
    } else {
        0.dp
    }

    //工具栏状态
    val state = rememberCollapsingToolbarScaffoldState()
    //标题切换阈值计算
    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.getTop(density)
    val showAlbumTitleThreshold =
        //state.toolbarState.progress：折叠进度（0=完全展开，1=完全折叠）
        (1 - state.toolbarState.progress) >= (statusBarTop + 188.cdp.toPx) / 584.cdp.toPx

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
            .padding(bottom = paddingBottom)
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppColorsProvider.current.background),
            state = state,  // 折叠状态
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,  // 滚动策略：折叠后退出
            toolbar = { // 可折叠的头部内容
                ScrollHeader(
                    album = album,
                    toolbarState = state,
                    title = if (showAlbumTitleThreshold) album.album_name.orEmpty() else "专辑", // 动态标题
                    viewModel = viewModel
                )
            }
        ) {
            Body(viewModel = viewModel, album = album)
        }

        // 选择模式底部栏
        if (viewModel.isSelectionMode) {
            AlbumSelectionBottomBar(viewModel = viewModel)
        }
    }

    // 添加到歌单弹窗
    AddToPlaylistPage(
        songs = viewModel.songsToAdd,
        visible = viewModel.showAddToPlaylistSheet,
        onDismiss = { viewModel.showAddToPlaylistSheet = false }
    )
}

@Composable
//实现了可折叠头部的所有视觉效果
private fun CollapsingToolbarScope.ScrollHeader(
    album: ArtistAlbum,
    toolbarState: CollapsingToolbarScaffoldState,
    title: String,
    viewModel: AlbumDetailViewModel,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(584.cdp)
            .parallax(1f)              // 视差效果，1f=正常速度
    ) {
        // 背景层（带渐变和裁剪）
        Box(
            modifier = Modifier
                .fillMaxSize()
                // progress * 80：圆角半径随折叠进度变化（0→80）
                .clip(CommonHeadBackgroundShape(toolbarState.toolbarState.progress * 80))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCCCCCC),
                            Color(0xFF555555),
                            Color(0xFF000000)
                        )
                    )
                )
        ) {
            // 专辑封面 + 唱片组合
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.cdp)
            ) {
                // 唱片（在封面右侧偏移，只露出一小部分）
                CommonIcon(
                    resId = R.drawable.ic_default_disk_cover,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 40.cdp)
                        .size(260.cdp),
                    tint = Color.Black
                )

                // 专辑封面（正方形圆角）
                CommonNetworkImage(
                    url = album.sizable_cover?.replaceSize(),
                    modifier = Modifier
                        .size(260.cdp)
                        .clip(RoundedCornerShape(20.cdp)),
                    placeholder = R.drawable.ic_default_place_holder,
                    error = R.drawable.ic_default_place_holder
                )
            }

            // 专辑信息（在图片下方）
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.cdp, bottom = 32.cdp)
                    .graphicsLayer {
                        alpha = toolbarState.toolbarState.progress
                    },
                verticalArrangement = Arrangement.spacedBy(10.cdp)
            ) {
                // 专辑名
                Text(
                    text = album.album_name.orEmpty(),
                    color = Color.White,
                    fontSize = 36.csp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // 歌手名
                Text(
                    text = album.author_name.orEmpty(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 24.csp
                )

                // 发行时间
                if (!album.publish_date.isNullOrBlank()) {
                    Text(
                        text = "发行时间：${album.publish_date}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 22.csp
                    )
                }

                // 简介（走马灯效果）
                if (!album.intro.isNullOrBlank()) {
                    MarqueeText(
                        text = album.intro,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 22.csp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }
        }
    }

    //没有parallax，不滚动
    // 顶部导航栏（固定在顶部）
    CommonTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(88.cdp),
        backgroundColor = Color.Transparent,
        title = title, // 动态标题（"专辑" 或 专辑名）
        contentColor = Color.White,
        rightIconResId = R.drawable.ic_drawer_toggle,
        rightClick = { viewModel.toggleSelectionMode() },
        leftClick = {
            // 选择模式开启时，点击返回按钮退出选择模式并恢复底部播放栏
            if (viewModel.isSelectionMode) {
                viewModel.clearSelection()
                if (viewModel.lastBottomPlayState) {
                    MusicPlayController.showBottomMusicPlay = true
                    viewModel.lastBottomPlayState = false
                }
            }
            NavController.instance.popBackStack()
        }
    )
}

@Composable
private fun Body(viewModel: AlbumDetailViewModel, album: ArtistAlbum) {
    val scope = rememberCoroutineScope()

    //当需要的歌曲数据没有时，加载数据
    if (viewModel.songListFlow == null) {
        viewModel.buildSongListPager(album.album_id)
    }

    viewModel.songListFlow?.let { flow ->
        val songList = flow.collectAsLazyPagingItems()

        //禁用 Android 原生的过度滚动效果（边缘发光/拉伸）
        //让滚动效果更干净
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            ViewStateListPagingComponent(
                modifier = Modifier.fillMaxSize(),
                viewStateComponentModifier = Modifier.fillMaxSize(),
                collectAsLazyPagingItems = songList,
                viewStateContentAlignment = BiasAlignment(0f, -0.6f),  // 内容向上偏移
                enableRefresh = false, //不允许下拉刷新
            ) {
                item {
                    //歌曲列表的头部
                    AlbumSongHeader(songList = songList)
                    //水平分割线
                    HorizontalDivider(Modifier.fillMaxWidth(), thickness = 1.cdp, color = Color.LightGray)
                }
                //歌曲
                items(count = songList.itemCount) { index ->
                    songList[index]?.let { song ->
                        SongItem(
                            index = index,
                            song = song,
                            isSelectionMode = viewModel.isSelectionMode,
                            isSelected = viewModel.selectedMap[index] ?: false,
                            onSelectClick = { idx ->
                                viewModel.selectedMap[idx] = !(viewModel.selectedMap[idx] ?: false)
                            },
                            onClick = {
                                scope.launch {
                                    val songs = songList.toSongList()
                                    if (songs.getOrNull(index)?.hash.isNullOrEmpty()) {
                                        showToast("该歌曲暂不支持播放")
                                    } else {
                                        MusicPlayController.songList.clear()
                                        //通过这个函数过滤所有无法播放的歌曲
                                        MusicPlayController.setDataSource(
                                            songs,
                                            songs[index].hash
                                        )
                                        MusicPlayController.showBottomMusicPlay = false
                                        MusicPlayController.showPlayMusicSheet = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

//歌曲列表的头部，显示"播放全部"按钮
@Composable
private fun AlbumSongHeader(songList: LazyPagingItems<Song>) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.cdp),
        verticalAlignment = Alignment.CenterVertically  // 垂直居中
    ) {
        // 左侧播放按钮区域
        Row(
            modifier = Modifier
                .clickable {
                    //播放当前列表
                    scope.launch {
                        val songs = songList.toSongList()
                        if (songs.isEmpty()) return@launch
                        MusicPlayController.setDataSource(
                            songs,
                            songs.firstOrNull()?.hash
                        )
                        MusicPlayController.showBottomMusicPlay = false
                        MusicPlayController.showPlayMusicSheet = true
                    }
                }
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommonIcon(
                R.drawable.ic_play_list_header_play,
                tint = AppColorsProvider.current.primary,
                modifier = Modifier
                    .padding(horizontal = 32.cdp)
                    .size(50.cdp)
            )
            Text(
                text = "播放全部",
                fontSize = 32.csp,
                fontWeight = FontWeight.Bold,
                color = AppColorsProvider.current.firstText,
            )
        }
    }
}

// 选择模式底部栏（无删除按钮）
@Composable
private fun AlbumSelectionBottomBar(viewModel: AlbumDetailViewModel) {
    val scope = rememberCoroutineScope()
    val songList = viewModel.songListFlow?.collectAsLazyPagingItems()
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
            Text(
                text = if (viewModel.isAllSelected) "取消全选" else "全选",
                fontSize = 22.csp,
                color = colors.firstText,
                modifier = Modifier
                    .clickable {
                        if (viewModel.isAllSelected) {
                            viewModel.clearSongSelection()
                        } else {
                            viewModel.selectAll()
                        }
                    }
                    .padding(horizontal = 12.cdp, vertical = 8.cdp)
            )
            Text(
                text = "播放",
                fontSize = 22.csp,
                color = colors.firstText,
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            val selectedIndices = viewModel.selectedMap.filter { it.value }.keys.sorted()
                            if (selectedIndices.isEmpty()) {
                                showToast("请先选择歌曲")
                                return@launch
                            }
                            val songs = songList?.toSongList().orEmpty()
                            val selectedSongs = selectedIndices.mapNotNull { songs.getOrNull(it) }
                            if (selectedSongs.isEmpty()) {
                                showToast("没有可播放的歌曲")
                                viewModel.clearSelection()
                                return@launch
                            }
                            MusicPlayController.songList.clear()
                            MusicPlayController.setDataSource(selectedSongs, selectedSongs.firstOrNull()?.hash)
                            MusicPlayController.showBottomMusicPlay = false
                            MusicPlayController.showPlayMusicSheet = true
                            viewModel.clearSelection()
                        }
                    }
                    .padding(horizontal = 12.cdp, vertical = 8.cdp)
            )
            Text(
                text = "添加到歌单",
                fontSize = 22.csp,
                color = colors.firstText,
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            val selectedIndices = viewModel.selectedMap.filter { it.value }.keys.sorted()
                            if (selectedIndices.isEmpty()) {
                                showToast("请先选择歌曲")
                                return@launch
                            }
                            val songs = songList?.toSongList().orEmpty()
                            val selectedSongs = selectedIndices.mapNotNull { songs.getOrNull(it) }
                            if (selectedSongs.isEmpty()) {
                                showToast("没有可添加的歌曲")
                                viewModel.clearSelection()
                                return@launch
                            }
                            viewModel.songsToAdd = selectedSongs
                            viewModel.showAddToPlaylistSheet = true
                            viewModel.clearSelection()
                        }
                    }
                    .padding(horizontal = 12.cdp, vertical = 8.cdp)
            )
        }
    }
}

private fun LazyPagingItems<Song>.toSongList(): List<Song> {
    return (0 until itemCount).mapNotNull { get(it) }
}
