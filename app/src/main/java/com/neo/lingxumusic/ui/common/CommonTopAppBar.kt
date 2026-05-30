package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.transformDp

/*
自定义的通用顶部导航栏组件，使用ConstraintLayout代替TopAppBar
//传统TopAppBar高度固定，部分手机字体过大可能会显示不全，导致文字被截断
┌─────────────────────────────────────┐
│  ← 返回按钮        标题        右侧按钮 │
├─────────────────────────────────────┤
│         （可选的分割线）               │
└─────────────────────────────────────┘*/
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

    // 左边按钮宽度
    var leftWidth by remember {
        mutableIntStateOf(1)
    }

    // 右边按钮宽度
    var rightWidth by remember {
        mutableIntStateOf(1)
    }

    Column(modifier = modifier) {
        // 顶部导航栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(appBarHeight) // 高度（按屏幕比例适配）
                .background(backgroundColor) // 导航栏背景色
                .zIndex(1f),  // 层级为 1，确保在其他内容之上
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                //创建了 4 个引用对象，每个引用对应一个子组件，用于在约束规则中标识该组件
                //ConstraintLayout 需要知道哪些组件之间要建立约束关系，引用就是这些组件的标识
                val (leftIcon, rightLayout, titleLayout, bottomDivider) = createRefs()

                Box(
                    modifier = Modifier
                        .constrainAs(leftIcon) {
                            start.linkTo(parent.start)      // 左边缘 对齐 父容器左边缘
                            end.linkTo(titleLayout.start)   // 右边缘 对齐 标题左边缘
                            top.linkTo(parent.top)          // 上边缘 对齐 父容器上边缘
                            bottom.linkTo(parent.bottom)    // 下边缘 对齐 父容器下边缘
                        }
                        .onGloballyPositioned {
                            leftWidth = it.size.width //记录宽度
                        }
                ) {
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
                            tint = contentColor // 左侧图标颜色
                        )
                    }
                }

                //右侧区域
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .constrainAs(rightLayout) {
                            start.linkTo(titleLayout.end)   // 左边缘 对齐 标题右边缘
                            end.linkTo(parent.end)          // 右边缘 对齐 父容器右边缘
                            top.linkTo(parent.top)          // 上边缘 对齐 父容器上边缘
                            bottom.linkTo(parent.bottom)    // 下边缘 对齐 父容器下边缘
                        }
                        .clickable(enabled = rightClick != null) { // 有点击回调时才可以点击
                            rightClick?.invoke()
                        }
                        .onGloballyPositioned {
                            rightWidth = it.size.width //记录宽度
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
                            tint = contentColor // 右侧图标颜色
                        )
                        // 最后：右侧文字
                        rightText.isNotBlank() -> MarqueeText(
                            text = rightText,
                            fontSize = 30.csp,
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            modifier = Modifier.padding(20.cdp)
                        )
                    }
                }

                // 根据左右区域宽度差，补偿标题 padding，使标题视觉居中
                val titleLeftPadding = if (leftWidth >= rightWidth) {
                    0
                } else {
                    rightWidth - leftWidth
                }

                val titleRightPadding = if (leftWidth < rightWidth) {
                    0
                } else {
                    leftWidth - rightWidth
                }

                //标题
                Box(
                    modifier = Modifier
                        // 第一层：动态补偿 padding（使标题视觉居中）
                        .padding(
                            start = titleLeftPadding.transformDp,   // 左侧补偿（可能为 0）
                            end = titleRightPadding.transformDp     // 右侧补偿（可能为 0）
                        )
                        // 第二层：固定内边距（保证标题不与左右按钮紧贴）
                        .padding(horizontal = 16.cdp)
                        // 第三层：约束规则
                        .constrainAs(titleLayout) {
                            start.linkTo(leftIcon.end)      // 左边缘对齐左按钮右边缘
                            end.linkTo(rightLayout.start)   // 右边缘对齐右按钮左边缘
                            top.linkTo(parent.top)          // 上边缘对齐父容器顶部
                            bottom.linkTo(parent.bottom)    // 下边缘对齐父容器底部
                            width = Dimension.fillToConstraints  // 宽度填满约束区域
                        }
                ) {
                    if (customTitleLayout != null) {
                        customTitleLayout.invoke()
                    } else {
                        MarqueeText(
                            text = title,
                            fontSize = 36.csp,  // 字体大小 36（按屏幕比例）
                            fontWeight = FontWeight.Medium,
                            textAlign = titleAlign,
                            color = contentColor, // 标题文字颜色
                            modifier = Modifier.fillMaxWidth() // 宽度占满，保证居中
                        )
                    }
                }

                // 底部分割线
                if (showBottomDivider) {
                    HorizontalDivider(
                        modifier = Modifier.constrainAs(bottomDivider) {
                            start.linkTo(parent.start)      // 左边缘 对齐 父容器左边缘
                            end.linkTo(parent.end)          // 右边缘 对齐 父容器右边缘
                            bottom.linkTo(parent.bottom)    // 下边缘 对齐 父容器下边缘
                            width = Dimension.fillToConstraints  // 宽度填满约束区域
                        },
                        color = Color(0xFFE5E5E5), // 浅灰色
                        thickness = 1.cdp,  // 厚度 1（按屏幕比例）
                    )
                }
            }
        }
    }
}
