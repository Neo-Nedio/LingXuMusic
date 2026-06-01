package com.neo.lingxumusic.ui.page.podcast

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.common.CommonTopAppBar

@Composable
fun PodcastPage(onToggleDrawer: () -> Unit) {
    Column(
        Modifier
            .statusBarsPadding()
            .fillMaxSize(),
    ) {
        CommonTopAppBar(
            title = "播客",
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = onToggleDrawer,
        )
        androidx.compose.material.Text("播客")
    }
}
