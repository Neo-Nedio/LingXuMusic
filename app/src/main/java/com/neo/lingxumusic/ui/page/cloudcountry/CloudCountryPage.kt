package com.neo.lingxumusic.ui.page.cloudcountry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.VideoPlayController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.displayPlayUrl
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.cloudcountry.component.BrushVideoPlay
import com.neo.lingxumusic.viewmodel.cloudcountry.CloudCountryViewModel

@Composable
fun CloudCountryPage(onToggleDrawer: () -> Unit) {
    val viewModel: CloudCountryViewModel = hiltViewModel()
    val context = LocalContext.current

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
            .background(Color.Black)
    ) {
        // 状态组件：处理加载中、空数据、失败、成功状态
        ViewStateComponent(
            viewStateLiveData = viewModel.brushVideoResult,
            loadDataBlock = { viewModel.loadBrushVideo() },
            specialRetryBlock = { viewModel.loadBrushVideo() },
            lifeCycleListener = object : ComposeLifeCycleListener {
                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    VideoPlayController.pauseVideo() // 页面不可见时暂停视频
                }

                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    VideoPlayController.resumeVideo() // 页面可见时恢复播放
                }
            },
            viewStateComponentModifier = Modifier.fillMaxSize(),
        ) {
            // 数据加载成功后显示视频列表
            BrushVideoList()
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

@Composable
private fun BrushVideoList() {
    val viewModel: CloudCountryViewModel = hiltViewModel()
    val videoList = viewModel.videoList          // 视频列表数据
    val itemCount = videoList.size               // 视频总数
    val lazyListState = rememberLazyListState()  // 列表滚动状态

    // 当 curVideoUrl 变化时，自动加载视频
    LaunchedEffect(VideoPlayController.curVideoUrl) {
        VideoPlayController.curVideoUrl?.let { VideoPlayController.loadVideo(it) }
    }

    // 首次进入，自动播放第一个视频
    LaunchedEffect(itemCount) {
        if (VideoPlayController.curVideoIndex < 0 && itemCount > 0) {
            VideoPlayController.switchVideo(0, videoList[0].displayPlayUrl)
        }
    }

    // 懒加载列（禁用用户手动滚动，通过手势切换视频）
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        userScrollEnabled = false, // 禁止用户手动滚动，通过视频内部手势切换
    ) {
        items(
            count = itemCount,
            key = { index -> videoList[index].displayPlayUrl ?: index }, // 用播放地址作为唯一 key
        ) { index ->
            BrushVideoPlay(
                index = index,
                lazyListState = lazyListState,
                video = videoList[index],
                itemCount = itemCount,
                onSwitchVideo = { newIndex ->
                    // 切换到新视频
                    VideoPlayController.switchVideo(newIndex, videoList[newIndex].displayPlayUrl)
                },
                modifier = Modifier.fillParentMaxHeight(), // 每个 item 占满屏幕高度
            )
        }
    }
}
