package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.LifeCycleObserverComponent
import com.neo.lingxumusic.ui.page.playMusic.DISK_ROTATE_ANIM_CYCLE
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.playMusic.PlayMusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun DiskPagers() {
    DiskRoundBackground()  // 底层：半透明圆形背景
    DiskPager()  // 中层：唱片轮播
    DiskNeedle()           // 顶层：唱针
}

// 半透明圆形背景
@Composable
private fun DiskRoundBackground() {
    Box(
        modifier = Modifier
            .padding(top = 100.dp)
            .width(274.dp)
            .height(274.dp)
            .clip(CircleShape)
            .background(Color(0x55EEEEEE))
    )
}

//唱针
@Composable
private fun DiskNeedle() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    // 唱针旋转角度动画
    val needleRotateAnim by animateFloatAsState(
        targetValue = if (viewModel.sheetNeedleUp) -25f else 0f,  // 抬起 = -25°，落下 = 0°
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)  // 200ms 线性动画
    )
    Image(
        painter = painterResource(id = R.drawable.ic_play_neddle),
        contentDescription = "needle",
        modifier = Modifier
            .padding(start = 70.dp)
            .width(110.dp)
            .height(167.dp)
            .graphicsLayer(
                rotationZ = needleRotateAnim,                     // 旋转角度
                transformOrigin = TransformOrigin(0.164f, 0.109f) // 旋转中心点
            )
    )
}

//唱片轮播
private var onStopBefore = false //记录页面是否曾经停止过，用于恢复时判断是否需要重新启动动画

@Composable
private fun DiskPager() {
    val pagerState = rememberPagerState(
        initialPage = MusicPlayController.curIndex,  // 从当前播放歌曲开始
        pageCount = { MusicPlayController.songList.size }  // 总页数 = 歌单数量
    )
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    //记录值，当歌曲索引curIndex变化时会引起pagerState变化，但是当pagerState变化时，会使用play逻辑
    //索引curIndex变化时内部已经处理了音乐播放逻辑，此时引起的pagerState动画会导致其内再次play造成冲突
    //因此在引curIndex变化时会引起pagerState变化时，suppressPagerSync为true，让pagerState监听不起作用，防止冲突
    var suppressPagerSync by remember { mutableStateOf(false) }

    //通过生命周期控制，当页面不存在时后台不要转动，节省资源
    LifeCycleObserverComponent(lifeCycleListener = object : ComposeLifeCycleListener {
        //页面恢复时重新启动
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (onStopBefore) {
                onStopBefore = false
                coroutineScope.launch {
                    delay(300) // 等待页面完全可见
                    controlSheetNeedleAndDiskAnim(viewModel)
                }
            }
        }

        //页面不可见时停止唱片旋转动画，防止浪费资源
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            onStopBefore = true
            coroutineScope.launch {
                viewModel.lastSheetDiskRotateAngleForSnap = 0f // 重置角度
                viewModel.sheetDiskRotate.snapTo(0f) // 立即设置到 0°
                viewModel.sheetDiskRotate.stop()  // 停止旋转
            }
        }
    }) {
        //播放状态监听
        LaunchedEffect(MusicPlayController.isPlaying()) {
            controlSheetNeedleAndDiskAnim(viewModel)
        }

        //外部切歌时同步 UI
        LaunchedEffect(MusicPlayController.curIndex) {
            if (MusicPlayController.curIndex != -1 &&
                MusicPlayController.curIndex != pagerState.currentPage
            ) {
                // 1. 重置旋转角度
                viewModel.lastSheetDiskRotateAngleForSnap = 0f
                viewModel.sheetDiskRotate.snapTo(viewModel.lastSheetDiskRotateAngleForSnap)

                // 2. 滚动 Pager 到目标页
                suppressPagerSync = true
                if (abs(MusicPlayController.curIndex - pagerState.currentPage) > 1) {
                    pagerState.scrollToPage(MusicPlayController.curIndex)  // 无动画跳转
                } else {
                    pagerState.animateScrollToPage(                         // 动画滚动
                        MusicPlayController.curIndex,
                        animationSpec = tween(400)
                    )
                }
                suppressPagerSync = false
            }
        }

        //用户滑动时同步数据
        LaunchedEffect(pagerState.settledPage) {
            if (!suppressPagerSync && MusicPlayController.curIndex != pagerState.settledPage) {
                viewModel.lastSheetDiskRotateAngleForSnap = 0f
                viewModel.sheetDiskRotate.snapTo(viewModel.lastSheetDiskRotateAngleForSnap)
                MusicPlayController.play(pagerState.settledPage)
            }
        }

        //页面
        HorizontalPager(
            modifier = Modifier
                .padding(top = 100.dp)
                .fillMaxWidth()
                .height(274.dp),
            state = pagerState,
        ) { position ->
            DiskItem(MusicPlayController.songList[position])
        }
    }
}

//播放状态监听
private suspend fun controlSheetNeedleAndDiskAnim(viewModel: PlayMusicViewModel) {
    if (MusicPlayController.isPlaying()) {
        viewModel.sheetNeedleUp = false  // 唱针落下
        viewModel.sheetDiskRotate.stop()
        viewModel.sheetDiskRotate.snapTo(viewModel.lastSheetDiskRotateAngleForSnap) // 从上次角度开始
        viewModel.sheetDiskRotate.animateTo(
            targetValue = 360f + viewModel.lastSheetDiskRotateAngleForSnap,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = DISK_ROTATE_ANIM_CYCLE, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        viewModel.sheetNeedleUp = true // 唱针抬起
    }
}

@Composable
private fun DiskItem(song: Song) {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null  // 设置为 null 取消涟漪效果
            ) {
                viewModel.showLyric = !viewModel.showLyric
            }
            //手势检测详解
            .pointerInput(Unit) {
                awaitEachGesture { // 等待一个完整的手势序列（按下→移动→松开）
                    while (true) { //循环等待事件
                        // 等待下一个触摸事件
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        // 只处理单指触摸
                        if (event.changes.size == 1) {
                            val pointer = event.changes[0]
                            if (pointer.pressed) { //按压
                                viewModel.sheetNeedleUp = true // 抬起唱针
                            } else { //松开处理
                                scope.launch {
                                    delay(400)
                                    if (MusicPlayController.isPlaying()) {
                                        viewModel.sheetNeedleUp = false  // 落下唱针
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            }
            .graphicsLayer {
                rotationZ =
                        // 当前播放的歌曲 → 旋转
                    if (MusicPlayController.isPlaying(song)) {
                        viewModel.sheetDiskRotate.value
                    } else {
                        0f
                    }
            },
        contentAlignment = Alignment.Center
    ) {
        //背景圆环
        CommonLocalImage(
            R.drawable.ic_disc_background,
            modifier = Modifier
                .width(270.dp)
                .height(270.dp)
        )

        //旋转图片
        CommonNetworkImage(
            url = song.cover?.replaceSize(),
            modifier = Modifier
                .width(180.dp)
                .height(180.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = CircleShape
                ),
            placeholder = R.drawable.ic_default_disk_cover,
            error = R.drawable.ic_default_disk_cover,
        )
    }
}

