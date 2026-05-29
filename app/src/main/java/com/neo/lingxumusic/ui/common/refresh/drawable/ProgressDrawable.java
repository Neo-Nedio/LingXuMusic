package com.neo.lingxumusic.ui.common.refresh.drawable;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * 自定义加载进度 Drawable（转圈动画）
 * 效果：12 个逐渐变淡的小圆点围成一圈，轮流高亮，形成旋转效果
 * 使用场景：下拉刷新时的加载动画、底部加载更多的加载动画
 * 继承关系：ProgressDrawable → PaintDrawable → Drawable
 * 实现接口：Animatable（提供 start/stop/isRunning 控制动画）
 *          ValueAnimator.AnimatorUpdateListener（监听动画进度）
 */
/*
旋转方向 →

        ●           (i=0, 最亮)
        ●   ●         (i=1)
        ●     ●        (i=2)
        ●       ●       (i=3)
        ●       ●       (i=4)
        ●     ●        (i=5)
        ●   ●         (i=6)
        ●           (i=7, 最暗)

实际有 12 个点，每个间隔 30 度，旋转时视觉上像一圈点在依次高亮*/
public class ProgressDrawable extends PaintDrawable implements Animatable, ValueAnimator.AnimatorUpdateListener {

    // 记录 Drawable 的宽高（用于判断是否需要重新构建路径）
    protected int mWidth = 0;
    protected int mHeight = 0;

    // 当前旋转角度（0-3600，每 30 度一个刻度，共 12 个位置）
    protected int mProgressDegree = 0;

    // 值动画器（控制旋转角度从 30 到 3600，无限循环）
    protected ValueAnimator mValueAnimator;

    // 单个元素的绘制路径（一个圆 + 一个矩形组成的形状）
    protected Path mPath = new Path();

    /**
     * 构造函数：初始化动画器
     */
    public ProgressDrawable() {
        // 动画值从 30 到 3600（3600 / 30 = 120 个刻度，每个刻度 30 度）
        // 不是从 0 开始，避免初始时看不到动画
        mValueAnimator = ValueAnimator.ofInt(30, 3600);
        mValueAnimator.setDuration(10000);      // 完整一圈 10 秒
        mValueAnimator.setInterpolator(null);    // 匀速动画
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);  // 无限循环
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);    // 每次循环重新开始
    }

    /**
     * 动画进度更新回调
     * @param animation 动画器
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        // 按 30 的倍数取整（0, 30, 60, 90...），使动画一格一格跳，而不是连续旋转
        mProgressDegree = 30 * (value / 30);
        // 触发重绘
        invalidateSelf();
    }

    /**
     * 绘制方法
     * @param canvas 画布
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        final Drawable drawable = ProgressDrawable.this;
        final Rect bounds = drawable.getBounds();
        final int width = bounds.width();
        final int height = bounds.height();
        // 计算单个元素的半径（宽度 / 22，至少为 1px）
        final float r = Math.max(1f, width / 22f);

        // 尺寸变化时重新构建绘制路径（第一次或宽高改变时）
        if (mWidth != width || mHeight != height) {
            mPath.reset();
            // 画一个圆（右侧的小圆点）
            mPath.addCircle(width - r, height / 2f, r, Path.Direction.CW);
            // 画一个矩形（连接圆和中心的矩形）
            mPath.addRect(width - 5 * r, height / 2f - r, width - r, height / 2f + r, Path.Direction.CW);
            // 再画一个圆（中心的大圆点？）
            mPath.addCircle(width - 5 * r, height / 2f, r, Path.Direction.CW);
            mWidth = width;
            mHeight = height;
        }

        // 保存画布状态
        canvas.save();
        // 绕中心旋转当前角度
        canvas.rotate(mProgressDegree, (width) / 2f, (height) / 2f);

        // 绘制 12 个元素（每隔 30 度一个）
        for (int i = 0; i < 12; i++) {
            // 设置透明度：越靠前的元素越亮（i=0 最亮，i=11 最暗）
            // (0+5)*0x11 = 5*17 = 85（半透明）
            // (11+5)*0x11 = 16*17 = 272（但 272>255，实际为 255，完全透明？）
            mPaint.setAlpha((i + 5) * 0x11);
            // 旋转 30 度
            canvas.rotate(30, (width) / 2f, (height) / 2f);
            // 绘制当前元素
            canvas.drawPath(mPath, mPaint);
        }

        // 恢复画布状态
        canvas.restore();
    }

    /**
     * 开始动画
     */
    @Override
    public void start() {
        if (!mValueAnimator.isRunning()) {
            mValueAnimator.addUpdateListener(this);
            mValueAnimator.start();
        }
    }

    /**
     * 停止动画
     */
    @Override
    public void stop() {
        if (mValueAnimator.isRunning()) {
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
        }
    }

    /**
     * 是否正在运行
     */
    @Override
    public boolean isRunning() {
        return mValueAnimator.isRunning();
    }
}