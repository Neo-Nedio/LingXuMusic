package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp

@Composable
fun PlaylistPage(playlist: Playlist) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScrollHeader(playlist)
        Body()
    }
}

@Composable
private fun ScrollHeader(playlist: Playlist) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(584.cdp)
            .clip(CommonHeadBackgroundShape())
            .background(brush =
                Brush.linearGradient(
                    listOf(Color.LightGray.copy(0.5f),
                        Color.Gray.copy(0.5f),
                        Color.LightGray.copy(0.5f)
                    )
                )
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(playlist.pic),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(584.cdp)
                .graphicsLayer { alpha = 0.5f },
        )
    }

    CommonTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(88.cdp),
        backgroundColor = Color.Transparent,
        title = "歌单",
        contentColor = AppColorsProvider.current.pure,
        leftIconResId = R.drawable.ic_drawer_toggle,
        leftClick = { },
        rightIconResId = R.drawable.ic_search
    )
}

@Composable
private fun Body() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        for (i in 1 until 20) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.cdp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$i")
            }
        }
    }
}