package com.neo.lingxumusic.ui.page.podcast

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.ui.common.CommonTopAppBar

@Composable
fun PodcastPage() {
    Column(Modifier.statusBarsPadding().fillMaxSize()) {
        CommonTopAppBar(title = "博客", leftIconResId = -1)
        Text("博客")
    }
}