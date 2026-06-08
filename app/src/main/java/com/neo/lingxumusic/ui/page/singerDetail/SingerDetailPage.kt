package com.neo.lingxumusic.ui.page.singerDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.R
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerHeader
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerHomeContent
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerSelectionBottomBar
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerSongsContent
import com.neo.lingxumusic.ui.page.playList.AddToPlaylistPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.singerDetail.SingerDetailViewModel

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

    // 当前选中的 Tab 索引
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab 标题（主标题 + 小字数量）
    val tabTexts = listOf(
        "主页",
        buildTabText("单曲", artistDetail.song_count),
        buildTabText("专辑", artistDetail.album_count),
        buildTabText("MV", artistDetail.mv_count)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // 导航栏（固定在最顶部，不重叠）
        CommonTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = artistDetail.author_name ?: "",
            backgroundColor = colors.background,
            contentColor = colors.firstText,
            leftIconResId = R.drawable.ic_back,
            leftClick = { NavController.instance.popBackStack() }
        )

        // 歌手头部（固定，不滚动）
        SingerHeader(
            avatarUrl = artistDetail.sizable_avatar?.replaceSize(),
            singerName = artistDetail.author_name,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.cdp)
        )

        // Tab 导航栏
        CommonTabLayout(
            selectedIndex = selectedTabIndex,
            tabTexts = tabTexts,
            style = CommonTabLayoutStyle(
                isScrollable = false,
                selectedTextSize = 32.csp,
                unselectedTextSize = 32.csp,
                selectedTextBold = true,
                unselectedTextBold = false,
                indicatorHeight = 0.cdp
            ),
            backgroundColor = colors.background,
            selectedTextColor = colors.primary,
            unselectedTextColor = colors.secondText,
            onTabSelected = { selectedTabIndex = it }
        )

        Spacer(modifier = Modifier.height(16.cdp))

        // 内容区域：weight(1f) 提供有限高度，
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> SingerHomeContent(artistDetail)
                1 -> SingerSongsContent(singerId = viewModel.artistDetail?.author_id?.toLongOrNull() ?: 0L,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> SingerAlbumsContent()
                3 -> SingerMvContent()
            }
        }

        // 选择模式底部栏
        if (viewModel.isSelectionMode) {
            SingerSelectionBottomBar()
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
private fun SingerAlbumsContent() {
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

@Composable
private fun SingerMvContent() {
    val colors = AppColorsProvider.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.cdp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "MV列表", color = colors.thirdText, fontSize = 28.csp)
    }
}

private fun buildTabText(title: String, count: Int): String {
    return "$title ${StringUtil.friendlyNumber(count)}"
}
