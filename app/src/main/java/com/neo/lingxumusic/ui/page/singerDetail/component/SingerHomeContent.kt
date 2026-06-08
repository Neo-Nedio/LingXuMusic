package com.neo.lingxumusic.ui.page.singerDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.neo.lingxumusic.model.ArtistDetail
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp


/**
 * 歌手主页内容组件
 * - 显示统计数据卡片
 * - 显示简介卡片
 * - 显示 long_intro 列表
 */
@Composable
fun SingerHomeContent(
    artistDetail: ArtistDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.cdp, vertical = 16.cdp)
    ) {
        // 统计数据卡片
        StatsCard(
            songCount = artistDetail.song_count,
            albumCount = artistDetail.album_count,
            mvCount = artistDetail.mv_count,
            fansCount = artistDetail.fansnums
        )

        Spacer(modifier = Modifier.height(24.cdp))

        // 简介卡片
        artistDetail.intro?.let { intro ->
            IntroCard(intro = intro)
        }
    }
}

/**
 * 统计数据卡片组件
 */
@Composable
private fun StatsCard(
    songCount: Int,
    albumCount: Int,
    mvCount: Int,
    fansCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = AppColorsProvider.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.card.copy(alpha = 0.6f),
                        colors.background.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.cdp)
            )
            .padding(vertical = 24.cdp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("歌曲", songCount)
        StatItem("专辑", albumCount)
        StatItem("MV", mvCount)
        StatItem("粉丝", fansCount)
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    val colors = AppColorsProvider.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = StringUtil.friendlyNumber(count),
            color = colors.firstText,
            fontSize = 36.csp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.cdp))
        Text(
            text = label,
            color = colors.secondText,
            fontSize = 24.csp
        )
    }
}

/**
 * 简介卡片组件
 */
@Composable
private fun IntroCard(intro: String, modifier: Modifier = Modifier) {
    val colors = AppColorsProvider.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.card.copy(alpha = 0.6f),
                        colors.background.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.cdp)
            )
            .padding(24.cdp)
    ) {
        // 左上角"简介"标题
        Text(
            text = "简介",
            color = colors.primary,
            fontSize = 28.csp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.cdp))
        // 简介内容
        Text(
            text = intro,
            color = colors.secondText,
            fontSize = 26.csp,
            lineHeight = 42.csp
        )
    }
}

