package com.neo.lingxumusic.ui.page.discovery.component

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.UserFavoriteSongsController
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.playMusic.component.SongItem
import com.neo.lingxumusic.ui.page.playMusic.BottomMusicPlayPadding
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.viewmodel.discovery.RankAudioViewModel
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

// 排行榜详情页面
@Composable
fun RankAudioPage(rankInfo: RankInfo) {
    // 底部内边距
    val paddingBottom = if (MusicPlayController.showBottomMusicPlay) {
        BottomMusicPlayPadding
    } else {
        0.dp
    }

    // 折叠工具栏状态
    val state = rememberCollapsingToolbarScaffoldState()
    val density = LocalDensity.current
    // 获取状态栏高度
    val statusBarTop = WindowInsets.statusBars.getTop(density)
    // 判断是否显示标题（当滚动超过阈值时显示榜单名称，否则显示"排行榜"
    val showTitleThreshold =
        (1 - state.toolbarState.progress) >= (statusBarTop + 188.cdp.toPx) / 584.cdp.toPx

    // 可折叠工具栏
    CollapsingToolbarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
            .padding(bottom = paddingBottom),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed, // 折叠后退出
        toolbar = {
            ScrollHeader(
                rankInfo,
                state,
                if (showTitleThreshold) rankInfo.rankname.orEmpty() else "排行榜"
            )
        }
    ) {
        Body(rankInfo)
    }
}

// 排行榜页面可折叠头部组件
@Composable
private fun CollapsingToolbarScope.ScrollHeader(
    rankInfo: RankInfo,
    toolbarState: CollapsingToolbarScaffoldState,
    title: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(584.cdp)           // 头部固定高度
            .parallax(1f)              // 视差效果（滚动速度比内容慢）
    ) {
        // 背景层：圆角 + 渐变背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 圆角随折叠进度变化（progress * 80，范围 0~80）
                .clip(CommonHeadBackgroundShape(toolbarState.toolbarState.progress * 80))
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Gray.copy(0.7f),
                            Color.LightGray.copy(0.7f),
                            Color.Gray.copy(0.7f)
                        )
                    )
                )
        ) {
            // 背景封面图（半透明）
            CommonNetworkImage(
                url = rankInfo.songCoverUrl()?.replaceSize(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(584.cdp)
                    .graphicsLayer { alpha = 0.5f },
            )
            // 榜单名称（展开时显示在封面中央，折叠时淡出）
            Text(
                text = rankInfo.rankname.orEmpty(),
                color = Color.White,
                fontSize = 48.csp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { alpha = toolbarState.toolbarState.progress },  // 随折叠进度淡出
            )
        }
    }

    // 顶部导航栏（固定在屏幕顶部，不随滚动移动）
    CommonTopAppBar(
        modifier = Modifier
            .statusBarsPadding() // 适配状态栏高度
            .fillMaxWidth()
            .height(88.cdp),
        backgroundColor = Color.Transparent,
        title = title,
        contentColor = Color.White,
    )
}

// 排行榜页面主内容（歌曲列表）
@Composable
private fun Body(rankInfo: RankInfo) {
    val viewModel: RankAudioViewModel = hiltViewModel()

    // 初始化分页数据流（如果未初始化）
    if (viewModel.songListFlow == null) {
        viewModel.buildSongListPager(rankInfo)
    }

    viewModel.songListFlow?.let { flow ->
        // 收集分页数据
        val songList = flow.collectAsLazyPagingItems()

        // 禁用过度滚动效果（边缘光效/拉伸效果）
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            ViewStateListPagingComponent(
                modifier = Modifier.fillMaxSize(),
                viewStateComponentModifier = Modifier.fillMaxSize(),
                collectAsLazyPagingItems = songList,
                viewStateContentAlignment = BiasAlignment(0f, -0.6f), // 内容向上偏移
                enableRefresh = false,  // 禁用下拉刷新
            ) {
                item {
                    //播放列表头部
                    RankAudioHeader(songList)
                    // 顶部分割线
                    HorizontalDivider(
                        Modifier.fillMaxWidth(),
                        thickness = 1.cdp,
                        color = Color.LightGray
                    )
                }
                // 歌曲列表
                items(count = songList.itemCount) { index ->
                    songList[index]?.let { item ->
                        SongItem(
                            index = index,
                            song = item,
                            onClick = { MusicPlayController.addSong(item) },
                            //尾部收藏图标
                            trailingIcon = {
                                val isFavorite = UserFavoriteSongsController.isFavoriteSong(item)
                                CommonIcon(
                                    resId = if (isFavorite) R.drawable.ic_like_yes else R.drawable.ic_like_no,
                                    tint = if (isFavorite) {
                                        AppColorsProvider.current.primary
                                    } else {
                                        AppColorsProvider.current.firstIcon
                                    },
                                    modifier = Modifier
                                        .size(32.cdp)
                                        .clip(RoundedCornerShape(4.cdp))
                                        .clickable {
                                            if (isFavorite) {
                                                UserFavoriteSongsController.removeFavoriteSong(item)
                                            } else {
                                                UserFavoriteSongsController.addFavoriteSong(item)
                                            }
                                        }
                                )
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
private fun RankAudioHeader(songList: LazyPagingItems<Song>) {
    val viewModel: RankAudioViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.cdp)
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
            },
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
}

private fun LazyPagingItems<Song>.toSongList(): List<Song> {
    val list = mutableListOf<Song>()
    for (i in 0 until itemCount) {
        get(i)?.let { list.add(it) }
    }
    return list
}
