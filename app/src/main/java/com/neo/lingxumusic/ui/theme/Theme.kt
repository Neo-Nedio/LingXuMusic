package com.neo.lingxumusic.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.neo.lingxumusic.ui.theme.color.AppColors
import com.neo.lingxumusic.ui.theme.color.palette.dark.DarkColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.BlueColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.DefaultColorPalette


// 默认主题
const val THEME_DEFAULT = 0

// 蓝色主题
const val THEME_BLUE = 1

/**
 * 主题状态
 */
val themeTypeState: MutableState<Int> by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf(THEME_DEFAULT) // 全局主题状态，初始为默认主题
}

var AppColorsProvider = compositionLocalOf {
    DefaultColorPalette // 默认值，如果没有提供则使用默认主题
}


const val TWEEN_DURATION = 600

@Composable
fun AppTheme(
    themeType: Int,           // 主题类型（0=默认，1=蓝色）
    isDark: Boolean = isSystemInDarkTheme(),  // 是否深色模式，默认跟随系统
    content: @Composable () -> Unit
) {

    val targetColors = if (isDark) DarkColorPalette else {
        when (themeType) {
            THEME_BLUE -> BlueColorPalette    // 蓝色主题（浅色模式）
            else -> DefaultColorPalette       // 默认主题（浅色模式）
        }
    }

    //颜色动画
    val statusBarColor = animateColorAsState(targetColors.statusBarColor, TweenSpec(TWEEN_DURATION))
    val pure = animateColorAsState(targetColors.pure, TweenSpec(TWEEN_DURATION))
    val primary = animateColorAsState(targetColors.primary, TweenSpec(TWEEN_DURATION))
    val primaryVariant = animateColorAsState(targetColors.primaryVariant, TweenSpec(TWEEN_DURATION))
    val secondary = animateColorAsState(targetColors.secondary, TweenSpec(TWEEN_DURATION))
    val background = animateColorAsState(targetColors.background, TweenSpec(TWEEN_DURATION))
    val firstText = animateColorAsState(targetColors.firstText, TweenSpec(TWEEN_DURATION))
    val secondText = animateColorAsState(targetColors.secondText, TweenSpec(TWEEN_DURATION))
    val thirdText = animateColorAsState(targetColors.thirdText, TweenSpec(TWEEN_DURATION))
    val firstIcon = animateColorAsState(targetColors.firstIcon, TweenSpec(TWEEN_DURATION))
    val secondIcon = animateColorAsState(targetColors.secondIcon, TweenSpec(TWEEN_DURATION))
    val thirdIcon = animateColorAsState(targetColors.thirdIcon, TweenSpec(TWEEN_DURATION))
    val appBarBackground = animateColorAsState(targetColors.appBarBackground, TweenSpec(TWEEN_DURATION))
    val appBarContent = animateColorAsState(targetColors.appBarContent, TweenSpec(TWEEN_DURATION))
    val card = animateColorAsState(targetColors.card, TweenSpec(TWEEN_DURATION))
    val bottomMusicPlayBarBackground = animateColorAsState(targetColors.bottomMusicPlayBarBackground, TweenSpec(TWEEN_DURATION))
    //创建 AppColors 对象
    val appColors = AppColors(
        statusBar = statusBarColor.value,
        pure = pure.value,
        primary = primary.value,
        primaryVariant = primaryVariant.value,
        secondary = secondary.value,
        background = background.value,
        firstText = firstText.value,
        secondText = secondText.value,
        thirdText = thirdText.value,
        firstIcon = firstIcon.value,
        secondIcon = secondIcon.value,
        thirdIcon = thirdIcon.value,
        appBarBackground = appBarBackground.value,
        appBarContent = appBarContent.value,
        card = card.value,
        bottomMusicPlayBarBackground = bottomMusicPlayBarBackground.value
    )

    //设置状态栏/导航栏颜色
    val activity = LocalActivity.current
    val systemBarColor = appColors.statusBarColor.toArgb()
    val transparent = Color.Transparent.toArgb()
    SideEffect {
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        (activity as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(transparent, transparent),
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(systemBarColor)
            } else {
                SystemBarStyle.light(systemBarColor, systemBarColor)
            }
        )
    }

    //CompositionLocalProvider：向下传递 appColors 对象
    //子组件可以通过 AppColorsProvider.current 获取颜色
    //MaterialTheme 提供 Compose 原生的 Material 主题
    CompositionLocalProvider(AppColorsProvider provides appColors) {
        MaterialTheme(
            shapes = shapes,
            typography = typography,
        ) {
            content()
        }
    }
}