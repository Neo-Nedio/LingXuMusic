package com.neo.lingxumusic.ui.page.singerDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.MvInfo
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

fun LazyListScope.singerMvContent(
    mvList: LazyPagingItems<MvInfo>
) {
    items(
        count = mvList.itemCount,
        key = mvList.itemKey { it.video_id }
    ) { index ->
        val mv = mvList[index]
        if (mv != null) {
            MvItem(
                mv = mv,
                onClick = { /* TODO 跳转到 MV 详情页 */ },
                modifier = Modifier.padding(horizontal = 24.cdp, vertical = 12.cdp)
            )
        }
    }
}


@Composable
fun MvItem(
    mv: MvInfo,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppColorsProvider.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // 视频区域：背景 + 播放量 + 播放图标
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.cdp)
                .clip(RoundedCornerShape(16.cdp))
        ) {
            // 视频背景（用视频封面图作为静态背景）
            CommonNetworkImage(
                url = (mv.hdpic ?: mv.cover)?.replaceSize(),
                modifier = Modifier.fillMaxSize()
            )

            // 顶部到底部的渐变遮罩（让左下/右下文字和图标更清晰）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // 左下角：播放量
            Text(
                text = StringUtil.friendlyNumber(mv.heat),
                color = Color.White,
                fontSize = 22.csp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.cdp, bottom = 16.cdp)
            )

            // 右下角：播放图标
            CommonIcon(
                resId = R.drawable.ic_play_without_circle,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.cdp, bottom = 16.cdp)
                    .size(36.cdp)
            )
        }

        // 图片下方：视频名
        Text(
            text = mv.video_name.orEmpty(),
            color = colors.firstText,
            fontSize = 26.csp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.cdp, end = 4.cdp, top = 12.cdp, bottom = 4.cdp)
        )
    }
}