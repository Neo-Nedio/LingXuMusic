package com.neo.lingxumusic.ui.page.home.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.ui.theme.THEME_BLUE
import com.neo.lingxumusic.ui.theme.THEME_DEFAULT
import com.neo.lingxumusic.ui.theme.THEME_GREEN
import com.neo.lingxumusic.ui.theme.THEME_NIGHT
import com.neo.lingxumusic.ui.theme.THEME_ORIGIN
import com.neo.lingxumusic.ui.theme.THEME_PURPLE
import com.neo.lingxumusic.ui.theme.THEME_YELLOW
import com.neo.lingxumusic.ui.theme.color.palette.dark.DarkColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.BlueColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.DefaultColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.GreenColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.OriginColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.PurpleColorPalette
import com.neo.lingxumusic.ui.theme.color.palette.light.YellowColorPalette
import com.neo.lingxumusic.ui.theme.themeTypeState
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import kotlinx.coroutines.launch

@Composable
fun DrawerThemeSetting() {
    // 动画值，控制展开/收起的进度（0=收起，1=完全展开）
    val anim by remember {
        mutableStateOf(Animatable(0f))
    }
    // 主题列表总高度 = 每个主题 88dp × 7个主题
    val themeListHeight = 88 * themeModels.size
    val scope = rememberCoroutineScope()

    // 外层 Column，高度随动画值变化，实现展开/收起效果
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height((anim.value * themeListHeight + 88f).cdp) //动态高度
    ) {
        // 入口项：显示"当前主题"和当前选中的主题名称
        DrawerItem(
            R.drawable.ic_theme,
            R.drawable.ic_arrow_down,
            rightIconModifier = Modifier.graphicsLayer {
                rotationZ = anim.value * 180
            },
            title = buildAnnotatedString {
                withStyle(style = SpanStyle(color = AppColorsProvider.current.firstText, fontSize = 32.csp)) {
                    append("当前主题")
                }
                withStyle(style = SpanStyle(color = AppColorsProvider.current.secondText, fontSize = 28.csp)) {
                    append("（${themeModels[lastSelectedThemeIndex].value.name}）")
                }
            },
        ) {
            // 点击切换展开/收起状态
            showThemeList = !showThemeList
            scope.launch {
                if (showThemeList) {
                    anim.animateTo(1f, tween(500))
                } else {
                    anim.animateTo(0f, tween(500))
                }
            }

        }
        // 主题列表：动画未完成或展开状态时显示
        if (anim.value != 0f || showThemeList) {
            themeModels.forEachIndexed { index, themeModel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.cdp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            // 更新全局主题类型
                            themeTypeState.value = themeModel.value.themeType
                            // 取消之前的选中状态
                            themeModels[lastSelectedThemeIndex].value = themeModels[lastSelectedThemeIndex].value.copy(selected = false)
                            // 更新最后选中的索引
                            lastSelectedThemeIndex = index
                            // 设置新的选中状态
                            themeModels[index].value = themeModels[index].value.copy(selected = true)
                        }
                        .padding(horizontal = 32.cdp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：主题颜色圆点
                    Box(
                        modifier = Modifier
                            .size(40.cdp)
                            .clip(RoundedCornerShape(50))
                            .background(themeModel.value.color)
                    )
                    // 中间：主题名称
                    Text(
                        text = themeModel.value.name,
                        modifier = Modifier.padding(start = 24.cdp),
                        fontSize = 32.csp,
                        color = AppColorsProvider.current.secondText
                    )
                    // 弹性空间，将右侧内容推到右边
                    Spacer(modifier = Modifier.weight(1f))
                    // 右侧：如果当前主题被选中，显示勾选图标
                    if (themeModels[index].value.selected) {
                        CommonIcon(
                            resId = R.drawable.ic_checked,
                            modifier = Modifier.size(32.cdp),
                            tint = themeModel.value.color
                        )
                    }
                }
            }
        }
    }
}

private var showThemeList by mutableStateOf(false)
private var lastSelectedThemeIndex = 0
private val themeModels = mutableStateListOf(
    mutableStateOf(ThemeModel("默认(跟随系统)", THEME_DEFAULT, DefaultColorPalette.primary, true)),
    mutableStateOf(ThemeModel("夜间", THEME_NIGHT, DarkColorPalette.pure, false)),
    mutableStateOf(ThemeModel("蓝色", THEME_BLUE, BlueColorPalette.primary, false)),
    mutableStateOf(ThemeModel("绿色", THEME_GREEN, GreenColorPalette.primary, false)),
    mutableStateOf(ThemeModel("橙色", THEME_ORIGIN, OriginColorPalette.primary, false)),
    mutableStateOf(ThemeModel("紫色", THEME_PURPLE, PurpleColorPalette.primary, false)),
    mutableStateOf(ThemeModel("黄色", THEME_YELLOW, YellowColorPalette.primary, false)),
)


data class ThemeModel(val name: String, val themeType: Int, val color: Color, val selected: Boolean)
