package com.neo.lingxumusic.ui.common

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection



@Stable
//自定义 Shape，用于绘制底部带弧形弯曲的矩形，常用于卡片背景、图片容器的特殊造型
class CommonHeadBackgroundShape(var radius: Float = 80f) : Shape {

    override fun createOutline(
        size: Size,                    // 组件的宽高
        layoutDirection: LayoutDirection,  // 布局方向（LTR/RTL）
        density: Density               // 密度转换工具
    ): Outline {
        val path = Path()

        // 1. 从左上角开始
        path.moveTo(0f, 0f)

        // 2. 向左下角画直线（到达弧形起始点）
        path.lineTo(0f, size.height - radius)

        // 3. 画底部弧形（二次贝塞尔曲线）
        path.quadraticTo(
            size.width / 2f,           // 控制点：底部中心
            size.height,               // 控制点 Y：底部边缘
            size.width,                // 终点：右下角
            size.height - radius       // 终点 Y：弧形结束位置
        )

        // 4. 向右上角画直线
        path.lineTo(size.width, 0f)

        // 5. 闭合路径（回到起点）
        path.close()

        return Outline.Generic(path)
    }
}