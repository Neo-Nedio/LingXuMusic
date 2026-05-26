package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

/*
┌─────────┐
│  头像   │  ← 圆形头像，悬空在卡片上方
└────┬────┘
┌────────┴────────┐
│                 │
│   ssk_evan      │  ← 白色卡片
│ 2关注｜2粉丝｜Lv.8│
│                 │
└─────────────────┘*/
@Composable
fun UserInfoComponent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .padding(top = 60.cdp, start = 32.cdp, end = 32.cdp)
                .fillMaxWidth()
                .height(240.cdp)
                .clip(RoundedCornerShape(24.cdp))
                .background(AppColorsProvider.current.card),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppGlobalData.sLoginData?.nickname ?: "无",
                fontSize = 40.csp,
                color = AppColorsProvider.current.firstText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 60.cdp)
            )
            Text(
                text = "2 关注  ｜  2 粉丝  ｜  Lv.8",
                fontSize = 32.csp,
                color = AppColorsProvider.current.secondText,
                modifier = Modifier.padding(top = 26.cdp)
            )
        }

        CommonNetworkImage(
            url = AppGlobalData.sLoginData?.pic,
            placeholder = R.drawable.ic_default_avator,
            error = R.drawable.ic_default_avator,
            modifier =  Modifier.size(120.cdp)
                .clip(
                    RoundedCornerShape(50)
                )
        )
    }
}

@Stable
//自定义 Shape，用于绘制底部带弧形弯曲的矩形，常用于卡片背景、图片容器的特殊造型
class BgImageShapes(var radius: Float = 80f) : Shape {

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