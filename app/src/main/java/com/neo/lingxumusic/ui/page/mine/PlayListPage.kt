package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun PlaylistPage(playlist: PlaylistBrief) {
    val scrollState = rememberScrollState()                      // 滚动状态
    val showPlayListTitle = scrollState.value >= 188.cdp.toPx   // 滚动超过188dp时显示标题

    //底部边距（当播放条出来时上移，可以看到所有音乐）
    val paddingBottom = if (showBottomMusicPlay) {
        BottomMusicPlayPadding
    } else {
        0.dp
    }

    val viewModel: PlayListViewModel = hiltViewModel()
    viewModel.playlist = playlist

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
            .padding(bottom = paddingBottom)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ScrollHeader(playlist)  // 头部
            Body()          // 歌曲列表
        }

        // 顶部导航栏（固定在顶部）
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.cdp),
            backgroundColor = if (showPlayListTitle) AppColorsProvider.current.background else Color.Transparent,
            title = if (showPlayListTitle) playlist.displayName() else "歌单",
            contentColor = Color.White,
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = { },
            rightIconResId = R.drawable.ic_search
        )
    }
}

@Composable
private fun ScrollHeader(playlist: PlaylistBrief) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(584.cdp)
    ) {
        // 背景层（带渐变和裁剪）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CommonHeadBackgroundShape())
                .background(brush = Brush.linearGradient(listOf(Color.Gray.copy(0.7f), Color.LightGray.copy(0.7f), Color.Gray.copy(0.7f))))
        ) {
            HeadBackground(playlist)  // 背景图片
            HeadPlayListInfo(
                modifier = Modifier, // 歌单信息（封面、名称、创建者）
                playlist
            )
        }
        // 底部按钮栏（播放、评论、分享）
        HeadCountInfoLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter) //位于底部
                .graphicsLayer { alpha = 1f },
            playlist
        )
    }
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
            .fillMaxSize()
    ) { data ->
        val detail = data.dataAs<PlaylistDetailData>()
        val songs = detail?.songs.orEmpty()

        Column {
            PlayListHeader( viewModel.playlist)
            HorizontalDivider(Modifier.fillMaxWidth(), thickness = 1.cdp, color = Color.LightGray)
            songs.forEachIndexed { index, item ->
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
                        showPlayMusicSheet = true
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
                showPlayMusicSheet = true
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