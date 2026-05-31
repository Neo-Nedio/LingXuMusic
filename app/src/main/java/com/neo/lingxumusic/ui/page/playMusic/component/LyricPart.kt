package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.LyricUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.playMusic.LyricModel
import com.neo.lingxumusic.viewmodel.playMusic.PlayMusicViewModel
import dev.omkartenkale.explodable.Explodable
import dev.omkartenkale.explodable.ExplosionAnimationSpec
import dev.omkartenkale.explodable.rememberExplosionController
import kotlin.math.abs
import kotlin.math.min

@Composable
fun LyricPart() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    // 唱片页（未展开全屏歌词）时显示，位于唱片与底部控制区之间
    AnimatedVisibility(
        visible = !viewModel.showLyric,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 408.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            ViewStateComponent(
                modifier = Modifier.fillMaxWidth(),
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
                },
            ) {
                LyricPartList()
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
            .fillMaxWidth()
            .height(120.cdp)
            .clickable(
                enabled = enableRetry,
                //无涟漪效果
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (enableRetry) {
                    viewModel.getLyric(MusicPlayController.songList[MusicPlayController.curIndex])
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = tip, color = Color.White, fontSize = 24.csp)
    }
}

@Composable
private fun LyricPartList() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val lyricList = viewModel.lyricModelList      // 所有歌词
    val curIndex = viewModel.curLyricIndex        // 实际应该播放的歌词索引

    // 主句索引：静止时等于 curIndex；切句动画期间保持旧值，动画结束再对齐
    var displayedIndex by remember { mutableIntStateOf(-1) }  // 当前显示的歌词索引
    val animState = remember { Animatable(0f) }               // 动画进度 0→1
    val explosionController = rememberExplosionController()

    //如果暂停再播放，垂直索引和动画
    LaunchedEffect(MusicPlayController.isPlaying()) {
        if (!MusicPlayController.isPlaying()) {
            animState.stop()
            displayedIndex = curIndex
            animState.snapTo(0f)
            explosionController.reset()
        }
    }

    // 歌词切换动画开始播放时触发爆炸
    LaunchedEffect(animState.value > 0f) {
        explosionController.reset() //重置爆炸控制器
        //等待两个帧的时间，库内部需要先截取要爆炸的内容（截屏）
        withFrameNanos { }
        withFrameNanos { }
        //触发真正的爆炸动画
        explosionController.explode()

    }

    //主要在歌词索引变化时执行动画，行内歌词颜色变化时不会引起动画
    LaunchedEffect(curIndex, lyricList.size) {
        // 1. 无效情况：清除状态
        if (curIndex < 0 || lyricList.isEmpty()) {
            displayedIndex = -1
            animState.snapTo(0f)
            return@LaunchedEffect
        }
        // 2. 首次显示：直接显示当前歌词
        if (displayedIndex < 0) {
            displayedIndex = curIndex
            return@LaunchedEffect
        }
        // 3. 索引没变：不做任何事
        if (curIndex == displayedIndex) return@LaunchedEffect

        // 4. 非逐句切换（用户拖动进度条跳转）：直接切换，无动画
        if (abs(curIndex - displayedIndex) != 1) {
            displayedIndex = curIndex
            animState.snapTo(0f)
            return@LaunchedEffect
        }

        // 5. 逐句前进（正常播放）：播放切换动画
        val prevLyric = lyricList[displayedIndex]
        val nextLyric = lyricList[curIndex]
        val prevEndTime = if (prevLyric.duration > 0) {
            prevLyric.time + prevLyric.duration
        } else {
            lyricList.getOrNull(displayedIndex + 1)?.time ?: nextLyric.time
        }

        var animDuration = (nextLyric.time - prevEndTime).toInt().coerceAtLeast(1)
        animDuration = min(animDuration,500)
        animState.snapTo(0f)
        animState.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = animDuration
                0f at 0
                1f at animDuration
            },
        )

        displayedIndex = curIndex   // 动画完成后更新显示索引
        animState.snapTo(0f)        // 重置动画状态
        explosionController.reset()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.cdp), // 总高度 = 112dp + 68dp
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (curIndex < 0 || lyricList.isEmpty()) return@Column

        // LaunchedEffect 首帧前 displayedIndex 仍为 -1，用 curIndex 兜底
        val topIndex = displayedIndex.takeIf { it in lyricList.indices } ?: curIndex
        if (topIndex !in lyricList.indices) return@Column

        // 上方：正在播放（大字）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.cdp),
            contentAlignment = Alignment.Center,
        ) {
            //动画未播放（animState.value == 0f）
            if (animState.value == 0f) {
                LyricPartItem(
                    index = topIndex,
                    lyricModel = lyricList[topIndex],
                    viewModel = viewModel,
                    isMain = true,
                )
            } //动画播放中
            else if (displayedIndex in lyricList.indices) {
                // 当前句爆炸散开
                key(displayedIndex) { //当 displayedIndex 变化时，强制销毁旧的 Explodable 组件并创建一个全新的组件
                    Explodable(
                        modifier = Modifier.wrapContentSize(),
                        controller = explosionController,
                        animationSpec = ExplosionAnimationSpec(
                            shakeDurationMs = 0,
                            explosionDurationMs = 300,
                            explosionPower = 5f,
                        ),
                    ) {
                        LyricPartItem(
                            index = displayedIndex,
                            lyricModel = lyricList[displayedIndex],
                            viewModel = viewModel,
                            isMain = true,
                        )
                    }
                }
            }
        }

        // 下方：下一句（小字）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.cdp),
            contentAlignment = Alignment.Center,
        ) {
            //动画未播放
            if (animState.value == 0f) {
                val bottomIndex = (topIndex + 1).coerceAtMost(lyricList.lastIndex)
                if (bottomIndex in lyricList.indices && bottomIndex != topIndex) {
                    LyricPartItem(
                        index = bottomIndex,
                        lyricModel = lyricList[bottomIndex],
                        viewModel = viewModel,
                        isMain = false,
                    )
                }
            } //动画播放
            else if (displayedIndex in lyricList.indices) {
                val outgoingNext = (displayedIndex + 1).coerceAtMost(lyricList.lastIndex)
                val riseScale = 1f + animState.value * (38f / 28f - 1f)
                // 小字从底部原位上滑至大字位置，并随进度放大
                if (outgoingNext in lyricList.indices && outgoingNext != displayedIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = -112.cdp.toPx() * animState.value
                                scaleX = riseScale
                                scaleY = riseScale
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        LyricPartItem(
                            index = outgoingNext,
                            lyricModel = lyricList[outgoingNext],
                            viewModel = viewModel,
                            isMain = false,
                        )
                    }
                }
                // 动画后半段：新下一句在底部淡入
                if (animState.value >= 0.5f) {
                    val incomingNext = (curIndex + 1).coerceAtMost(lyricList.lastIndex)
                    if (incomingNext in lyricList.indices && incomingNext != curIndex) {
                        val reveal = (animState.value - 0.5f) * 2f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = reveal
                                    scaleX = 0.85f + reveal * 0.15f
                                    scaleY = 0.85f + reveal * 0.15f
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            LyricPartItem(
                                index = incomingNext,
                                lyricModel = lyricList[incomingNext],
                                viewModel = viewModel,
                                isMain = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

//歌词
@Composable
private fun LyricPartItem(
    index: Int,
    lyricModel: LyricModel,
    viewModel: PlayMusicViewModel,
    isMain: Boolean,
) {
    val activeColor = AppColorsProvider.current.primary
    val inactiveColor = Color.White
    val textStyle = TextStyle(
        fontSize = if (isMain) 38.csp else 28.csp,
        fontWeight = if (isMain) FontWeight.Medium else FontWeight.Normal,
        textAlign = TextAlign.Center,
    )

    val words = lyricModel.words
    if (!words.isNullOrEmpty()) {
        val text = words.joinToString("") { it.text }
        if (isMain && index == viewModel.curLyricIndex) {
            val endFraction = LyricUtil.animatedBrushEndFraction(
                words,
                lyricModel.time,
                viewModel.curPlayPosition,
            )
            Text(
                text = text,
                style = textStyle.merge(
                    TextStyle(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to activeColor,
                                endFraction to activeColor,
                                endFraction to inactiveColor,
                                1f to inactiveColor,
                            ),
                        ),
                    ),
                ),
            )
        } else {
            Text(
                text = text,
                color = inactiveColor.copy(alpha = if (isMain) 1f else 0.6f),
                style = textStyle,
            )
        }
    } else {
        lyricModel.lyric?.let { text ->
            Text(
                text = text,
                style = textStyle,
                color = when {
                    isMain && index == viewModel.curLyricIndex -> activeColor
                    else -> inactiveColor.copy(alpha = if (isMain) 1f else 0.6f)
                },
            )
        }
    }
}
