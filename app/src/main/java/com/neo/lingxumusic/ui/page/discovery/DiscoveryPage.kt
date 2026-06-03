package com.neo.lingxumusic.ui.page.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.discovery.component.RankPage
import com.neo.lingxumusic.ui.page.discovery.component.RecommendPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import kotlinx.coroutines.launch

private val discoveryTabs = listOf("推荐", "排行榜", "分类")
private val tabLayoutHeight = 88.cdp

@Composable
fun DiscoveryPage(onToggleDrawer: () -> Unit) {
    Column(
        Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        CommonTopAppBar(
            title = "发现",
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = onToggleDrawer,
            rightIconResId = R.drawable.ic_search,
            rightClick = {}
        )

        Body(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Body(modifier: Modifier = Modifier) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { discoveryTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
    ) {
        CommonTabLayout(
            selectedIndex = selectedTabIndex,
            tabTexts = discoveryTabs,
            backgroundColor = AppColorsProvider.current.background,
            style = CommonTabLayoutStyle(
                isScrollable = false,
                indicatorPaddingBottom = 18.cdp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tabLayoutHeight)
                    .background(AppColorsProvider.current.background),
                tabItemDrawBehindBlock = { position ->
                    if (position != discoveryTabs.size - 1) {
                        drawLine(
                            Color.LightGray,
                            Offset(size.width, size.height * 0.3f),
                            Offset(size.width, size.height * 0.7f),
                            strokeWidth = 2.cdp.toPx()
                        )
                    }
                }
            )
        ) { index ->
            selectedTabIndex = index
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> RecommendPage()
                1 -> RankPage()
                2 -> CategoryPage()
            }
        }
    }
}

@Composable
private fun CategoryPage() {
    DiscoveryPlaceholderPage(title = "分类")
}

@Composable
private fun DiscoveryPlaceholderPage(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 32.csp,
            color = AppColorsProvider.current.secondText
        )
    }
}
