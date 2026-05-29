package com.neo.lingxumusic.ui.common.refresh.footer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neo.lingxumusic.core.viewState.LoadingComponent
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun CommonLoadFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.cdp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoadingComponent(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 20.cdp),
            loadingWidth = 30.cdp,
            loadingHeight = 30.cdp,
            color = AppColorsProvider.current.secondIcon
        )

        Text(
            text = "正在加载...",
            fontSize = 30.csp,
            color = AppColorsProvider.current.secondText,
        )
    }
}
