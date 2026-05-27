package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

/**
 * @Description-> compose版本通用TabLayout
 * @param selectedIndex: 当前选中的tab索引
 * @param tabTexts： tab文本集合
 * @param style： 样式配置
 * @param modifier： 修饰
 * @param onTabSelected： 选中回调
 *
 * 在顶部显示一行 Tab 标签，支持点击切换，有选中指示器动画效果
 * ┌─────────────────────────────────────────┐
 * │  推荐  │  热歌  │  新歌  │  榜单  │  歌手  │
 * │      ●       （指示器）                   │
 * └─────────────────────────────────────────┘
 */
@Composable
fun CommonTabLayout(
    selectedIndex: Int = 0,
    tabTexts: List<String>, //Tab 文字列表
    backgroundColor: Color = AppColorsProvider.current.background,  // tab的背景颜色
    selectedTextColor: Color = AppColorsProvider.current.firstText,  // 选中tab字体颜色
    unselectedTextColor: Color = AppColorsProvider.current.secondText,  // 未选中tab字体颜色
    indicatorColor: Brush = Brush.horizontalGradient(// 指示器颜色
        listOf(AppColorsProvider.current.primary,
            AppColorsProvider.current.secondary)
    ),
    style: CommonTabLayoutStyle = CommonTabLayoutStyle(),
    onTabSelected: ((index: Int) -> Unit)? = null //	点击回调
) {
    if (style.isScrollable) {
        // Tab 可滚动
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = style.modifier,
            edgePadding = 0.dp, // 左右边缘内边距为 0
            containerColor = backgroundColor,
            //指示器实现
            indicator = @Composable {
                style.customIndicator?.invoke(selectedIndex) ?: Box(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedIndex) //自动跟随选中项移动
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(style.indicatorWidth)
                            .background(
                                brush = indicatorColor, // 渐变色画笔
                                shape = RoundedCornerShape(50)  // 圆角胶囊形状
                            ),
                        thickness = style.indicatorHeight, // 指示器高度（如 12dp）
                        color = Color.Transparent // ← 透明，让 background 透出来
                    )
                }

            },
            // 透明分割线
            divider = @Composable {
                HorizontalDivider(color = Color.Transparent)
            }
        ) {
            tabTexts.forEachIndexed { i, tabText ->
                //字体选择
                var fontWeight = FontWeight.Normal
                if (selectedIndex == i) {
                    if (style.selectedTextBold) {
                        fontWeight = FontWeight.Bold
                    }
                } else {
                    if (style.unselectedTextBold) {
                        fontWeight = FontWeight.Bold
                    }
                }

                //主体
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {  // 手势检测
                            detectTapGestures(
                                onTap = {
                                    onTabSelected?.invoke(i) // 点击时回调
                                }
                            )
                        }
                        .zIndex(1f)  // 层级为 1，确保在其他元素之上
                        .drawBehind { // 自定义绘制，在 Box 背后绘制自定义内容（如背景、边框等）
                            style.tabItemDrawBehindBlock?.invoke(this, i)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabText,
                        fontSize = if (selectedIndex == i) style.selectedTextSize else style.unselectedTextSize,
                        fontWeight = fontWeight,
                        color = if (selectedIndex == i) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center,
                    )
                }

            }
        }
    } else {
        // Tab 平分宽度
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = style.modifier,
            containerColor = backgroundColor,
            indicator = @Composable {
                style.customIndicator?.invoke(selectedIndex) ?: Box(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedIndex)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(bottom = 20.cdp)
                            .width(style.indicatorWidth)
                            .background(
                                brush = indicatorColor,
                                shape = RoundedCornerShape(50)
                            ),
                        thickness = style.indicatorHeight,
                        color = Color.Transparent
                    )
                }

            },
            divider = @Composable {
                HorizontalDivider(color = Color.Transparent)
            }
        ) {
            tabTexts.forEachIndexed { i, tabText ->
                var fontWeight = FontWeight.Normal
                if (selectedIndex == i) {
                    if (style.selectedTextBold) {
                        fontWeight = FontWeight.Bold
                    }
                } else {
                    if (style.unselectedTextBold) {
                        fontWeight = FontWeight.Bold
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onTabSelected?.invoke(i)
                                }
                            )
                        }
                        .zIndex(1f)
                        .drawBehind {
                            style.tabItemDrawBehindBlock?.invoke(this, i)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabText,
                        fontSize = if (selectedIndex == i) style.selectedTextSize else style.unselectedTextSize,
                        fontWeight = fontWeight,
                        color = if (selectedIndex == i) selectedTextColor else unselectedTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 通用TabBar样式
 */
data class CommonTabLayoutStyle(
    val modifier: Modifier = Modifier, // 修饰
    val selectedTextSize: TextUnit = 36.csp,  // 选中tab字体大小
    val unselectedTextSize: TextUnit = 36.csp,  // 未选中tab字体大小
    val selectedTextBold: Boolean = true,  // 选中tab字体加粗
    val unselectedTextBold: Boolean = false, // 未选中tab字体加粗
    val indicatorWidth: Dp = 140.cdp,  // 指示器宽度
    val indicatorHeight: Dp = 12.cdp,  // 指示器高度
    val isScrollable: Boolean = true,  // 是否可滑动
    val tabItemDrawBehindBlock: (DrawScope.(position: Int) -> Unit)? = null, //为每个tab在 Box 背后绘制自定义内容（如背景、边框等）
    val customIndicator: @Composable ((selectedPosition: Int) -> Unit)? = null  // 自定义指示器
)
