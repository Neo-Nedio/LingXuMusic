package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import kotlin.math.min

//圆环进度条
@Composable
fun CircleProgress(modifier: Modifier = Modifier, progress: Int) {
    // 计算圆弧扫过的角度（0% → 0度，100% → 360度）
    val sweepAngle = progress / 100f * 360
    val progressColor = AppColorsProvider.current.primary

    Canvas(modifier = modifier) {
        // 取宽高中的最小值，保证圆是正圆
        val canvasSize = min(size.width, size.height)

        // 1. 背景圆环（浅灰色，完整圆）
        drawCircle(
            color = Color.LightGray,
            radius = canvasSize / 2,
            style = Stroke(width = 4f)  // 空心圆，线宽 4f
        )

        // 2. 进度圆弧（深灰色）
        drawArc(
            color = progressColor,
            style = Stroke(width = 4f),
            startAngle = -90f,           // 从顶部开始（-90° = 12点钟方向）
            sweepAngle = sweepAngle,     // 扫过的角度
            useCenter = false            // 不连接圆心，保持圆弧
        )
    }
}
