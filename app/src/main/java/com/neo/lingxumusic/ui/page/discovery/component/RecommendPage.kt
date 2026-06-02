package com.neo.lingxumusic.ui.page.discovery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.displayTitle
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.theme.isInDarkTheme
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.discovery.RecommendViewModel

@Composable
fun RecommendPage() {
    val viewModel: RecommendViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.cdp, vertical = 24.cdp),
    ) {
        //每日推荐与猜你喜欢
        Row(modifier = Modifier.fillMaxWidth()) {
            ViewStateComponent(
                modifier = Modifier.weight(1f),
                viewStateLiveData = viewModel.everyDayResult,
                loadDataBlock = { viewModel.loadEveryDayRecommend() },
                specialRetryBlock = { viewModel.loadEveryDayRecommend() },
                viewStateContentAlignment = Alignment.TopCenter,
                viewStateComponentModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                RecommendCoverCard(
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "每日推荐",
                    songName = viewModel.everyDaySongList.firstOrNull()?.displayTitle().orEmpty(),
                    coverUrl = viewModel.everyDayCover,
                )
            }
            Box(modifier = Modifier.width(16.cdp))
            ViewStateComponent(
                modifier = Modifier.weight(1f),
                viewStateLiveData = viewModel.guessLikeResult,
                loadDataBlock = { viewModel.loadGuessYourLike() },
                specialRetryBlock = { viewModel.loadGuessYourLike() },
                viewStateContentAlignment = Alignment.TopCenter,
                viewStateComponentModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                RecommendCoverCard(
                    modifier = Modifier.align(Alignment.TopCenter),
                    label = "猜你喜欢",
                    songName = viewModel.guessLikeSongList.firstOrNull()?.displayTitle().orEmpty(),
                    coverUrl = viewModel.guessLikeCover,
                )
            }
        }
    }
}

//推荐的card
@Composable
private fun RecommendCoverCard(
    label: String,
    songName: String,
    coverUrl: String?,
    modifier: Modifier = Modifier,
) {
    val isDark = isInDarkTheme()
    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF4A4A4A)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFE0E0E0)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }
    val textColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.cdp))
            .background(brush = backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.65f)
                .padding(start = 16.cdp, top = 16.cdp, end = 8.cdp),
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 26.csp,
            )
            MarqueeText(
                text = songName,
                modifier = Modifier.padding(top = 8.cdp),
                color = textColor,
                fontSize = 22.csp,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.58f)
                .aspectRatio(1f)
                .padding(end = 12.cdp, bottom = 12.cdp),
        ) {
            CommonNetworkImage(
                url = coverUrl?.replaceSize(),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.cdp)),
            )
            CommonIcon(
                resId = R.drawable.ic_action_play,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.cdp)
                    .size(40.cdp),
                tint = Color.White,
            )
        }
    }
}