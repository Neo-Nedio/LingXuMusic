package com.neo.lingxumusic.ui.page.cloudcountry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.VideoPlayController
import com.neo.lingxumusic.core.viewState.ViewStateListPagingComponent
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.displayPlayUrl
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.cloudcountry.component.BrushVideoPlay
import com.neo.lingxumusic.viewmodel.cloudcountry.CloudCountryViewModel

@Composable
fun CloudCountryPage(onToggleDrawer: () -> Unit) {
    val viewModel: CloudCountryViewModel = hiltViewModel()
    val context = LocalContext.current

    // 初始化视频分页数据（如果未初始化）
    if (viewModel.brushVideoFlow == null) {
        viewModel.buildBrushVideoPager()
    }

    // 页面进入时初始化视频播放器，退出时释放资源
    DisposableEffect(Unit) {
        VideoPlayController.initIfNeeded(context)
        onDispose {
            VideoPlayController.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)  // 黑色背景
    ) {
        viewModel.brushVideoFlow?.let { flow ->
            val brushVideoItems = flow.collectAsLazyPagingItems()  // 分页视频数据
            val lazyListState = rememberLazyListState()            // 列表滚动状态
            val itemCount = brushVideoItems.itemCount              // 视频总数

            // 当 curVideoUrl 变化时，自动加载视频
            LaunchedEffect(VideoPlayController.curVideoUrl) {
                VideoPlayController.curVideoUrl?.let { VideoPlayController.loadVideo(it) }
            }

            // 首次进入，自动播放第一个视频
            LaunchedEffect(itemCount) {
                if (VideoPlayController.curVideoIndex < 0 && itemCount > 0) {
                    val url = brushVideoItems[0]?.displayPlayUrl
                    VideoPlayController.switchVideo(0, url)
                }
            }

            // 通用列表分页组件（禁用刷新和底部 Footer，禁止用户滚动切换页面）
            ViewStateListPagingComponent(
                modifier = Modifier.fillMaxSize(),
                enableRefresh = false,
                showNoMoreDataFooter = false,
                collectAsLazyPagingItems = brushVideoItems,
                userScrollEnabled = false, // 禁止用户手动滚动列表
                lazyListState = lazyListState,
                // 生命周期监听
                lifeCycleListener = object : ComposeLifeCycleListener {
                    override fun onPause(owner: LifecycleOwner) {
                        super.onPause(owner)
                        VideoPlayController.pauseVideo() // 页面不可见时暂停视频
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        super.onResume(owner)
                        VideoPlayController.resumeVideo()  // 页面可见时恢复播放
                    }
                },
            ) {
                // 视频列表项（每个视频占满整个屏幕高度）
                items(brushVideoItems.itemCount) { index ->
                    brushVideoItems[index]?.let { video ->
                        BrushVideoPlay(
                            index = index,
                            lazyListState = lazyListState,
                            video = video,
                            itemCount = itemCount,
                            onSwitchVideo = { newIndex ->
                                // 切换视频
                                val url = brushVideoItems[newIndex]?.displayPlayUrl
                                VideoPlayController.switchVideo(newIndex, url)
                            },
                            modifier = Modifier.fillParentMaxHeight(), // 每个 item 占满屏幕高度
                        )
                    }
                }
            }
        }

        // 顶部导航栏（透明背景，白色文字）
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            title = "云村",
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = onToggleDrawer,
            backgroundColor = Color.Transparent,
            contentColor = Color.White,
        )
    }
}
