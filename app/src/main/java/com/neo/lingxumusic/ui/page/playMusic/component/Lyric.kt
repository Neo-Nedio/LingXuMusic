package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.transformDp
import com.neo.lingxumusic.viewmodel.playMusic.LyricModel
import com.neo.lingxumusic.viewmodel.playMusic.PlayMusicViewModel

@Composable
fun Lyric() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false) }
            .apply { targetState = viewModel.showLyric },
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var lyricHeight  by remember { mutableIntStateOf(0) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    lyricHeight = it.size.height
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.showLyric = !viewModel.showLyric
                },
            contentAlignment = Alignment.Center
        ) {
            ViewStateComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 50.cdp),
                viewStateLiveData = viewModel.lyricResult,
                customLoadingComponent = {
                    ViewStateTip("加载歌词中...")
                },
                customEmptyComponent = {
                    ViewStateTip("暂无歌词")
                },
                customFailComponent = {
                    ViewStateTip("加载歌词出错, 点击重试", enableRetry = true)
                },
                customErrorComponent = {
                    ViewStateTip("加载歌词出错, 点击重试", enableRetry = true)
                }
            ) {
                LyricList(lyricHeight)
            }
        }
    }
}

//自定义状态页面
@Composable
private fun ViewStateTip(tip: String, enableRetry: Boolean = false) {
    val viewModel: PlayMusicViewModel = hiltViewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                enabled = enableRetry,
                //无涟漪效果
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enableRetry) {
                    viewModel.getLyric(MusicPlayController.songList[MusicPlayController.curIndex])
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = tip, color = Color.White, fontSize = 30.csp)
    }
}

@Composable
private fun LyricList( lyricHeight: Int) {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val lazyListState = rememberLazyListState()

    //歌词索引变化，自动滚动
    LaunchedEffect(viewModel.curLyricIndex) {
        if (viewModel.curLyricIndex >= 0) {
            lazyListState.animateScrollToItem(
                viewModel.curLyricIndex,
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                val paint = Paint().asFrameworkPaint()
                drawIntoCanvas {
                    //创建一个临时画布层，在层上绘制内容，最后 restore 把结果合并到主画布
                    val layerId = it.nativeCanvas.saveLayer(
                        0f,
                        0f,
                        size.width,
                        size.height,
                        paint
                    )
                    drawContent()
                    drawRect(
                        //垂直绘画，中间白色，两端透明
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,   // 顶部（y=0%）：完全透明
                            0.15f to Color.White,       // y=15%：完全不透明
                            0.85f to Color.White,       // y=85%：完全不透明
                            1f to Color.Transparent     // 底部（y=100%）：完全透明
                        ),
                        blendMode = BlendMode.DstIn //保留目标中与源重叠的部分，且使用源的透明度
                    )
                    // 合并结果
                    it.nativeCanvas.restoreToCount(layerId)
                }
            },
        state = lazyListState,
        contentPadding = PaddingValues(vertical = (lyricHeight * 0.4).transformDp)
    ) {
        itemsIndexed(viewModel.lyricModelList) { index, item ->
            LyricItem(index, item, viewModel)
        }
    }
}

//歌词
@Composable
private fun LyricItem(index: Int, lyricModel: LyricModel, viewModel: PlayMusicViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.cdp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        lyricModel.lyric?.let {
            Text(
                text = it,
                fontSize = 32.csp,
                fontWeight = FontWeight.Medium,
                color = if (viewModel.curLyricIndex == index) {
                    AppColorsProvider.current.primary
                } else {
                    Color.White
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
