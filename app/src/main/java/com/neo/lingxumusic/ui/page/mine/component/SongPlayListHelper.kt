package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.ScreenUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import kotlinx.coroutines.delay

@Composable
fun SongPlayListHelper() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 标题
        Text(
            text = "歌单助手",
            color = AppColorsProvider.current.secondText,
            fontSize = 28.csp,
            modifier = Modifier.padding(bottom = 12.dp, top = 20.cdp, start = 32.cdp)
        )

        // 描述
        Text(
            text = "你可以从歌单中筛选出",
            color = AppColorsProvider.current.secondText,
            fontSize = 14.csp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 10.cdp, bottom = 24.cdp)
                .fillMaxWidth(),
        )

        // 轮播文字动画
        RollTextLayout()

        // 按钮
        Button(
            onClick = {},
            modifier = Modifier
                .padding(start = 160.cdp, end = 160.cdp, bottom = 40.cdp, top = 20.cdp)
                .fillMaxWidth()
                .height(70.cdp),
            shape = CircleShape, // 圆形按钮（实际是胶囊形状，因为高度70，左右padding很大）
            colors = ButtonDefaults.buttonColors(
                contentColor = AppColorsProvider.current.primary
            ),
            content = {
                Text(text = "试一下", fontSize = 30.csp, color = Color.White)
            }
        )
    }
}

@Composable
private fun RollTextLayout() {
    var topIndex by remember { mutableStateOf(0) }      // 上一行文字索引
    var middleIndex by remember { mutableStateOf(1) }    // 当前显示的文字索引
    var bottomIndex by remember { mutableStateOf(2) }    // 下一行文字索引
    var doAnim by remember { mutableStateOf(false) }     // 是否开始动画
    var resetAnim by remember { mutableStateOf(true) }   // 是否重置动画
    val animState = remember { Animatable(0f) }          // 动画进度 0→1

    //动画执行逻辑
    LaunchedEffect(doAnim, resetAnim) {
        // 条件满足：两个都是 true → 执行动画
        if (doAnim && resetAnim) {
            animState.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 800   // 总时长 800 毫秒
                    0f at 0                // 0ms 时，值为 0
                    1f at 800              // 800ms 时，值为 1
                }
            )
        } else {
            // 条件不满足：任一为 false → 停止动画
            animState.stop()
        }
    }

    // 当动画到达终点（值为1）时，执行切换文字的逻辑
    LaunchedEffect(animState.value == 1f) {
        if (animState.value == 1f) {
            resetAnim = false        // ① 停止新的动画启动
            delay(3000)              // ② 停留 3 秒

            //  切换索引（循环 +1）
            topIndex = if (topIndex == mSongLabels.size - 1) 0 else topIndex + 1
            middleIndex = if (middleIndex == mSongLabels.size - 1) 0 else middleIndex + 1
            bottomIndex = if (bottomIndex == mSongLabels.size - 1) 0 else bottomIndex + 1

            //  重置动画值
            animState.snapTo(0f)     // 立即将动画值设为 0
            resetAnim = true         // ⑤ 允许下一次动画
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .height(160.cdp)
            .onGloballyPositioned {
                //当组件在屏幕上确定位置后，判断它是否在屏幕可见范围内，从而决定是否开始动画
                doAnim = it.positionInWindow().y < ScreenUtil.getScreenHeight()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //顶部文字（上一段）动画
        var topAnimValue = animState.value
        if (animState.value > 0.5f) {
            topAnimValue = 0.5f // 限制最大值 0.5
        }
        topAnimValue = 1 - topAnimValue * 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.cdp)
                .graphicsLayer {
                    alpha = topAnimValue
                    scaleY = topAnimValue
                    scaleX = topAnimValue
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = getSpanText(mSongLabels[topIndex]))
        }

        //中部文字（当前文字）动画
        Box {
            var middleAnimValue = animState.value
            if (middleAnimValue <= 0.5f) {
                middleAnimValue = 0.5f // 限制最小值 0.5
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.cdp)
                    .graphicsLayer {
                        alpha = middleAnimValue
                        scaleY = middleAnimValue
                        scaleX = middleAnimValue
                        translationY = if (animState.value != 0f) {
                            -80.cdp.toPx() * animState.value // 向上移动
                        } else {
                            0f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = getSpanText(mSongLabels[middleIndex]))
            }

            //底部文字（下一段）动画
            if (animState.value >= 0.5f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.cdp)
                        .graphicsLayer {
                            alpha = animState.value - 0.5f
                            scaleY = animState.value - 0.5f
                            scaleX = animState.value - 0.5f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = getSpanText(mSongLabels[bottomIndex]))
                }
            }
        }

    }
}


private fun getSpanText(sourceText: String): AnnotatedString {

    return buildAnnotatedString {
        sourceText.split(" ").forEachIndexed() { index, it ->
            var ss = ""
            if (it.contains("的")) {
                ss = it
                withStyle(
                    style = SpanStyle(
                        color = Color.LightGray,
                        fontSize = (32 * 0.9).csp,
                    ),
                ) {
                    append(" $ss ")
                }
            } else {
                ss = " $it "
                withStyle(
                    style = SpanStyle(
                        color = Color.LightGray,
                        fontSize = 32.csp,
                    ),
                ) {
                    append(" ")
                }
                withStyle(
                    style = SpanStyle(
                        color = FOREGROUND_COLORS[index % FOREGROUND_COLORS.size],
                        background = BACKGROUND_COLORS[index % BACKGROUND_COLORS.size],
                        fontSize = 32.csp
                    )
                ) {
                    append(" $ss ")
                }
                withStyle(
                    style = SpanStyle(
                        color = Color.LightGray,
                        fontSize = 32.csp,
                    ),
                ) {
                    append(" ")
                }
            }
        }
    }
}


private val BACKGROUND_COLORS = listOf(
    Color(0xFFE6F7FF),
    Color(0xFFFBEFFF),
    Color(0xFFFFFBE6),
    Color(0xFFFFF3F3)
)
private val FOREGROUND_COLORS = listOf(
    Color(0xFF1890FF),
    Color(0xFFCA72E7),
    Color(0xFFFAAD14),
    Color(0xFFEA4C43)
)

private var mSongLabels = listOf(
    "80年代 华语 老歌",
    "最近收藏 的 清新民谣",
    "适合 夜晚听 的 纯音乐",
    "很久没听 的 后摇",
    "最近一年 发布 的 流行音乐"
)