package com.neo.lingxumusic.ui.page.singerDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerHeader
import com.neo.lingxumusic.ui.page.singerDetail.component.SingerHomeContent
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

    val scrollState = rememberScrollState()

    // 导航栏透明度：根据滚动位置计算（0~400 范围内从透明到不透明）
    val navAlpha by remember {
        derivedStateOf {
            (scrollState.value / 400f).coerceIn(0f, 1f)
        }
    }

    // 当前选中的 Tab 索引
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab 标题（主标题 + 小字数量）
    val tabTexts = listOf(
        "主页",
        buildTabText("单曲", artistDetail.song_count),
        buildTabText("专辑", artistDetail.album_count),
        buildTabText("MV", artistDetail.mv_count)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 可滚动内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 歌手头部背景（头像 + 底部渐变 + 名字）
            SingerHeader(
                avatarUrl = artistDetail.sizable_avatar?.replaceSize(),
                singerName = artistDetail.author_name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.cdp)
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
                    indicatorHeight = 0.cdp // 不显示指示器
                ),
                backgroundColor = colors.background,
                selectedTextColor = colors.primary,
                unselectedTextColor = colors.secondText,
                onTabSelected = { selectedTabIndex = it }
            )

            // 内容区域
            when (selectedTabIndex) {
                0 -> SingerHomeContent(artistDetail)
                1 -> SingerSongsContent()
                2 -> SingerAlbumsContent()
                3 -> SingerMvContent()
            }

            Spacer(modifier = Modifier.height(100.cdp))
        }

        // 顶部导航栏（根据滚动逐渐显现）
        CommonTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .zIndex(1f),
            title = artistDetail.author_name ?: "",
            backgroundColor = colors.background.copy(alpha = navAlpha),
            contentColor = if (navAlpha > 0.5f) colors.firstText else Color.White,
            leftIconResId = R.drawable.ic_back,
            leftClick = { NavController.instance.popBackStack() }
        )
    }
}

/**
 * 构建 Tab 文本（主标题 + 小字数量）
 */
private fun buildTabText(title: String, count: Int): String {
    return "$title ${StringUtil.friendlyNumber(count)}"
}

@Composable
private fun SingerSongsContent() {
    val colors = AppColorsProvider.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.cdp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "单曲列表", color = colors.thirdText, fontSize = 28.csp)
    }
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
