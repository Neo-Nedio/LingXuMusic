package com.neo.lingxumusic.ui.page.playList

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.UserPlaylistController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewState
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import androidx.compose.runtime.livedata.observeAsState
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.page.playMusic.BottomMusicPlayPadding
import com.neo.lingxumusic.ui.page.playMusic.component.SongItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.viewmodel.playList.PlayListViewModel
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun PlaylistPage(playlist: PlaylistBrief) {
    val viewModel: PlayListViewModel = hiltViewModel()
    viewModel.playlist = playlist

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
        viewModel.initSelectedMap(playlist.count)
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
    val showPlayListTitleThreshold =
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
                    playlist,
                    state,
                    if (showPlayListTitleThreshold) playlist.displayName() else "歌单", // 动态标题
                    onToggleSelectionMode = { viewModel.toggleSelectionMode() }
                )
            }
        ) {
            Body()
        }

        // 选择模式底部栏
        if (viewModel.isSelectionMode) {
            SelectionBottomBar()
        }
    }

    // 添加到歌单弹窗
    AddToPlaylistPage(
        songs = viewModel.songsToAdd,
        visible = viewModel.showAddToPlaylistSheet,
        onDismiss = { viewModel.showAddToPlaylistSheet = false }
    )

    // 删除歌曲状态弹窗
    val deleteViewState by viewModel.deleteSongsResult.observeAsState()
    LaunchedEffect(deleteViewState) {
        when (deleteViewState) {
            is ViewState.Success -> {
                showToast("删除成功")
                viewModel.clearSelection()
                viewModel.deleteSongsResult.value = null
                // 刷新歌单歌曲列表
                viewModel.songListFlow = null
                viewModel.buildSongListPager(viewModel.playlist)
            }
            is ViewState.Fail -> {
                val data = (deleteViewState as ViewState.Fail).errorMsg
                showToast("删除失败: $data")
                viewModel.deleteSongsResult.value = null
            }
            is ViewState.Error -> {
                showToast("删除失败")
                viewModel.deleteSongsResult.value = null
            }
            else -> {}
        }
    }
}

@Composable
//实现了可折叠头部的所有视觉效果
private fun CollapsingToolbarScope.ScrollHeader(
    playlist: PlaylistBrief,
    toolbarState: CollapsingToolbarScaffoldState,
    title: String,
    onToggleSelectionMode: () -> Unit,
) {
    val viewModel: PlayListViewModel = hiltViewModel()
    //底部按钮栏淡出阈值计算
/*    584f：头部总高度
    88：导航栏高度
    584 - 88 = 496：内容区域高度
    1 - 496/584 = 1 - 0.85 = 0.15*/
    //当折叠进度超过 15% 时，底部按钮栏开始淡出
    val headCountInfoLayoutChangeAlphaThreshold = remember { 1 - (584f - 88) / 584 }
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
                .background(brush = Brush.linearGradient(listOf(Color.Gray.copy(0.7f), Color.LightGray.copy(0.7f), Color.Gray.copy(0.7f))))
        ) {
            HeadBackground(playlist)  // 背景图片
            HeadPlayListInfo(
                //渐隐效果
                modifier = Modifier.graphicsLayer { alpha = toolbarState.toolbarState.progress }, // 歌单信息（封面、名称、创建者）
                playlist
            )
        }
        /*// 底部按钮栏（播放、评论、分享）
        HeadCountInfoLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter) //位于底部
                .graphicsLayer {
                    val alphaValue = toolbarState.toolbarState.progress
                    //当 progress < 0.15 时，按钮栏随滚动逐渐淡出
                    if (alphaValue < headCountInfoLayoutChangeAlphaThreshold) {
                        alpha = toolbarState.toolbarState.progress
                    }
                },
            playlist
        )*/
    }

    //没有parallax，不滚动
    // 顶部导航栏（固定在顶部）
    CommonTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(88.cdp),
        backgroundColor = Color.Transparent,
        title = title, // 动态标题（"歌单" 或 歌单名）
        contentColor = Color.White,
        rightIconResId = R.drawable.ic_drawer_toggle,
        rightClick = onToggleSelectionMode,
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

//背景图
@Composable
private fun HeadBackground(playlist: PlaylistBrief) {
    CommonNetworkImage(
        url = playlist.pic?.replaceSize(),
        modifier = Modifier
            .fillMaxWidth()
            .height(584.cdp)
            .graphicsLayer { alpha = 0.5f },
    )
}

//歌单信息
@Composable
private fun HeadPlayListInfo(modifier: Modifier, playlist: PlaylistBrief) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 132.cdp)
            .fillMaxSize()
            .padding(horizontal = 32.cdp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：歌单封面（正方形）
        CommonNetworkImage(
            url = playlist.pic?.replaceSize(),
            modifier = Modifier
                .size(240.cdp)
                .clip(RoundedCornerShape(16.cdp))
        )
        // 右侧：歌单信息
        Column(
            modifier = Modifier
                .padding(start = 32.cdp)
                .weight(1f)
                .height(240.cdp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // 歌单名
            Text(text = playlist.displayName(), color = Color.White, fontWeight = FontWeight.Medium, maxLines = 2, fontSize = 28.csp)

            // 创建者头像 + 昵称
            Row(
                modifier = Modifier.height(88.cdp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CommonNetworkImage(
                    url = playlist.create_user_pic ?: AppGlobalData.sLoginData?.pic,
                    placeholder = R.drawable.ic_default_avator,
                    error = R.drawable.ic_default_avator,
                    modifier = Modifier
                        .size(50.cdp)
                        .clip(RoundedCornerShape(50))
                )
                Text(
                    text = playlist.list_create_username ?: AppGlobalData.sLoginData?.nickname.orEmpty(),
                    fontSize = 28.csp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.cdp)
                )
            }

            // 歌单简介
            MarqueeText(
                text = playlist.descriptionText(),
                color = Color.White,
                fontSize = 28.csp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/*//底部按钮栏
@Composable
private fun HeadCountInfoLayout(modifier: Modifier, playlist: PlaylistBrief) {
    Row(
        modifier = modifier
            .padding(start = 32.cdp, end = 32.cdp, bottom = 4.cdp)
            .height(80.cdp)
            .fillMaxWidth()
            .padding(horizontal = 16.cdp)
            .shadow(2.dp, RoundedCornerShape(50))
            .background(AppColorsProvider.current.card),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCountInfoItem(
            R.drawable.ic_action_play,
            "播放(0)",
            true
        )
        HeaderCountInfoItem(
            R.drawable.ic_comment_count,
            "评论(0)",
            true
        )
        HeaderCountInfoItem(
            R.drawable.ic_share,
            "分享(0)",
            false
        )
    }
}*/
/*
//底部按钮栏
@Composable
private fun RowScope.HeaderCountInfoItem(
    iconRedId: Int,      // 图标资源 ID
    text: String,        // 显示的文字（如"播放(24)"）
    showDivider: Boolean // 是否显示右侧分割线
) {
    Row(modifier = Modifier.weight(1f)) {
        // 主要内容区域（图标 + 文字）
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 图标
            Icon(
                painterResource(iconRedId),
                "",
                tint = AppColorsProvider.current.firstIcon,
                modifier = Modifier
                    .size(40.cdp)
                    .padding(end = 8.cdp)
            )
            // 文字
            Text(
                text = text,
                fontSize = 24.csp,
                color = AppColorsProvider.current.firstText,
            )
        }
        // 分割线（右侧）
        if (showDivider) {
            VerticalDivider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(40.cdp)
                    .width(2.cdp)
            )
        }
    }
}*/

@Composable
private fun Body() {
    val viewModel: PlayListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    //当需要的歌单数据没有时，加载数据
    if (viewModel.songListFlow == null) {
        viewModel.buildSongListPager(viewModel.playlist)
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
                    PlayListHeader(viewModel.playlist, songList)
                    //水平分割线
                    HorizontalDivider(Modifier.fillMaxWidth(), thickness = 1.cdp, color = Color.LightGray)
                }
                //歌曲
                items(count = songList.itemCount) { index ->
                    songList[index]?.let { item ->
                        SongItem(
                            index = index,
                            song = item,
                            isSelectionMode = viewModel.isSelectionMode,
                            isSelected = viewModel.selectedMap[index] ?: false,
                            onSelectClick = { idx ->
                                viewModel.selectedMap[idx] = !(viewModel.selectedMap[idx] ?: false)
                            },
                            onClick = {
                                scope.launch {
                                    var songs = songList.toSongList()
                                    //判断歌曲是否全部加载完成，否则加载
                                    if (songs.size < viewModel.songCount) {
                                        songs = viewModel.loadAllSongs()
                                    }
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

//歌曲列表的头部，显示"播放全部"按钮和歌曲数量
@Composable
private fun PlayListHeader(playlist: PlaylistBrief, songList: LazyPagingItems<Song>) {
    val viewModel: PlayListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val isCollected = UserPlaylistController.hasPlaylist(playlist.global_collection_id.orEmpty())
    
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
                    //判断歌曲是否全部加载完成，否则加载
                    scope.launch {
                        var songs = songList.toSongList()
                        if (songs.size < viewModel.songCount) {
                            songs = viewModel.loadAllSongs()
                        }
                        MusicPlayController.setDataSource(
                            songs,
                            songs.firstOrNull()?.hash
                        )
                        MusicPlayController.showBottomMusicPlay = false
                        MusicPlayController.showPlayMusicSheet = true
                    }
                }
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically  // 垂直居中
        ) {
            // 左侧播放图标
            CommonIcon(
                R.drawable.ic_play_list_header_play,
                tint = AppColorsProvider.current.primary,
                modifier = Modifier
                    .padding(horizontal = 32.cdp)
                    .size(50.cdp)
            )

            // "播放全部" 文字
            Text(
                text = "播放全部",
                fontSize = 32.csp,
                fontWeight = FontWeight.Bold,
                color = AppColorsProvider.current.firstText,
            )
            // 歌曲数量，如 "(24)"
            Text(
                text = "(${viewModel.songCount})",
                fontSize = 28.csp,
                color = AppColorsProvider.current.secondText,
            )
        }

        // 收藏按钮（"我喜欢"歌单不显示）
        if (playlist.global_collection_id != AppGlobalData.favoritePlaylistGlobalCollectionId) {
            Row(
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            if (isCollected) {
                                UserPlaylistController.removePlaylist(
                                    listid = playlist.listid,
                                    globalCollectionId = playlist.global_collection_id
                                )
                            } else {
                                UserPlaylistController.addPlaylist(
                                    name = playlist.name,
                                    listCreateUserid = playlist.list_create_userid,
                                    listCreateListid = playlist.list_create_listid,
                                    globalCollectionId = playlist.global_collection_id.orEmpty()
                                )
                            }
                        }
                    }
                    .padding(horizontal = 32.cdp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.cdp)
            ) {
                CommonIcon(
                    resId = if (isCollected) R.drawable.ic_like_yes else R.drawable.ic_like_no,
                    tint = if (isCollected) {
                        AppColorsProvider.current.primary
                    } else {
                        AppColorsProvider.current.firstIcon
                    },
                    modifier = Modifier.size(32.cdp)
                )
                Text(
                    text = "收藏",
                    fontSize = 28.csp,
                    color = AppColorsProvider.current.firstText
                )
            }
        }
    }
}

// 选择模式底部栏
@Composable
private fun SelectionBottomBar() {
    val viewModel: PlayListViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val songList = viewModel.songListFlow?.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColorsProvider.current.card,
                    RoundedCornerShape(16.cdp)
                )
                .padding(vertical = 16.cdp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 全选 / 取消全选
            BottomBarOptionItem(
                text = if (viewModel.isAllSelected) "取消全选" else "全选",
                onClick = {
                    if (viewModel.isAllSelected) {
                        viewModel.clearSongSelection()
                    } else {
                        viewModel.selectAll()
                    }
                }
            )
            // 播放
            BottomBarOptionItem(
                text = "播放",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            viewModel, songList, "没有可播放的歌曲"
                        ) ?: return@launch
                        // 开始播放
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
                            viewModel, songList, "没有可添加的歌曲"
                        ) ?: return@launch
                        // 显示添加到歌单弹窗
                        viewModel.songsToAdd = selectedSongs
                        viewModel.showAddToPlaylistSheet = true
                        viewModel.clearSelection()
                    }
                }
            )
            // 删除
            BottomBarOptionItem(
                text = "删除",
                onClick = {
                    scope.launch {
                        val selectedSongs = extractSelectedSongs(
                            viewModel, songList, "没有可删除的歌曲"
                        ) ?: return@launch
                        viewModel.deleteSongsFromPlaylist(selectedSongs)
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomBarOptionItem(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        fontSize = 22.csp,
        color = AppColorsProvider.current.firstText,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.cdp, vertical = 8.cdp)
    )
}

private fun LazyPagingItems<Song>.toSongList(): List<Song> {
    return (0 until itemCount).mapNotNull { get(it) }
}

private fun PlaylistBrief.displayName(): String {
    return name.orEmpty()
}

private fun PlaylistBrief.descriptionText(): String {
    return intro?.takeIf { it.isNotBlank() } ?: "暂无描述"
}

private fun PlaylistBrief.playCountValue(): Number {
    return count
}

/**
 * 从已选索引中提取歌曲列表
 * 如果歌曲未完全加载，会自动调用 loadAllSongs 加载全部
 * @return 提取成功返回歌曲列表，失败返回 null（已处理提示和清空选择）
 */
private suspend fun extractSelectedSongs(
    viewModel: PlayListViewModel,
    songList: LazyPagingItems<Song>?,
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
