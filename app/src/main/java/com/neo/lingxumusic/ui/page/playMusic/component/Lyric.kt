package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import kotlin.math.abs
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.utils.transformDp
import com.neo.lingxumusic.viewmodel.playMusic.LyricModel
import com.neo.lingxumusic.viewmodel.playMusic.LyricWordModel
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

    //滚动阈值
    val scrollThresholdPx = 150.cdp.toPx
    var seekReleased by remember { mutableStateOf(false) }

    // 监听 isSeeking true→false，下次索引变化时强制滚动
    LaunchedEffect(Unit) {
        var wasSeeking = MusicPlayController.isSeeking
        snapshotFlow { MusicPlayController.isSeeking }.collect { seeking ->
            if (wasSeeking && !seeking) seekReleased = true
            wasSeeking = seeking
        }
    }

    //歌词索引变化，自动滚动
    LaunchedEffect(viewModel.curLyricIndex) {
        val index = viewModel.curLyricIndex
        if (index < 0) return@LaunchedEffect

        //进度条移动时，不管屏幕所在位置位置，强制移动到歌词的位置
        if (seekReleased) {
            lazyListState.animateScrollToItem(index)
            seekReleased = false
            return@LaunchedEffect
        }

        //计算lazyColumn区域中心点
        val layoutInfo = lazyListState.layoutInfo
        //viewportStartOffset	可视区域顶部相对于列表内容的位置（像素）
        //viewportEndOffset	可视区域底部相对于列表内容的位置（像素）
        //viewportCenter	可视区域的中心点位置
        val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f

        // 当前句或上一句仍在中心 ±150cdp 内时才跟随，避免用户手动滑动后被拉回
        val shouldScroll = layoutInfo.visibleItemsInfo.any { item ->
            item.index in maxOf(0, index - 1)..index &&
                abs(item.offset + item.size / 2f - viewportCenter) <= scrollThresholdPx
        }
        if (shouldScroll) {
            lazyListState.animateScrollToItem(index)
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
    val activeColor = AppColorsProvider.current.primary
    val inactiveColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.cdp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val words = lyricModel.words //一行歌词的每个字
        if (!words.isNullOrEmpty()) {
            //再取出完整的一行歌词
            val text = words.joinToString("") { it.text }
            val textStyle = TextStyle(
                fontSize = 32.csp,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        MusicPlayController
                            .seekToPosition(
                                lyricModel.words?.firstOrNull()?.startTime?.toInt()
                                    ?: lyricModel.time.toInt()
                            )
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    //如果不是当前播放的歌词，显示白色
                    index < viewModel.curLyricIndex -> {
                        Text(text = text, color = inactiveColor, style = textStyle)
                    }
                    index > viewModel.curLyricIndex -> {
                        Text(text = text, color = inactiveColor, style = textStyle)
                    }
                    //当前播放的歌词
                    else -> {
                        val endFraction = animatedBrushEndFraction(
                            words, //拆分的一行歌词（包含参数）
                            lyricModel.time,
                            viewModel.curPlayPosition,
                        )
                        Text(
                            text = text, //完整的一行歌词
                            style = textStyle.merge(
                                TextStyle(
                                    brush = Brush.horizontalGradient(
                                        colorStops = arrayOf(
                                            0f to activeColor,           // 起点：高亮色
                                            endFraction to activeColor,  // 终点前：高亮色
                                            endFraction to inactiveColor,// 终点后：白色
                                            1f to inactiveColor          // 结尾：白色
                                        ),
                                    ),
                                ),
                            ),
                        )
                    }
                }
            }
        } else {
            //如果无法得到一行的每个字，就按原来的一行行显示
            lyricModel.lyric?.let {
                Text(
                    text = it,
                    fontSize = 32.csp,
                    fontWeight = FontWeight.Medium,
                    color = if (viewModel.curLyricIndex == index) {
                        activeColor
                    } else {
                        inactiveColor
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

//计算渐变终点
@Composable
private fun animatedBrushEndFraction(
    words: List<LyricWordModel>,
    lineStartTime: Long,
    playPosition: Int,
): Float {
    //累加每个字的长度
    val totalChars = words.sumOf { it.text.length }.coerceAtLeast(1)
    var fraction = 0f
    for (word in words) {
        // 计算每个字的权重(字的长度占总字数的比例)
        val weight = word.text.length.toFloat() / totalChars
        //权重 * 动画值(这样当一个字的动画值到1后，只是根据权重沾满相应的字)
        fraction += weight * animatedWordProgress(word, lineStartTime, playPosition)
    }
    //限制返回值范围
    return fraction.coerceIn(0f, 1f)
}

/**
 * 单字：delay = startTime - 整句 startTime，duration = 该字 duration。
 * 行切入时用当前 playPosition 校准，避免 UI 切换晚于音频导致字幕滞后。
 */
@Composable
private fun animatedWordProgress(
    word: LyricWordModel, //单个字的数据（文字、开始时间、持续时间）
    lineStartTime: Long, //整行歌词的开始时间（毫秒）
    playPosition: Int, //当前播放位置（毫秒）
): Float {
    // 动画进度值，每个字独立，当字开始时间变化时重新创建
    val progress = remember(word.startTime) { Animatable(0f) }
    // 锁定进入当前行时的播放位置，避免因 playPosition 持续更新而反复触发动画
    //用户可能不是在整行开始的瞬间进入，可能是在播放过程中才打开歌词页面。entryPosition 锁定了进入时刻，以此作为动画时间轴的起点，确保动画与当前播放位置同步
    val entryPosition = remember { mutableIntStateOf(playPosition) }
    // 该字的结束时间 = 开始时间 + 持续时间
    val endTime = word.startTime + word.duration

    //切换到不同行、或同一行的不同字时 ， 切换到不同行时    执行
    LaunchedEffect(word.startTime, lineStartTime) {
        //当前播放位置
        val anchor = entryPosition.intValue
        when {
            // 情况1：已经错过了这个字
            // 条件：进入时的位置 ≥ 该字的结束时间
            // 处理：直接跳到 100% 完成，无动画
            anchor >= endTime -> progress.snapTo(1f)
            // 情况2：在该字的播放区间中间进入
            // 条件：进入时的位置在该字的时间范围内
            // 处理：从当前进度开始，动画到 100%
            anchor >= word.startTime -> {
                // 计算当前进度（0f 到 1f）
                val start = ((anchor - word.startTime).toFloat() / word.duration)
                    .coerceIn(0f, 1f)
                progress.snapTo(start)// 立即跳转到当前进度
                //执行动画到对应位置
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        // 剩余时间
                        durationMillis = (endTime - anchor).toInt().coerceAtLeast(1),
                        easing = LinearEasing,
                    ),
                )
            }
            // 情况3：正常播放（还没到这个字）
            // 条件：进入时的位置在该字开始时间之前
            // 处理：延迟等待后，从 0% 动画到 100%
            else -> {
                progress.snapTo(0f) // 起始进度为 0
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        // 字的持续时间
                        durationMillis = word.duration.coerceAtLeast(1),
                        // 等待时间
                        delayMillis = (word.startTime - anchor).toInt().coerceAtLeast(0),
                        easing = LinearEasing,
                    ),
                )
            }
        }
    }
    return progress.value
}
