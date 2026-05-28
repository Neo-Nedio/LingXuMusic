package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

/*
自定义的通用顶部导航栏组件，封装了 TopAppBar
┌─────────────────────────────────────┐
│  ← 返回按钮        标题        右侧按钮 │
├─────────────────────────────────────┤
│         （可选的分割线）               │
└─────────────────────────────────────┘*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    modifier: Modifier = Modifier,
    title: String = "",
    titleAlign: TextAlign = TextAlign.Center,
    customTitleLayout: (@Composable () -> Unit)? = null,
    appBarHeight: Dp = 88.cdp,
    backgroundColor: Color = AppColorsProvider.current.appBarBackground,
    contentColor: Color = AppColorsProvider.current.appBarContent,
    leftIconResId: Int = R.drawable.ic_back,
    rightIconResId: Int = -1,
    rightText: String = "",
    customRightLayout: (@Composable () -> Unit)? = null, //完全自定义右侧区域
    leftClick: (() -> Unit)? = null,
    rightClick: (() -> Unit)? = null,
    showBottomDivider: Boolean = false, //是否显示底部分割线
) {
    Column(modifier = modifier) {
        // 顶部导航栏
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(appBarHeight) // 高度（按屏幕比例适配）
                .zIndex(1f),  // 层级为 1，确保在其他内容之上
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor,           // 导航栏背景色
                titleContentColor = contentColor,           // 标题文字颜色
                navigationIconContentColor = contentColor,  // 左侧图标颜色
                actionIconContentColor = contentColor       // 右侧图标颜色
            ),
            navigationIcon = {
                if (leftIconResId != -1) { // 如果左侧资源ID有效
                    CommonIcon(
                        leftIconResId,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                // 优先使用自定义点击，否则默认返回上一页
                                leftClick?.invoke() ?: NavController.instance.popBackStack()
                            }
                            .padding(20.cdp)
                            .size(48.cdp),
                        tint = contentColor
                    )
                }
            },
            //标题
            title = {
                customTitleLayout?.invoke()?: Text(
                    text = title,
                    fontSize = 36.csp,  // 字体大小 36（按屏幕比例）
                    fontWeight = FontWeight.Medium,
                    textAlign = titleAlign,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth() // 宽度占满，保证居中
                )
            },
            //右侧区域
            actions = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(enabled = rightClick != null) { // 有点击回调时才可以点击
                            rightClick?.invoke()
                        },
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    when {
                        //优先级最高：自定义布局
                        customRightLayout != null -> customRightLayout.invoke()
                        //其次：右侧图标
                        rightIconResId != -1 -> CommonIcon(
                            rightIconResId,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    rightClick?.invoke()
                                }
                                .padding(20.cdp)
                                .size(48.cdp),
                            tint = contentColor
                        )
                        // 最后：右侧文字
                        rightText.isNotBlank() -> Text(
                            text = rightText,
                            fontSize = 30.csp,
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            maxLines = 1,
                            modifier = Modifier.padding(20.cdp)
                        )
                    }
                }
            }
        )

        // 底部分割线
        if (showBottomDivider) {
            HorizontalDivider(
                color = Color(0xFFE5E5E5), // 浅灰色
                thickness = 1.cdp)  // 厚度 1（按屏幕比例）
        }
    }
}

