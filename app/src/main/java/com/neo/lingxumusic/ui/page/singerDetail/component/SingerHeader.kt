package com.neo.lingxumusic.ui.page.singerDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun SingerHeader(
    avatarUrl: String?,
    singerName: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomStart
    ) {
        // 头像背景图（占满整个区域）
        CommonNetworkImage(
            url = avatarUrl,
            modifier = Modifier.fillMaxSize(),
        )

        // 底部渐变遮罩（从上到下：透明 → 黑色）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.cdp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // 歌手名字（底部）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.cdp, end = 32.cdp, bottom = 48.cdp)
        ) {
            Text(
                text = singerName ?: "",
                color = Color.White,
                fontSize = 56.csp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
