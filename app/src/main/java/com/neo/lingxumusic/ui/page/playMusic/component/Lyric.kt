package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.neo.lingxumusic.ui.page.playMusic.showLyric
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun Lyric() {
    Box(
        modifier = Modifier
            .padding(vertical = 50.cdp)
            .fillMaxSize()
            .background(Color.Red)
            .clickable {
                showLyric = !showLyric
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material.Text(text = "歌词列表", fontSize = 50.csp)
    }
}