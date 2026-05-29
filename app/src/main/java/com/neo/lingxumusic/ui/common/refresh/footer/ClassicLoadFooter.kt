package com.neo.lingxumusic.ui.common.refresh.footer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.lingxumusic.ui.common.refresh.drawable.ProgressDrawable
import com.neo.lingxumusic.ui.common.refresh.rememberDrawablePainter

@Composable
//列表底部加载提示组件，通常用于下拉刷新框架中，在列表滚动到底部加载更多数据时显示
fun ClassicLoadFooter() {

    Row(
        modifier = Modifier
            .fillMaxWidth()    // 宽度填满父容器
            .height(80.dp),    // 高度80dp（约在屏幕上占一定空间）
        horizontalArrangement = Arrangement.Center,  // 水平居中对齐
        verticalAlignment = Alignment.CenterVertically  // 垂直居中对齐
    ) {
        //加载图标
        Image(
            painter = rememberDrawablePainter(ProgressDrawable().apply {
                setColor(0xff666666.toInt())  // 设置图标颜色为灰色
            }),
            contentDescription = "",
            modifier = Modifier
                .padding(end = 10.dp)  // 右边距10dp，与文字间隔
                .size(20.dp)           // 图标宽高20dp
        )

        //加载文字
        Text(
            text = "正在加载...",
            fontSize = 16.sp,
            color = Color(0xff666666),
        )
    }
}
