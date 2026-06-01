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
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.VideoPlayController
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.displayPlayUrl
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.LifeCycleObserverComponent
import com.neo.lingxumusic.ui.page.cloudcountry.component.BrushVideoPlay
import com.neo.lingxumusic.viewmodel.cloudcountry.CloudCountryViewModel

@Composable
fun CloudCountryPage(onToggleDrawer: () -> Unit) {
    val countryViewModel: CloudCountryViewModel = hiltViewModel()
    val context = LocalContext.current

    if (countryViewModel.brushVideoFlow == null) {
        countryViewModel.buildBrushVideoPager()
    }

    DisposableEffect(Unit) {
        VideoPlayController.initIfNeeded(context)
        onDispose {
            VideoPlayController.release()
        }
    }

    LifeCycleObserverComponent(lifeCycleListener = object : ComposeLifeCycleListener {
        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            VideoPlayController.pauseVideo()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            VideoPlayController.resumeVideo()
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            BrushVideoList()

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
}

@Composable
private fun BrushVideoList() {
    val countryViewModel: CloudCountryViewModel = hiltViewModel()
    val brushVideoItems = countryViewModel.brushVideoFlow?.collectAsLazyPagingItems()
    val itemCount = brushVideoItems?.itemCount ?: 0
    val lazyListState = rememberLazyListState()

    LaunchedEffect(VideoPlayController.curVideoUrl) {
        VideoPlayController.curVideoUrl?.let { VideoPlayController.loadVideo(it) }
    }

    LaunchedEffect(itemCount) {
        if (VideoPlayController.curVideoIndex < 0 && itemCount > 0) {
            val url = brushVideoItems?.get(0)?.displayPlayUrl
            VideoPlayController.switchVideo(0, url)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        userScrollEnabled = false,
    ) {
        brushVideoItems?.let { items ->
            items(items.itemCount) { index ->
                items[index]?.let { video ->
                    BrushVideoPlay(
                        index = index,
                        lazyListState = lazyListState,
                        video = video,
                        itemCount = itemCount,
                        onSwitchVideo = { newIndex ->
                            val url = items[newIndex]?.displayPlayUrl
                            VideoPlayController.switchVideo(newIndex, url)
                        },
                        modifier = Modifier.fillParentMaxHeight(),
                    )
                }
            }
        }
    }
}
