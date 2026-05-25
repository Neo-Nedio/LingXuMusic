package com.neo.lingxumusic.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

// 缓存屏幕信息，避免重复获取
private val screenWidthDp by lazy {
    //像素/密度 = dp
    Resources.getSystem().displayMetrics.widthPixels / Resources.getSystem().displayMetrics.density
}

//设计稿宽度
private const val designWidth = 750f

/**
 * compose屏幕适配单位
 */
val Number.cdp
    get() = Dp(
        toFloat() * //toFloat是Number本身，有上下文
                screenWidthDp / designWidth
    )

val Dp.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,      // 单位：dp
               value,                            // dp 值
               Resources.getSystem().displayMetrics  // 屏幕密度信息
    )

/**
 * compose屏幕适配单位（字体专用）
 */
val Number.csp
    get() = (toFloat() *
            Resources.getSystem().displayMetrics.widthPixels
            / 750
            / Resources.getSystem().configuration.fontScale).sp


/**
 * 将数字转换成compose中的DP
 */
val Number.transformDp
    get() = Dp(toFloat() / Resources.getSystem().displayMetrics.density)

