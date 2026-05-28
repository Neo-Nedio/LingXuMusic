package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.neo.lingxumusic.ui.page.mine.component.CurrentPlayList

var showPlayListSheet by mutableStateOf(false)

@Composable
fun PlayListSheet() {
    AnimatedVisibility(
        visible = showPlayListSheet,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(400),
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(400),
        ),
    ) {
        PlayListSheetContent()
    }
}

@Composable
private fun PlayListSheetContent() {
    BackHandler(enabled = showPlayListSheet) {
        showPlayListSheet = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        //上部区域，点击关闭播放列表
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable { showPlayListSheet = false },
        )
        //下部播放列表
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            CurrentPlayList()
        }
    }
}
