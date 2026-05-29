package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.mine.component.CommentItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.mine.SongCommentViewModel
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.CollapsingToolbarState
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun SongCommentPage(song: Song?) {
    // 空值处理
    if (song == null) {
        BackHandler(true) { // 拦截返回键
            NavController.instance.popBackStack()
        }
        return
    }

    val viewModel: SongCommentViewModel = hiltViewModel()

    // 返回键处理
    BackHandler(true) {
        NavController.instance.popBackStack()
        MusicPlayController.showPlayMusicSheet = true
    }

    Box {
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { viewModel.commentSortTabs.size },
        )

        val state = rememberCollapsingToolbarScaffoldState()
        CollapsingToolbarScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorsProvider.current.background),
            state = state,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed, // 折叠后退出
            toolbar = {
                //可折叠头部
                ScrollHeader(song, state.toolbarState, pagerState)
            }
        ) {
            Body(song, pagerState)
        }

        //楼中楼评论弹窗
        FloorCommentSheet()
    }
}

@Composable
private fun CollapsingToolbarScope.ScrollHeader(
    song: Song,
    toolbarState: CollapsingToolbarState,
    pagerState: PagerState,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val maxHeight = statusBarTop + (88 + 150 + 100 + 20).cdp
    // progress：1=展开，0=折叠（由 toolbar 高度计算）；折叠后显示固定 Tab
    val showPinnedStickyHeader = toolbarState.progress < 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight)
            .parallax(1f)// 正常速度滚动
            .verticalScroll(rememberScrollState())
    ) {
        SongInfoComponent(song) // 歌曲信息
        if (!showPinnedStickyHeader) {
            StickyHeader(pagerState) // 评论区标题 + Tab
        }
    }

    //没有parallax，不滚动
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColorsProvider.current.background),
    ) {
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            appBarHeight = 88.cdp,
            title = "评论",
            titleAlign = TextAlign.Start,
            leftClick = {
                NavController.instance.popBackStack()
                MusicPlayController.showPlayMusicSheet = true
            },
        )
        //粘性的 评论区标题 + Tab
        if (showPinnedStickyHeader) {
            StickyHeader(pagerState)
        }
    }
}

@Composable
private fun SongInfoComponent(song: Song) {
    val name = song.name ?: ""
    val (singer, songName) = StringUtil.parseSongName(name)
    Column {
        Row(
            modifier = Modifier
                .statusBarsPadding()      // 状态栏内边距
                .padding(top = 88.cdp)    // 顶部内边距
                .fillMaxWidth()
                .height(150.cdp)
                .padding(horizontal = 42.cdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：唱片图标 + 封面图片 (重叠)
            Box(
                modifier = Modifier
                    .size(104.cdp),
                contentAlignment = Alignment.Center
            ) {
                CommonLocalImage(
                    R.drawable.ic_disc,
                    modifier = Modifier.fillMaxSize()
                )
                CommonNetworkImage(
                    song.cover?.replaceSize(),
                    placeholder = R.drawable.ic_default_disk_cover,
                    error = R.drawable.ic_default_disk_cover,
                    modifier = Modifier
                        .size(70.cdp)
                        .clip(CircleShape)
                )
            }
            // 右侧：歌曲名 - 歌手名
            MarqueeText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AppColorsProvider.current.firstText, fontSize = 36.csp)) {
                        append(songName)
                    }
                    withStyle(style = SpanStyle(color = AppColorsProvider.current.secondText, fontSize = 32.csp)) {
                        append(" - $singer")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 22.cdp, end = 48.cdp)
            )
        }

        // 分割线（厚度20dp，相当于一个间隔区域）
        HorizontalDivider(
            color = AppColorsProvider.current.divider,
            modifier = Modifier.fillMaxWidth(),
            thickness = 20.cdp
        )
    }
}

//粘性头部
@Composable
private fun StickyHeader(pagerState: PagerState) {
    val viewModel: SongCommentViewModel = hiltViewModel()
    val selectedIndex = viewModel.commentSortTabs.indexOfFirst { it.type == viewModel.curSelectedTabType }
        .coerceAtLeast(0)
    val scopeState = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.cdp)
            .padding(horizontal = 32.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧："评论区" 标题
        Text(
            text = "评论区",
            color = AppColorsProvider.current.firstText,
            fontSize = 32.csp,
            fontWeight = FontWeight.Bold
        )

        // 右侧：Tab 切换（如"最热"、"最新"）
        CommonTabLayout(
            tabTexts = viewModel.commentSortTabs.map { it.title },
            backgroundColor = Color.Transparent,
            style = CommonTabLayoutStyle(
                isScrollable = false, //不可滚动
                modifier = Modifier.width(300.cdp),
                selectedTextSize = 28.csp,
                unselectedTextSize = 28.csp,
                //画分割线
                tabItemDrawBehindBlock = { position ->
                    if (position != viewModel.commentSortTabs.size - 1) {
                        drawLine(
                            Color.LightGray,
                            Offset(size.width, size.height * 0.35f),
                            Offset(size.width, size.height * 0.65f),
                            strokeWidth = 2.cdp.toPx()
                        )
                    }
                },
                customIndicator = { } //指示器为空，不需要指示器
            ),
            selectedIndex = selectedIndex,
        ) {
            viewModel.curSelectedTabType = viewModel.commentSortTabs[it].type // 切换 Tab
            scopeState.launch {
                //滚向该tab页面
                pagerState.scrollToPage(it)
            }
        }
    }
}

@Composable
private fun Body(song: Song, pagerState: PagerState) {
    val viewModel: SongCommentViewModel = hiltViewModel()

    //水平分页
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        userScrollEnabled = false, //用户不能滚动
    ) { position ->
        CommentPager(song, viewModel.commentSortTabs[position].type)
    }
}

@Composable
private fun CommentPager(song: Song, sortType: Int) {

    val viewModel: SongCommentViewModel = hiltViewModel()
    //当需要的tab数据没有时，加载数据
    if (viewModel.commentBeanListFlows[sortType] == null) {
        viewModel.buildNewCommentListPager(song, sortType)
    }

    //构建评论
    viewModel.commentBeanListFlows[sortType]?.let {
        val commentBeanList = it.collectAsLazyPagingItems()
        ViewStateListPagingComponent(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorsProvider.current.background),
            viewStateComponentModifier = Modifier
                .fillMaxSize()
                .background(AppColorsProvider.current.background),
            collectAsLazyPagingItems = commentBeanList,
            viewStateContentAlignment = BiasAlignment(0f, -0.6f),
            enableRefresh = false, //不允许下拉刷新
        ) {
            items(count = commentBeanList.itemCount) { index ->
                commentBeanList[index]?.let { data ->
                    CommentItem(
                        comment = data,
                        //点击后赋值楼中楼加载需要的数据，触发FloorCommentSheet里面的协程重新加载数据
                        onFloorCommentClick = { comment ->
                            viewModel.song = song
                            viewModel.floorOwnerComment = comment
                            viewModel.floorOwnerCommentId = comment.id
                            viewModel.floorOwnerSpecialChildId = comment.special_child_id
                            viewModel.showFloorCommentSheet = true
                        }
                    )
                }
            }
        }
    }
}
