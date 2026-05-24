package com.neo.lingxumusic.ui.page.discovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.ui.common.CommonTopAppBar

@Composable
fun DiscoveryPage() {
    Column(Modifier.fillMaxSize()) {
        CommonTopAppBar(title = "发现")
        Text("发现")
    }
}