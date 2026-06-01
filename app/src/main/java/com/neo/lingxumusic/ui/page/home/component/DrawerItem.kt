package com.neo.lingxumusic.ui.page.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp

/*
┌─────────────────────────────────────────────────────────────┐
│  [左侧图标]  [标题文字]  ................  [右侧图标]      │
│    40dp       24dp间距       弹性空间          32dp         │
└─────────────────────────────────────────────────────────────┘*/
@Composable
fun DrawerItem(
    leftIconResId: Int,        // 左侧图标资源ID
    rightIconResId: Int,       // 右侧图标资源ID
    rightIconModifier: Modifier = Modifier,  // 右侧图标的修饰符（可选）
    title: AnnotatedString,    // 标题文字（支持富文本）
    onClick: (() -> Unit)? = null  // 点击回调（可选）
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.cdp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onClick?.invoke()
            }
            .padding(horizontal = 32.cdp),
        verticalAlignment = Alignment.CenterVertically // 垂直居中
    ) {
        //左侧图标
        CommonIcon(
            resId = leftIconResId,
            modifier = Modifier
                .size(40.cdp)
        )

        //标题文字
        Text(
            text = title,
            modifier = Modifier.padding(start = 24.cdp),
            color = AppColorsProvider.current.firstText
        )

        //弹性空间（让右侧图标靠右）
        Spacer(modifier = Modifier.weight(1f))

        //右侧图标
        CommonIcon(
            resId = rightIconResId,
            modifier = rightIconModifier
                .size(32.cdp),
            tint = AppColorsProvider.current.secondIcon
        )
    }
}
