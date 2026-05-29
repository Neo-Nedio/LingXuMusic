package com.neo.lingxumusic.ui.common.refresh.drawable;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * 自定义 Drawable 的抽象基类
 * 作用：
 * 1. 封装 Paint 对象，简化子类的绘制代码
 * 2. 统一设置默认样式（填充、抗锯齿、灰色）
 * 3. 提供 setColor、setAlpha、setColorFilter 等常用方法
 * 子类：ArrowDrawable、ProgressDrawable
 */
public abstract class PaintDrawable extends Drawable {

    // 画笔对象，供子类使用
    public Paint mPaint = new Paint();

    /**
     * 构造函数：初始化画笔默认样式
     */
    protected PaintDrawable() {
        mPaint.setStyle(Paint.Style.FILL);      // 填充模式（不是描边）
        mPaint.setAntiAlias(true);              // 抗锯齿，边缘平滑
        mPaint.setColor(0xff666666);            // 默认颜色：深灰色（#666666）
    }

    /**
     * 设置画笔颜色
     * @param color ARGB 颜色值，如 0xffff0000 表示红色
     */
    public void setColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 设置画笔透明度
     * @param alpha 0（完全透明）~ 255（完全不透明）
     *
     * 重写父类方法，实现透明度控制
     */
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    /**
     * 设置颜色过滤器
     * @param cf 颜色过滤器（可用于改变颜色、混合模式等）
     *
     * 重写父类方法，实现颜色滤镜
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    /**
     * 返回 Drawable 的透明度/像素格式
     * @return TRANSLUCENT = 支持透明度（如 PNG 透明效果）
     * 其他可能返回值：
     * - OPAQUE：完全不透明
     * - TRANSPARENT：完全透明
     * - TRANSLUCENT：半透明（支持 Alpha 通道）
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
