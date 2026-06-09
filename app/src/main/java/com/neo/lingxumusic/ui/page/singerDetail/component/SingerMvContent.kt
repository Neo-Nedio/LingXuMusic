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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.MvInfo
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.singerDetail.SingerDetailViewModel

// MV 分类标签
private val MV_TAG_TEXTS = listOf("全部", "官方", "现场", "饭制", "歌手发布")
private val MV_TAG_VALUES = listOf("all", "official", "live", "fan", "artist")


fun LazyListScope.singerMvContent(
    mvList: LazyPagingItems<MvInfo>
) {
    // MV 分类 tab（吸附顶部）：stickyHeader 块自带 Composable 上下文
    stickyHeader {
        MvTagTabBar()
    }

    // MV 列表
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
private fun MvTagTabBar() {
    val viewModel: SingerDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val colors = AppColorsProvider.current
    val selectedIndex = MV_TAG_VALUES.indexOf(viewModel.currentMvTag).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        CommonTabLayout(
            selectedIndex = selectedIndex,
            tabTexts = MV_TAG_TEXTS,
            backgroundColor = colors.background,
            selectedTextColor = colors.firstText,
            unselectedTextColor = colors.secondText,
            style = CommonTabLayoutStyle(
                isScrollable = false,
                selectedTextSize = 22.csp,
                unselectedTextSize = 22.csp,
                selectedTextBold = true,
                unselectedTextBold = false,
                indicatorHeight = 0.cdp,
                tabHorizontalPadding = 0.cdp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.cdp),
                // 在文字下方绘制一个半椭圆形背景
                tabItemDrawBehindBlock = { position: Int ->
                    val w = size.width
                    val h = size.height
                    val ellipseW = w * 0.6f
                    val ellipseH = h * 0.7f
                    val left = (w - ellipseW) / 2f
                    val top = (h - ellipseH) / 2f
                    val alpha = if (position == selectedIndex) 0.20f else 0.08f
                    drawRoundRect(
                        color = colors.firstText.copy(alpha = alpha),
                        topLeft = Offset(left, top),
                        size = Size(ellipseW, ellipseH),
                        // 用 height/2 当圆角半径 → 左右两端完全圆弧（半椭圆）
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            ellipseH / 2f,
                            ellipseH / 2f
                        )
                    )
                }
            ),
            onTabSelected = { index ->
                val tag = MV_TAG_VALUES[index]
                if (viewModel.currentMvTag != tag) {
                    viewModel.currentMvTag = tag
                    viewModel.mvListFlow = null      // 清空旧分页，否则 buildMvListPager 早退
                    viewModel.buildMvListPager()     // 重新构建（使用新 tag）
                }
            }
        )
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
