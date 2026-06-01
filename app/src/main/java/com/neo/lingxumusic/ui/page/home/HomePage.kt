package com.neo.lingxumusic.ui.page.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.DrawerState
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.ui.common.BottomNavigationBar
import com.neo.lingxumusic.ui.common.BottomNavigationItem
import com.neo.lingxumusic.ui.page.cloudcountry.CloudCountryPage
import com.neo.lingxumusic.ui.page.discovery.DiscoveryPage
import com.neo.lingxumusic.ui.page.mine.MinePage
import com.neo.lingxumusic.ui.page.podcast.PodcastPage
import com.neo.lingxumusic.ui.page.sing.SingPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.TwoBackFinish
import kotlinx.coroutines.launch

private val bottomNavigationItems = listOf(
    BottomNavigationItem("发现", R.drawable.ic_discovery),
    BottomNavigationItem("播客", R.drawable.ic_podcast),
    BottomNavigationItem("我的", R.drawable.ic_mine),
    BottomNavigationItem("k歌", R.drawable.ic_sing),
    BottomNavigationItem("云村", R.drawable.ic_cloud_country),
)

var selectedHomeTabIndex by mutableIntStateOf(2)

@Composable
fun HomePage(scaffoldState: ScaffoldState, onFinish: () -> Unit = { }) {
    val scope = rememberCoroutineScope()

    BackHandler {
        if(scaffoldState.drawerState.isOpen) {
            scope.launch {
                scaffoldState.drawerState.close()
            }
        }else {
            if(MusicPlayController.showPlayMusicSheet) {
                MusicPlayController.showPlayMusicSheet = false //关闭播放页
                MusicPlayController.showBottomMusicPlay = true //打开底部播放器
            }else { //退出应用
                TwoBackFinish().execute(onFinish)
            }
        }
    }

    Body(scaffoldState.drawerState)
}

@Composable
private fun Body(drawerState: DrawerState) {
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(
            initialPage = selectedHomeTabIndex,
            pageCount = { bottomNavigationItems.size }
        )


        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true, // 允许用户水平滑动切换页面
            beyondViewportPageCount = bottomNavigationItems.size - 1, // 预加载页面数量
            modifier = Modifier
                .weight(1f) // 占满剩余空间
                .background(AppColorsProvider.current.background)
        ) { pagePosition ->
            selectedHomeTabIndex = pagerState.currentPage

            when (pagePosition) {
                0 -> DiscoveryPage(drawerState)    // 发现页
                1 -> PodcastPage(drawerState)      // 播客页
                2 -> MinePage(drawerState)         // 我的页
                3 -> SingPage(drawerState)         // K歌页
                4 -> CloudCountryPage(drawerState) // 云村页
            }
        }

        BottomNavigationBar(
            bottomNavigationItems,
            pagerState,
            selectedHomeTabIndex
        ) {
            selectedHomeTabIndex = it
        }
    }
}