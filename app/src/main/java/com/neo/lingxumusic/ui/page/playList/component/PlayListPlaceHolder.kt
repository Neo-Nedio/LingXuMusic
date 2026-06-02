package com.neo.lingxumusic.ui.page.playList.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

// 歌单占位组件（当没有歌单时显示提示和添加按钮）
@Composable
fun PlayListPlaceHolder(tip: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.cdp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(bottomStart = 24.cdp, bottomEnd = 24.cdp))
            .background(AppColorsProvider.current.card)
            .padding(vertical = 80.cdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 提示文字
        Text(
            text = tip,
            color = AppColorsProvider.current.secondText,
            fontSize = 28.csp,
            modifier = Modifier.padding(bottom = 48.cdp)
        )

        // 添加按钮
        Button(
            onClick = {}, //todo 添加按钮回调
            modifier = Modifier
                .padding(start = 160.cdp, end = 160.cdp, bottom = 40.cdp)
                .fillMaxWidth()
                .height(70.cdp),
            shape = CircleShape, // 圆形（实际是胶囊形状，因高度70dp，左右padding 160dp）
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppColorsProvider.current.primary
            ),
            content = {
                Text(text = "去添加", fontSize = 30.csp, color = Color.White)
            }
        )
    }
}