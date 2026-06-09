package com.neo.lingxumusic.ui.page.singerDetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.ArtistDetail
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerHeader
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerSelectionBottomBar
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerSongsHeader
import com.neo.lingxumusic.ui.page.singerDetail.component.singerHomeItems
import com.neo.lingxumusic.ui.page.singerDetail.component.singerSongListItems
import com.neo.lingxumusic.ui.page.playList.AddToPlaylistPage
import com.neo.lingxumusic.ui.page.singerDetail.component.singerMvContent
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.transformDp
import com.neo.lingxumusic.viewmodel.singerDetail.SingerDetailViewModel
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

private val HEADER_HEIGHT = 400.cdp
private val NAV_BAR_HEIGHT = 88.cdp
private val TAB_LAYOUT_HEIGHT = 100.cdp

@Composable
fun SingerDetailPage(singerId: Long) {
    val viewModel: SingerDetailViewModel = hiltViewModel()

    LaunchedEffect(singerId) {
        viewModel.loadSingerDetail(singerId)
    }

    ViewStateComponent(
        viewStateLiveData = viewModel.singerResult,
        loadDataBlock = { viewModel.loadSingerDetail(singerId) },
        contentView = {
            SingerDetailContent(viewModel)
        }
    )
}

@Composable
private fun SingerDetailContent(viewModel: SingerDetailViewModel) {
    val artistDetail = viewModel.artistDetail ?: return
    val colors = AppColorsProvider.current
    val density = LocalDensity.current

    // 当前选中的 Tab 索引
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()
    val statusBarsTopDp = WindowInsets.statusBars.getTop(density).transformDp

    val toolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
    // 顶栏 alpha 计算：progress 完全展开（>= 0.97）时严格为 0，
    // 折叠超过 3% 后开始线性增长，超过一半（<= 0.5）后才完全显示。
    // 渐变范围拉到 47%（0.97→0.5），让颜色"回来"得更慢。
    val topBarAlpha = run {
        val p = toolbarScaffoldState.toolbarState.progress
        when {
            p >= 0.97f -> 0f
            p <= 0.50f -> 1f
            else -> (0.97f - p) / 0.47f
        }
    }

    val tabTexts = listOf(
        "主页",
        buildTabText("单曲", artistDetail.song_count),
        buildTabText("专辑", artistDetail.album_count),
        buildTabText("MV", artistDetail.mv_count)
    )

    val singerIdLong = artistDetail.author_id?.toLongOrNull() ?: 0L

    // 根据 tab 切换对应加载（首次切到该 tab 才加载）
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            1 -> viewModel.buildSongListPager(singerIdLong)
            3 -> viewModel.buildMvListPager(singerIdLong)
        }
    }

    // 返回键：选择模式退出
    BackHandler(enabled = viewModel.isSelectionMode) {
        viewModel.clearSelection()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CollapsingToolbarScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            state = toolbarScaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = {
                ScrollHeader(artistDetail, statusBarsTopDp)
            }
        ) {
            // body: LazyColumn（含 stickyHeader Tab + 切换内容）
            Body(
                viewModel = viewModel,
                artistDetail = artistDetail,
                lazyListState = lazyListState,
                selectedTabIndex = selectedTabIndex,
                tabTexts = tabTexts,
                onTabSelected = { selectedTabIndex = it }
            )
        }

        // 顶部固定导航栏：背景与文字都按 topBarAlpha 透明度渐变（颜色恒为 firstText）
        CommonTopAppBar(
            modifier = Modifier
                .background(colors.background.copy(alpha = topBarAlpha))
                .statusBarsPadding(),
            title = artistDetail.author_name ?: "",
            backgroundColor = Color.Transparent,
            contentColor = colors.firstText.copy(alpha = topBarAlpha),
            leftIconResId = R.drawable.ic_back,
            leftClick = { NavController.instance.popBackStack() }
        )

        // 选择模式底部栏（使用 Box.align(Alignment.BottomCenter) 固定在屏幕底部）
        val songList = viewModel.songListFlow?.collectAsLazyPagingItems()
        if (viewModel.isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                SingerSelectionBottomBar(songList = songList)
            }
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
private fun CollapsingToolbarScope.ScrollHeader(
    artistDetail: ArtistDetail,
    statusBarsTopDp: Dp
) {
    val headerTotalHeight = statusBarsTopDp + NAV_BAR_HEIGHT + HEADER_HEIGHT
    // 滚动部分：SingerHeader 从 toolbar 顶部 0 开始铺满整个 toolbar 高度
    Column(
        Modifier
            .fillMaxWidth()
            .height(headerTotalHeight)
            .parallax(1f)
            .verticalScroll(rememberScrollState())
    ) {
        SingerHeader(
            avatarUrl = artistDetail.sizable_avatar?.replaceSize(),
            singerName = artistDetail.author_name,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerTotalHeight)
        )
    }

    // 钉住：状态栏 + 顶栏占位（CommonTopAppBar 叠加在头部上方时位置正确）
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarsTopDp + NAV_BAR_HEIGHT)
    )
}


@Composable
private fun Body(
    viewModel: SingerDetailViewModel,
    artistDetail: ArtistDetail,
    lazyListState: LazyListState,
    selectedTabIndex: Int,
    tabTexts: List<String>,
    onTabSelected: (Int) -> Unit
) {
    val colors = AppColorsProvider.current
    val songList = viewModel.songListFlow?.collectAsLazyPagingItems()
    val mvList = viewModel.mvListFlow?.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        state = lazyListState
    ) {
        // Tab 吸顶
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
            ) {
                CommonTabLayout(
                    selectedIndex = selectedTabIndex,
                    tabTexts = tabTexts,
                    style = CommonTabLayoutStyle(
                        isScrollable = false,
                        selectedTextSize = 32.csp,
                        unselectedTextSize = 32.csp,
                        selectedTextBold = true,
                        unselectedTextBold = false,
                        indicatorHeight = 0.cdp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TAB_LAYOUT_HEIGHT)
                    ),
                    backgroundColor = colors.background,
                    selectedTextColor = colors.primary,
                    unselectedTextColor = colors.secondText,
                    onTabSelected = onTabSelected
                )
            }
        }

        // 内容（根据 tab 切换不同 items）
        when (selectedTabIndex) {
            0 -> singerHomeItems(artistDetail = artistDetail)
            1 -> {
                // 吸顶头部：排序 + 选择图标
                stickyHeader {
                    SingerSongsHeader(viewModel = viewModel)
                }
                if (songList != null) {
                    singerSongListItems(viewModel = viewModel, songList = songList)
                }
            }
            2 -> item(key = "albums") {
                AlbumsContent()
            }
            3 -> if (mvList != null) {
                singerMvContent(mvList = mvList)
            }
        }
    }
}

@Composable
private fun AlbumsContent() {
    val colors = AppColorsProvider.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.cdp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "专辑列表", color = colors.thirdText, fontSize = 28.csp)
    }
}

private fun buildTabText(title: String, count: Int): String {
    return "$title ${StringUtil.friendlyNumber(count)}"
}
