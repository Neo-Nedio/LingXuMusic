package com.neo.lingxumusic.ui.common.refresh.classic;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * 箭头 Drawable
 * 作用：下拉刷新时的指示器箭头，支持上下箭头切换（通过旋转实现）
 * 继承关系：ArrowDrawable → PaintDrawable → Drawable
 * 绘制逻辑：根据 Drawable 的宽高，动态计算箭头形状的路径
 *         箭头朝上（默认），需要朝下时通过外部旋转 180 度实现
 */
public class ArrowDrawable extends PaintDrawable {

    private int mWidth = 0;      // 缓存宽度，判断是否需要重建路径
    private int mHeight = 0;     // 缓存高度，判断是否需要重建路径
    private final Path mPath = new Path();  // 箭头路径

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Drawable drawable = ArrowDrawable.this;
        final Rect bounds = drawable.getBounds();
        final int width = bounds.width();
        final int height = bounds.height();

        // 尺寸变化时重新计算路径（第一次或宽高改变时）
        if (mWidth != width || mHeight != height) {
            // 计算箭头线条的宽度（占整体宽度的 30/225 ≈ 13.3%）
            int lineWidth = width * 30 / 225;
            mPath.reset();

            // 计算辅助向量（用于斜线长度换算）
            // 0.70710678 = sin(45°) ≈ √2/2，用于 45° 斜线的坐标转换
            float vector1 = (lineWidth * 0.70710678118654752440084436210485f);
            float vector2 = (lineWidth / 0.70710678118654752440084436210485f);

            // 从底部中心点开始绘制箭头（朝上）
            mPath.moveTo(width / 2f, height);                        // 1. 底部中心点
            mPath.lineTo(0, height / 2f);                            // 2. 左下角
            mPath.lineTo(vector1, height / 2f - vector1);            // 3. 左斜线内折
            mPath.lineTo(width / 2f - lineWidth / 2f, height - vector2 - lineWidth / 2f);  // 4. 底部左侧
            mPath.lineTo(width / 2f - lineWidth / 2f, 0);            // 5. 左上角
            mPath.lineTo(width / 2f + lineWidth / 2f, 0);            // 6. 右上角
            mPath.lineTo(width / 2f + lineWidth / 2f, height - vector2 - lineWidth / 2f);  // 7. 底部右侧
            mPath.lineTo(width - vector1, height / 2f - vector1);    // 8. 右斜线内折
            mPath.lineTo(width, height / 2f);                        // 9. 右下角
            mPath.close();                                           // 闭合回到底部中心点

            mWidth = width;
            mHeight = height;
        }

        // 使用父类的 mPaint 绘制路径
        canvas.drawPath(mPath, mPaint);
    }
}
