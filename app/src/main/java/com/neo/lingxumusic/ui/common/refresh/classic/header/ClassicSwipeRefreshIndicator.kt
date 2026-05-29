package com.neo.lingxumusic.ui.common.refresh.classic.header

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshState
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshStateType
import com.neo.lingxumusic.ui.common.refresh.classic.ArrowDrawable
import com.neo.lingxumusic.ui.common.refresh.classic.ProgressDrawable
import com.neo.lingxumusic.ui.common.refresh.rememberDrawablePainter

//指示器的固定高度为 60dp
private val IndicatorHeight = 60.dp

@Composable
//下拉刷新指示器组件
fun ClassicSwipeRefreshIndicator(
    state: SwipeRefreshState,      // 刷新状态管理
    refreshTrigger: Float,         // 触发刷新的阈值（像素）
    maxDrag: Float                 // 最大拖动距离（像素）
) {


    //将 60dp 转换为像素值（px），用于数学计算
    val indicatorHeight = with(LocalDensity.current) { IndicatorHeight.toPx() }
    // 计算偏移量
    val offset = (maxDrag - indicatorHeight).coerceAtMost(state.indicatorOffset - indicatorHeight)


    //判断是否可以刷新
    //当偏移量超过触发阈值时，表示用户已拉到位，"释放即可刷新"
    val releaseToRefresh = offset > refreshTrigger - indicatorHeight

    //状态文字映射
    val text = when (state.type) {
        SwipeRefreshStateType.IDLE -> if (releaseToRefresh) "释放刷新" else "下拉刷新"
        SwipeRefreshStateType.REFRESHING -> "正在刷新..."
        SwipeRefreshStateType.SUCCESS -> "刷新成功"
        SwipeRefreshStateType.FAIL -> "刷新失败"
    }

    //箭头旋转动画
    val angle = remember {
        Animatable(0f)
    }


    LaunchedEffect(releaseToRefresh) {  // 当释放状态改变时触发
        if (releaseToRefresh) {
            angle.animateTo(180f) // 下拉到位时箭头向下（180度）
        } else {
            angle.animateTo(0f) // 未到位时箭头向上（0度）
        }
    }

    //ui
    Box(
        modifier = Modifier
            .fillMaxWidth()                          // 宽度填满
            .height(60.dp)                           // 高度60dp
            .offset { IntOffset(0, offset.toInt()) }, // 根据拖动偏移位置
        contentAlignment = Alignment.Center
    ) {
        // 根据状态显示不同图标
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.isRefreshing()) {
                // 刷新中：显示加载进度圈
                Image(
                    painter = rememberDrawablePainter(ProgressDrawable().apply {
                        setColor(0xff666666.toInt())
                    }),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                )
            } else if (state.isIdle()) {
                // 空闲：显示箭头，并应用旋转角度
                Image(
                    painter = rememberDrawablePainter(ArrowDrawable()),
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(angle.value)
                )
            }
            // 状态文字
            Text(
                text = text,
                color = Color(0xff666666),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .wrapContentSize()
                    .clipToBounds()
                    .padding(16.dp, 0.dp)
            )
        }
    }
}
