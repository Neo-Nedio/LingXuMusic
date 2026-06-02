package com.neo.lingxumusic.ui.page.brush.component

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.VideoPlayController
import com.neo.lingxumusic.core.VideoPlayerState
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.displayCover
import com.neo.lingxumusic.model.displayPlayUrl
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.SeekBar
import com.neo.lingxumusic.utils.ScreenUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.viewmodel.brush.BrushViewModel
import kotlinx.coroutines.launch

//视频播放页面（单个视频播放器，支持上下滑动切换视频）
@Composable
fun BrushVideoPlay(
    index: Int,                          // 当前视频索引
    lazyListState: LazyListState,        // 列表状态，用于滑动切换
    video: BrushVideo,                   // 视频数据
    itemCount: Int,                      // 视频总数
    onSwitchVideo: (Int) -> Unit,        // 切换视频回调
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .videoDragDetect(index, lazyListState, itemCount, onSwitchVideo) // 添加上下滑动手势
        ) {
            VideoSurface(index, video) // 视频 Surface 渲染组件
        }

        // 视频信息（标题、作者等）
        BrushVideoInfo(video)

        // 视频进度条
        VideoSeekBar()
    }
}

// 上滑/下滑 切换视频的手势检测扩展函数
private fun Modifier.videoDragDetect(
    curIndex: Int,                       // 当前视频索引
    lazyListState: LazyListState,        // 列表状态
    itemCount: Int,                      // 总数量
    onSwitchVideo: (Int) -> Unit,        // 切换视频回调
) = composed {
    val viewModel: BrushViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    var totalDragAmount = remember { 0f }                        // 累计拖拽距离
    val threshold = remember { ScreenUtil.getScreenHeight() / 8f }  // 切换阈值（屏幕高度 / 8）

    this.pointerInput(curIndex, itemCount) {
        detectVerticalDragGestures(
            onDragStart = {
                totalDragAmount = 0f  // 开始拖拽，重置累计距离
            },
            onDragEnd = {
                // 拖拽结束，判断是否需要切换视频
                var newIndex = curIndex
                if (totalDragAmount < 0) {
                    // 向上滑（负值）→ 下一个
                    if (totalDragAmount < -threshold) {
                        if (curIndex >= itemCount - 1) {
                            viewModel.loadMoreBrushVideo(
                                onSameData = {
                                    showToast("暂时没有更多视频了，请稍后再试")
                                },
                                onAdded = { startIndex ->
                                    onSwitchVideo(startIndex)
                                    scope.launch {
                                        lazyListState.animateScrollToItem(startIndex)
                                    }
                                },
                            )
                        } else {
                            newIndex = curIndex + 1
                            onSwitchVideo(newIndex)
                        }
                    }
                } else if (totalDragAmount > threshold) {
                    // 向下滑（正值）→ 上一个
                    if (curIndex == 0) {
                        showToast("已经是第一个视频了")
                    } else {
                        newIndex = curIndex - 1
                        onSwitchVideo(newIndex)
                    }
                }
                // 滚动列表到新位置
                scope.launch {
                    lazyListState.animateScrollToItem(newIndex)
                }
            }
        ) { _, dragAmount ->
            totalDragAmount += dragAmount              // 累加拖拽距离
            lazyListState.dispatchRawDelta(-dragAmount)  // 同步滚动列表
        }
    }
}

// 视频 Surface 渲染组件
@Composable
private fun VideoSurface(index: Int, video: BrushVideo) {
    val playUrl = video.displayPlayUrl                           // 视频播放地址
    val isCurrentVideo = VideoPlayController.curVideoIndex == index  // 是否是当前播放的视频

    Box(modifier = Modifier.fillMaxSize()) {
        // 视频封面图（加载中或未播放时显示）
        CommonNetworkImage(
            url = video.displayCover,
            modifier = Modifier.fillMaxSize(),
            placeholder = R.drawable.ic_default_placeholder_video,
            error = R.drawable.ic_default_placeholder_video
        )

        // 当前视频且有播放地址 → 显示视频渲染器
        if (isCurrentVideo && !playUrl.isNullOrBlank()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        // 点击视频：播放/暂停切换
                        if (VideoPlayController.videoPlaying) {
                            VideoPlayController.pauseVideo()
                        } else {
                            VideoPlayController.resumeVideo()
                        }
                    },
                // 创建 Android 原生 TextureView 用于视频渲染
                factory = { context ->
                    TextureView(context).apply {
                        // 设置 SurfaceTexture 监听器
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            // Surface 可用时调用（视图已创建，可以开始渲染）
                            override fun onSurfaceTextureAvailable(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                // 将 Surface 绑定到 VideoPlayController，视频将渲染到这个 Surface 上
                                VideoPlayController.attachSurface(Surface(surfaceTexture))
                            }
                            // Surface 尺寸变化时调用（如屏幕旋转
                            override fun onSurfaceTextureSizeChanged(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) = Unit
                            // Surface 销毁时调用
                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                // 从 VideoPlayController 解绑 Surface
                                VideoPlayController.detachSurface(Surface(surfaceTexture))
                                return true  // 返回 true 表示已释放资源
                            }
                            // Surface 内容更新时调用（每帧都会调用）
                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                        }
                    }
                }
            )

            // 播放器未就绪 或 未在播放时，显示播放按钮图标
            if (VideoPlayController.playStatus != VideoPlayerState.READY || !VideoPlayController.videoPlaying) {
                CommonIcon(
                    resId = R.drawable.ic_video_play,                      // 播放图标资源
                    modifier = Modifier
                        .size(100.cdp)                                      // 图标大小 100dp
                        .align(Alignment.Center),                          // 居中显示
                    tint = Color.White                                      // 图标颜色白色
                )
            }
        }
    }
}

// 视频进度条组件（显示在视频底部）
@Composable
private fun BoxScope.VideoSeekBar() {
    SeekBar(
        progress = VideoPlayController.videoProgress,           // 当前播放进度（0-100）
        seeking = { VideoPlayController.onSeeking(it) },        // 拖动进度条时回调
        seekTo = { VideoPlayController.seekTo(it) },            // 拖动结束回调
        progressHeight = 2f,                                    // 进度条高度 2px
        progressColor = Color.White.copy(0.3f),                 // 进度条颜色（半透明白色）
        circleColor = Color.LightGray,                          // 拖拽圆点颜色（浅灰色）
        modifier = Modifier
            .fillMaxWidth()
            .height(seekBarTouchHeight)
            .align(Alignment.BottomCenter)
    )
}
