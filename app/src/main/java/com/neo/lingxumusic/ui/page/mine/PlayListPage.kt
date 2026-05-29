package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.mine.component.BottomMusicPlayPadding
import com.neo.lingxumusic.ui.page.mine.component.SongItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.viewmodel.mine.PlayListViewModel
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun PlaylistPage(playlist: PlaylistBrief) {
    //底部边距（当播放条出来时上移，可以看到所有音乐）
    val paddingBottom = if (MusicPlayController.showBottomMusicPlay) {
        BottomMusicPlayPadding
    } else {
        0.dp
    }

    val viewModel: PlayListViewModel = hiltViewModel()
    viewModel.playlist = playlist

    //工具栏状态
    val state = rememberCollapsingToolbarScaffoldState()
    //标题切换阈值计算
    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.getTop(density)
    val showPlayListTitleThreshold =
        //state.toolbarState.progress：折叠进度（0=完全展开，1=完全折叠）
        (1 - state.toolbarState.progress) >= (statusBarTop + 188.cdp.toPx) / 584.cdp.toPx

    CollapsingToolbarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
            .padding(bottom = paddingBottom),
        state = state,  // 折叠状态
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,  // 滚动策略：折叠后退出
        toolbar = { // 可折叠的头部内容
            ScrollHeader(
                playlist,
                state,
                if (showPlayListTitleThreshold) playlist.displayName() else "歌单" // 动态标题
            )
        }
    ) {
        Body()          // 歌曲列表
    }
}

@Composable
//实现了可折叠头部的所有视觉效果
private fun CollapsingToolbarScope.ScrollHeader(
    playlist: PlaylistBrief,
    toolbarState: CollapsingToolbarScaffoldState,
    title: String,
) {
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
        // 底部按钮栏（播放、评论、分享）
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
        )
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
        leftIconResId = R.drawable.ic_drawer_toggle,
        leftClick = { },
        rightIconResId = R.drawable.ic_search
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
            Text(text = playlist.descriptionText(), color = Color.White, fontSize = 28.csp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

//底部按钮栏
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
            "播放(${StringUtil.friendlyNumber(playlist.playCountValue())})",
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
}


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

}


@Composable
private fun Body() {
    val viewModel: PlayListViewModel = hiltViewModel()

    ViewStateComponent(
        viewStateLiveData = viewModel.songDetailResult,
        loadDataBlock = {
            viewModel.getSongDetail()
        },
        viewStateComponentModifier = Modifier
            .fillMaxSize(),
        viewStateContentAlignment = BiasAlignment(0f, -0.6f)  // 内容向上偏移
    ) { data ->
        val detail = data.dataAs<PlaylistDetailData>()
        val songs = detail?.songs.orEmpty()

        //禁用 Android 原生的过度滚动效果（边缘发光/拉伸）
        //让滚动效果更干净
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            Column {
                PlayListHeader(viewModel.playlist)
                HorizontalDivider(Modifier.fillMaxWidth(), thickness = 1.cdp, color = Color.LightGray)
                LazyColumn {
                    itemsIndexed(songs) { index, item ->
                        SongItem(index, item) {
                            if (viewModel.songList[index].hash.isNullOrEmpty()) {
                                showToast("该歌曲暂不支持播放")
                            } else {
                                MusicPlayController.songList.clear()
                                //通过这个函数过滤所有无法播放的歌曲
                                MusicPlayController.setDataSource(
                                    viewModel.songList,
                                    viewModel.songList[index].hash
                                )
                                MusicPlayController.showBottomMusicPlay = false
                                MusicPlayController.showPlayMusicSheet = true
                            }
                        }
                    }
                }
            }
        }
    }
}

//歌曲列表的头部，显示"播放全部"按钮和歌曲数量
@Composable
private fun PlayListHeader(playlist: PlaylistBrief) {
    val viewModel: PlayListViewModel = hiltViewModel()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.cdp)
            .clickable {
                MusicPlayController.setDataSource(
                    viewModel.songList,
                    viewModel.songList.firstOrNull()?.hash
                )
                MusicPlayController.showBottomMusicPlay = false
                MusicPlayController.showPlayMusicSheet = true
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
            text = "(${playlist.count})",
            fontSize = 28.csp,
            color = AppColorsProvider.current.secondText,
        )
    }
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