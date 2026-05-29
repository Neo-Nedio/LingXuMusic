package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.mine.SongCommentViewModel
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

    // 返回键处理
    BackHandler(true) {
        NavController.instance.popBackStack()
        MusicPlayController.playMusicSheetOffset = 0 // 重置播放器偏移量
    }


    val state = rememberCollapsingToolbarScaffoldState()
    CollapsingToolbarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background),
        state = state,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed, // 折叠后退出
        toolbar = {
            ScrollHeader(song, state.toolbarState)
        }
    ) {
        Body(song)
    }
}

@Composable
private fun CollapsingToolbarScope.ScrollHeader(song: Song, toolbarState: CollapsingToolbarState) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val maxHeight = statusBarTop + (88 + 150 + 100 + 20).cdp // 动态计算头部高度
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight)
            .parallax(1f) // 正常速度滚动
    ) {
        SongInfoComponent(song)   // 歌曲信息
        StickyHeader()            // 评论区标题 + Tab
    }

    //没有parallax，不滚动
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(statusBarTop) // 高度 = 状态栏高度
                .background(AppColorsProvider.current.background)
        )
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.cdp),  // 高度 = 88dp
            title = "评论",
            titleAlign = TextAlign.Start,
            leftClick = {
                NavController.instance.popBackStack()
                MusicPlayController.playMusicSheetOffset = 0
            },
        )
        Box(modifier = Modifier // 高度 = 100dp
            .fillMaxWidth()
            .height(100.cdp))
    }
}

@Composable
private fun Body(song: Song) {
    val viewModel: SongCommentViewModel = viewModel()

    //选择变化时，重新加载数据
    LaunchedEffect(viewModel.curSelectedTabType) {
        viewModel.buildNewCommentListPager(song, viewModel.curSelectedTabType)
    }
    if (viewModel.commentBeanListFlows[viewModel.curSelectedTabType] != null) {
        val commentBeanList = viewModel.commentBeanListFlows[viewModel.curSelectedTabType]!!.collectAsLazyPagingItems()
        ViewStateListPagingComponent(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorsProvider.current.background),
            viewStateComponentModifier = Modifier
                .fillMaxSize()
                .background(AppColorsProvider.current.background),
            enableRefresh = false, //不允许下拉刷新
            collectAsLazyPagingItems = commentBeanList,
        ) {
            items(count = commentBeanList.itemCount) { index ->
                commentBeanList[index]?.let {
                    Text(
                        text = "${it.content}", modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.cdp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SongInfoComponent(song: Song) {
    val name = song.singerinfo?.firstOrNull()?.name.orEmpty()
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
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AppColorsProvider.current.firstText, fontSize = 36.csp)) {
                        append(songName)
                    }
                    withStyle(style = SpanStyle(color = AppColorsProvider.current.secondText, fontSize = 32.csp)) {
                        append(" - $singer")
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
private fun StickyHeader() {
    val viewModel: SongCommentViewModel = viewModel()
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            viewModel.commentSortTabs.forEachIndexed { index, item ->
                Text(
                    modifier = Modifier
                        .width(100.cdp)
                        .clickable(
                            //// 提供交互源
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null //移除点击时的默认涟漪效果
                        ) {
                            viewModel.curSelectedTabType = item.type // 切换 Tab
                        },
                    textAlign = TextAlign.Center,
                    text = item.title,
                    color = if (item.type == viewModel.curSelectedTabType) {
                        AppColorsProvider.current.firstText
                    } else {
                        AppColorsProvider.current.secondText
                    },
                    fontSize = 28.csp,
                    fontWeight = if (item.type == viewModel.curSelectedTabType) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
                // 分割线（最后一个不显示）
                if (index != viewModel.commentSortTabs.size - 1) {
                    VerticalDivider(
                        modifier = Modifier
                            .width(2.cdp)
                            .height(30.cdp),
                        color = AppColorsProvider.current.divider
                    )
                }
            }
        }
    }
}
