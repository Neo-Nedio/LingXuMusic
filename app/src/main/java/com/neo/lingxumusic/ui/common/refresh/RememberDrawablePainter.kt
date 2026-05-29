package com.neo.lingxumusic.ui.common.refresh

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import com.google.accompanist.drawablepainter.DrawablePainter

/**
 * 将 Android Drawable 转换为 Compose Painter
 * @param drawable 要转换的 Android Drawable 对象（如 ArrowDrawable、ProgressDrawable）
 * @return Painter 可在 Compose 中使用的 Painter 对象（如 Image 组件的 painter 参数）
 *
 * 作用：
 * 让 Compose 能够使用自定义的 Android Drawable（特别是动画 Drawable）
 *
 * remember(drawable) 的作用：
 * - 当 drawable 不变时，复用之前创建的 Painter，避免重复创建
 * - drawable.mutate() 确保每个 Painter 有独立的 Drawable 实例，避免状态共享
 */
@Composable
fun rememberDrawablePainter(drawable: Drawable): Painter {
    // drawable.mutate() ： 让每个 Painter 有独立的 Drawable 实例
    return remember(drawable) { DrawablePainter(drawable.mutate()) }
}
