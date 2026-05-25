package com.neo.lingxumusic.ui.common

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

//todo
/*
可拖拽的布局组件，实现头部背景固定、主体内容可上下拖拽的效果
┌─────────────────────────────────┐
│      头部背景（固定位置）          │  ← 始终在顶部
├─────────────────────────────────┤
│                                 │
│      主体内容（可拖拽）            │  ← 可以上下滑动
│                                 │
│                                 │
└─────────────────────────────────┘*/
private const val DragMultiplier = 0.6f

@Composable
fun rememberDragToggleState(
    dragStatus: DragStatus
): DragToggleState {
    return remember {
        DragToggleState(dragStatus = dragStatus)
    }
}

//三种拖拽状态
sealed class DragStatus {
    object Idle : DragStatus()           // 闲置状态（收起）
    object OverOpenTrigger : DragStatus() // 超过触发阈值（松手会打开）
    object Opened : DragStatus()          // 完全打开状态
}

//拖拽状态管理器
@Stable
class DragToggleState(
    dragStatus: DragStatus
) {
    private val _offset = Animatable(0f)      // 当前拖拽偏移量（可动画）
    val offset: Float get() = _offset.value   // 只读偏移量

    private val mutatorMutex = MutatorMutex() // 互斥锁，防止动画冲突

    var dragStatus: DragStatus by mutableStateOf(dragStatus) // 当前状态（UI 会响应变化）
    var isDraggableInProgress: Boolean by mutableStateOf(false) // 是否正在拖拽中

    //动画方法
    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate { //互斥锁，防止动画冲突
            _offset.animateTo(offset, tween(300))  // 300ms 缓动动画
        }
    }

    //拖拽方法
    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) { //用户输入优先级更高，可以打断正在执行的动画
            _offset.snapTo(_offset.value + delta)  // 直接增加偏移量
        }
    }

    //状态判断方法
    fun isIdle() = dragStatus == DragStatus.Idle
    fun isOverOpenTrigger() = dragStatus == DragStatus.OverOpenTrigger
    fun isOpened() = dragStatus == DragStatus.Opened

}

//嵌套滚动连接器
private class DragToggleNestedScrollConnection(
    private val state: DragToggleState,           // 拖拽状态管理器
    private val coroutineScope: CoroutineScope,   // 协程作用域
    private val onOverOpenTrigger: () -> Unit,    // 超过触发点的回调
) : NestedScrollConnection {
    var enabled: Boolean = false      // 是否启用拖拽
    var openTrigger: Float = 0f       // 触发阈值（px）

    // 处理向上滑动
    override fun onPreScroll(
        available: Offset,      // 可滚动的距离（负值表示向上滑）
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero                                    // 未启用
        source == NestedScrollSource.UserInput && available.y < 0 -> onScroll(available)  //向上滑动（负值）
        else -> Offset.Zero
    }

    //处理向下滑动
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource //剩余距离	子组件滚动后没消耗完的部分
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.UserInput && available.y > 0 -> onScroll(available) //向下滑动（正值）
        else -> Offset.Zero
    }

    //核心滚动处理
    private fun onScroll(available: Offset): Offset {
        // 标记正在拖拽（用于后续判断是否要执行动画）
        state.isDraggableInProgress = true

        // 计算新偏移量（带阻尼系数）
        // available.y 是滚动距离，乘以阻尼系数产生"粘滞"感
        // coerceAtLeast(0f) 确保不会变成负数
        val newOffset = (available.y * DragMultiplier + state.offset).coerceAtLeast(0f)


        // 实际消耗的滚动距离
        val dragConsumed = newOffset - state.offset

        //判断是否足够明显，过滤掉小于 0.5px 的微小移动，避免频繁刷新
        return if (dragConsumed.absoluteValue >= 0.5f) {
            // 启动协程更新偏移量
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            // 返回实际消耗的滚动距离（回传给系统）
            Offset(x = 0f, y = dragConsumed / DragMultiplier)
        } else {
            Offset.Zero
        }
    }

    //松手处理
    override suspend fun onPreFling(available: Velocity): Velocity {
        // 如果已经超过了触发点，调用回调
        if (!state.isOverOpenTrigger() && state.offset >= openTrigger) {
            onOverOpenTrigger()
        }

        // 重置拖拽标记
        state.isDraggableInProgress = false

        // 不消耗任何速度，让内容正常 fling
        //Velocity.Zero 是一个零速度常量，表示没有速度、不滚动
        return Velocity.Zero
    }
}

/*
┌─────────────────────────────────────┐
│  Box（整体容器，支持嵌套滚动）         │
│  ┌─────────────────────────────────┐│
│  │  头部背景（固定位置）              ││  ← 不随拖拽移动
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │  主体内容（可拖拽偏移）            ││  ← 随拖拽移动
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘

一、初始化阶段
FixHeadBackgroundDraggableBodyLayout 启动
    ↓
remember { mutableStateOf(1) }  ← backgroundHeight 初始为 1
    ↓
计算 openTriggerPx = backgroundHeight × triggerRadio
计算 maxDrag = backgroundHeight × maxDragRadio
    ↓
创建 nestedScrollConnection
    ↓
布局渲染
    ├── Box1（头部背景容器）: onGloballyPositioned 测量高度 → 更新 backgroundHeight
    │       ↓
    │   backgroundHeight 更新 → openTriggerPx 和 maxDrag 重新计算
    │       ↓
    │   headBackgroundComponent 渲染
    │
    └── Box2（主体内容）: offset 初始为 0 → content 渲染




二、用户拖拽流程
用户手指触摸屏幕并滑动
    ↓
LazyColumn 等子组件滚动
    ↓
NestedScrollConnection 拦截滚动事件
    ↓
┌─────────────────────────────────────────────────────────────┐
│  onPostScroll（向下滑动） / onPreScroll（向上滑动）          │
│       ↓                                                     │
│  检查: enabled ?  source == Drag ?  方向正确 ?              │
│       ↓ 是                                                 │
│  调用 onScroll(available)                                  │
└─────────────────────────────────────────────────────────────┘
    ↓
onScroll(available):
    ├── state.isDraggableInProgress = true  ← 标记正在拖拽
    ├── newOffset = (available.y × 0.6 + state.offset).coerceAtLeast(0)
    ├── dragConsumed = newOffset - state.offset
    └── 如果 |dragConsumed| >= 0.5f:
            ├── 启动协程: state.dispatchScrollDelta(dragConsumed)
            │       ↓
            │   state._offset.snapTo(_offset.value + delta)  ← 立即更新偏移量
            │
            └── 返回 Offset(0, dragConsumed / 0.6)
    ↓
state.offset 变化 → UI 重组
    ↓
Box2（主体内容）的 Modifier.offset 读取新 offset
    ↓
主体内容向下移动（向下拖拽）或向上移动（向上拖拽）


三、拖拽过程中的状态变化
拖拽开始
    ↓
state.offset 从 0 开始增加
    ↓
┌─────────────────────────────────────────────────────────────┐
│  检查是否超过触发点                                         │
│                                                             │
│  if (offset >= openTriggerPx && !state.isOverOpenTrigger()) │
│      state.dragStatus = OverOpenTrigger                     │
│      调用 onOverOpenTrigger() 回调                          │
└─────────────────────────────────────────────────────────────┘
    ↓
继续拖拽
    ↓
┌─────────────────────────────────────────────────────────────┐
│  检查是否完全打开                                           │
│                                                             │
│  if (!isDraggableInProgress && offset == maxDrag && isOverOpenTrigger())
│      onOpened() 回调                                        │
│      state.dragStatus = Opened                              │
└─────────────────────────────────────────────────────────────┘

四、松手动画流程
用户抬起手指
    ↓
onPreFling 被调用
    ├── 检查: 超过触发点 ? → 调用 onOverOpenTrigger()
    ├── state.isDraggableInProgress = false  ← 清除拖拽标记
    └── 返回 Velocity.Zero（不拦截惯性滚动）
    ↓
LaunchedEffect(state.isDraggableInProgress, state.dragStatus) 触发
    ↓
检查: !state.isDraggableInProgress ?  ← 不再拖拽中
    ↓ 是
when (state.dragStatus) {
    ┌─────────────────────────────────────────────────────────┐
    │  Idle           → state.animateOffsetTo(0f)            │
    │                   ↓                                    │
    │              弹回顶部，内容回到原位                      │
    ├─────────────────────────────────────────────────────────┤
    │  OverOpenTrigger → state.animateOffsetTo(maxDrag)      │
    │                   ↓                                    │
    │              动画展开到最大，露出完整头部背景            │
    │                   ↓                                    │
    │              dragStatus = Opened                       │
    ├─────────────────────────────────────────────────────────┤
    │  Opened         → state.animateOffsetTo(0f)            │
    │                   ↓                                    │
    │              收起内容，回到初始状态                      │
    │                   ↓                                    │
    │              dragStatus = Idle                         │
    └─────────────────────────────────────────────────────────┘
}
*/
@Composable
fun FixHeadBackgroundDraggableBodyLayout(
    state: DragToggleState, //拖拽状态管理器
    onOverOpenTrigger: () -> Unit, //超过触发阈值回调
    onOpened: () -> Unit, //完全打开回调
    dragEnabled: Boolean = true, //是否启用拖拽
    modifier: Modifier = Modifier,
    triggerRadio: Float = 0.6f, //触发阈值比例（0-1）
    maxDragRadio: Float = 1f, //最大拖拽距离比例（0-1）
    headBackgroundComponent: @Composable (state: DragToggleState, trigger: Float, maxDrag: Float) -> Unit, //	头部背景组件
    content: @Composable () -> Unit, //主体内容组件
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnOverOpenTrigger = rememberUpdatedState(onOverOpenTrigger)
    var backgroundHeight by remember {
        mutableStateOf(1)
    }
    val openTriggerPx = backgroundHeight * triggerRadio    // 触发阈值（像素）
    val maxDrag = backgroundHeight * maxDragRadio           // 最大拖拽距离（像素）

    //打开完成检测： 没有在拖拽中 偏移量已达到最大值  状态是"超过触发点"
    if(!state.isDraggableInProgress && maxDrag == state.offset && state.isOverOpenTrigger()) {
        onOpened()
    }

    //松手动画
    LaunchedEffect(state.isDraggableInProgress, state.dragStatus) {
        if (!state.isDraggableInProgress) {
            when (state.dragStatus) {
                DragStatus.Idle -> state.animateOffsetTo(0f)           // 弹回顶部
                DragStatus.OverOpenTrigger -> state.animateOffsetTo(maxDrag) // 展开到最大
                DragStatus.Opened -> state.animateOffsetTo(0f)         // 收起
            }
        }
    }

    // 嵌套滚动连接
    val nestedScrollConnection = remember(state, coroutineScope) {
        DragToggleNestedScrollConnection(state, coroutineScope) {
            updatedOnOverOpenTrigger.value.invoke()
        }
    }.apply {
        this.enabled = dragEnabled
        this.openTrigger = openTriggerPx
    }

    //布局
    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        // 头部背景（固定，用于测量高度和显示背景）
        //offest控制主体区域位置，当主体区域向下时，头部背景更大
        Box(
            Modifier
                .onGloballyPositioned { //测量头部高度
                    backgroundHeight = it.size.height
                }
        ) {
            Box(Modifier.align(Alignment.TopCenter)) {
                headBackgroundComponent(state, openTriggerPx, maxDrag)
            }
        }
        //让主体内容发生偏移
        Box(
            modifier = Modifier.offset { //offset是偏移量，为0时背景图片也显示
                IntOffset(0, state.offset.toInt().coerceAtMost(maxDrag.toInt()))
            },
        ) {
            content()
        }
    }
}