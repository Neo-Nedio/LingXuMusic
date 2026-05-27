package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.neo.lingxumusic.utils.cdp

//进度条背景画刷 ：绘制进度条的背景（未播放部分的轨道）
val progressPaint = Paint().apply {
    isAntiAlias = true        // 抗锯齿，边缘更平滑
    style = PaintingStyle.Fill // 填充模式（不是描边）
    color = Color.LightGray    // 浅灰色
}

//圆点画刷 ：绘制进度条上的拖拽圆点
val circlePaint = Paint().apply {
    isAntiAlias = true
    style = PaintingStyle.Fill
    color = Color.White // 白色圆点
}
/*

正常状态（未触摸）
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ ← 灰色进度条（2px高）
│                                    ●                        │ ← 白色圆点（10px半径）
│                                                             │
└─────────────────────────────────────────────────────────────┘
0%                              50%                        100%
按下状态（正在拖动）
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                    ◉                        │ ← 圆点变大（20px半径）
│                                                             │
└─────────────────────────────────────────────────────────────┘
圆点从 ● 变成 ◉（更大），提供触摸反馈。
*/

@Composable
fun SeekBar(
    progress: Int = 0,
    seeking: (Int) -> Unit = {},
    seekTo: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    //画布尺寸
    var width by remember { mutableFloatStateOf(0f) }
    var height by remember { mutableFloatStateOf(0f) }
    // 圆点半径
    val smallRadius = 10f   // 未按下时半径 10px
    val largeRadius = 20f   // 按下时半径 20px（变大突出反馈）
    //进度条高度
    val progressHeight = 4f


    //圆点是否被按下
    var isPressed by remember {
        mutableStateOf(false)
    }

    //圆点的 X 坐标
    var circleCenterX by remember {
        mutableFloatStateOf(0f)
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(80.cdp)
        .pointerInput(Unit) {
            awaitEachGesture { //等待每一次，而不是之前awaitPointerEventScope只处理一次
                while (true) {
                        val event: PointerEvent = awaitPointerEvent(PointerEventPass.Final)
                        if (event.changes.size == 1) {  // 只处理单指
                            // 1.单指操作
                            val pointer = event.changes[0]
                            val x = pointer.position.x
                            circleCenterX = pointer.position.x // 先直接赋值

                            //边界检查
                            circleCenterX = if (x < 0f) {
                                0f
                            } else if (x > width) {
                                width
                            } else {
                                x
                            }
                            //实时回调进度（拖动中）
                            //将触摸点的 X 坐标（像素）转换为进度百分比（0-100）
                            seeking.invoke((circleCenterX * 100 / width + 0.5).toInt())

                            //处理按下/抬起
                            if (!pointer.pressed) {
                                // 手指抬起,结束
                                isPressed = false
                                //将触摸点的 X 坐标（像素）转换为进度百分比（0-100）
                                seekTo.invoke((circleCenterX * 100 / width + 0.5).toInt())
                                break // 退出循环，手势结束
                            } else {
                                if (!pointer.previousPressed) {
                                    // 手指刚按下
                                    isPressed = true
                                }
                            }
                    }
                }
            }
        }) {
        //更新了全局 width/height
        width = drawContext.size.width
        height = drawContext.size.height
        drawIntoCanvas {
            //绘制 SeekBar 的灰色背景进度条
/*
            Y=0  ┌─────────────────────────────────┐
            │                                 │
            Y=39 ├─────────────────────────────────┤ ← 进度条顶部
            Y=41 ├─────────────────────────────────┤ ← 进度条底部
            │                                 │
            Y=80 └─────────────────────────────────┘

 */
            val rect = Rect(
                Offset(0f, (height - progressHeight) / 2),      // 左上角坐标
                Offset(width, (height + progressHeight) / 2)     // 右下角坐标
            )
            it.drawRect(rect, progressPaint)

            //计算圆点位置
            var x = width * progress / 100 //根据当前进度（0-100）计算圆点应该在画布上的 X 坐标（像素）
            val radius = if (isPressed) largeRadius else smallRadius //确定圆点半径（根据是否按下）
            //边界检查
            if (x < radius) {
                x = radius
            } else if (x > width - radius) {
                x = width - radius
            }
            //绘制圆点
            it.drawCircle(
                Offset(x, height / 2),  // 圆心坐标（X = 计算出的位置，Y = 垂直居中）
                radius,                  // 半径（10px 或 20px）
                circlePaint              // 白色画刷
            )
        }
    }
}

