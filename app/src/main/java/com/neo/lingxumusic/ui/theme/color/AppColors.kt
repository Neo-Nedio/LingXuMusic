package com.neo.lingxumusic.ui.theme.color

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Stable//告诉 Compose 这个类是稳定的。当类的属性变化时，Compose 会知道需要重组 UI；当属性没变化时，Compose 会跳过重组，提升性能
class AppColors(
    statusBar: Color,      // 状态栏颜色
    primary: Color,        // 主色调（如按钮背景）
    primaryVariant: Color, // 主色调变体（如按压态）
    secondary: Color,      // 次要色调
    background: Color,     // 背景色
    firstText: Color,      // 主要文字颜色（标题）
    secondText: Color,     // 次要文字颜色（正文）
    thirdText: Color,      // 辅助文字颜色（提示）
    appBarBackground: Color, // AppBar 背景色
    appBarContent: Color,    // AppBar 内容颜色（标题、图标）
    card: Color            // 卡片背景色
) {
    var statusBarColor: Color by mutableStateOf(statusBar)
        internal set
    var primary: Color by mutableStateOf(primary)
        internal set
    var primaryVariant: Color by mutableStateOf(primaryVariant)
        internal set
    var secondary: Color by mutableStateOf(secondary)
        internal set
    var background: Color by mutableStateOf(background)
        private set
    var firstText: Color by mutableStateOf(firstText)
        private set
    var secondText: Color by mutableStateOf(secondText)
        private set
    var thirdText: Color by mutableStateOf(thirdText)
        private set
    var appBarBackground: Color by mutableStateOf(appBarBackground)
        private set
    var appBarContent: Color by mutableStateOf(appBarContent)
        private set
    var card: Color by mutableStateOf(card)
        private set
}