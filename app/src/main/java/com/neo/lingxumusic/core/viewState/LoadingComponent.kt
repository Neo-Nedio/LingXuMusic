package com.neo.lingxumusic.core.viewState

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import kotlin.math.max

@Composable
fun LoadingComponent(
    modifier: Modifier = Modifier.fillMaxSize(),
    contentAlignment: Alignment = Alignment.Center
) {
    val color = AppColorsProvider.current.primary
    //无限动画
    val animateTween by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(400, easing = LinearEasing),
            RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {

        //矩形区域的画布
        Canvas(
            modifier = Modifier
                .padding(30.dp)
                .width(30.dp)
                .height(24.dp)
        ) {
            val rectWidth = size.width / 10    // 每个竖条的宽度 = 总宽度的 1/10
            val canvasHeight = size.height      // 画布总高度（24dp）

            //第一条：高度在 canvasHeight * 0.2f 和 canvasHeight * 0.9f 之间变化
            val rectHeight1 = max(canvasHeight * 0.2f, canvasHeight * animateTween * 0.9f)
            drawRect(
                color = color,
                topLeft = Offset(0f, canvasHeight - rectHeight1),
                size = Size(rectWidth, rectHeight1)
            )

            //跳动逻辑与条1 相反（用减法）
            val rectHeight2 = canvasHeight - (canvasHeight * animateTween * 0.75f)
            drawRect(
                color = color,
                //条2 位置在 rectWidth * 3（距离左边 3 个竖条宽度）
                topLeft = Offset(rectWidth * 3, canvasHeight - rectHeight2),
                size = Size(rectWidth, rectHeight2)
            )

            //高度 0% ~ 100% 变化
            val rectHeight3 = canvasHeight * animateTween * 1.0f
            drawRect(
                color = color,
                topLeft = Offset(rectWidth * 6, canvasHeight - rectHeight3),
                size = Size(rectWidth, rectHeight3)
            )


            //跳动幅度 15% ~ 100%
            val rectHeight4 = canvasHeight - (canvasHeight * animateTween * 0.85f)
            drawRect(
                color = color,
                topLeft = Offset(rectWidth * 9, canvasHeight - rectHeight4),
                size = Size(rectWidth, rectHeight4)
            )
        }
    }

}