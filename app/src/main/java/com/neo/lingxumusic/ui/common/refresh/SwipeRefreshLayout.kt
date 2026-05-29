package com.neo.lingxumusic.ui.common.refresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.ui.common.refresh.classic.header.ClassicSwipeRefreshIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.absoluteValue

//拖动阻尼系数：用户拖动距离与实际偏移量的比例
//例如：用户下拉100px，指示器只移动50px，产生弹簧感
private const val DragMultiplier = 0.5f

@Composable
// Compose 函数，用于创建和记忆 SwipeRefreshState 实例
fun rememberSwipeRefreshState(
    type: SwipeRefreshStateType
): SwipeRefreshState {
    return remember(type) {  // 添加 key，当 type 变化时重新创建
        SwipeRefreshState(type = type)
    }
}

sealed class SwipeRefreshStateType {
    object IDLE : SwipeRefreshStateType()      // 空闲
    object REFRESHING : SwipeRefreshStateType() // 刷新中
    object SUCCESS : SwipeRefreshStateType()    // 刷新成功
    object FAIL : SwipeRefreshStateType()       // 刷新失败
}

// 下拉刷新状态管理类
@Stable
class SwipeRefreshState(
    type: SwipeRefreshStateType,
) {
    // 指示器偏移量动画器，初始为0
    private val _indicatorOffset = Animatable(0f)

    // 动画互斥锁，防止动画冲突
    private val mutatorMutex = MutatorMutex()

    // 当前刷新状态，变化时自动重组UI
    var type: SwipeRefreshStateType by mutableStateOf(type)

    // 是否正在拖拽中，仅模块内部可修改
    var isSwipeInProgress: Boolean by mutableStateOf(false)
        internal set

    // 获取当前指示器偏移量（只读）
    val indicatorOffset: Float get() = _indicatorOffset.value

    // 动画方式移动到指定偏移量
    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset, tween(300))
        }
    }

    // 立即增加偏移量（无动画），用于实时跟随手指拖拽
    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }

    // 判断当前是否正在刷新
    fun isRefreshing() = type == SwipeRefreshStateType.REFRESHING

    // 判断当前是否空闲
    fun isIdle() = type == SwipeRefreshStateType.IDLE

    // 判断当前是否刷新成功
    fun isSuccess() = type == SwipeRefreshStateType.SUCCESS

    // 判断当前是否刷新失败
    fun isFail() = type == SwipeRefreshStateType.FAIL

    // 立即重置偏移量为0
    suspend fun resetOffset() {
        _indicatorOffset.snapTo(0f)
    }
}

// 嵌套滚动连接器，处理下拉刷新的手势交互逻辑
private class SwipeRefreshNestedScrollConnection(
    private val state: SwipeRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    var enabled: Boolean = false  // 是否启用下拉刷新
    var refreshTrigger: Float = 0f  // 触发刷新的阈值（像素）
    private var lastSwipeInProgressChangeTimeStamp = 0L  // 上次拖拽状态变化的时间戳
    private val MIN_SWIPE_CHANGE_TIME = 20  // 最小拖拽变化时间（毫秒），用于防抖

    // 预滚动：优先拦截向下的滚动（下拉）
    //available.y < 0：手指往上滑 → 内容想往下滚（正常看下面的内容）
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero  // 未启用，不处理
        !state.isIdle() -> Offset.Zero  // 非空闲状态，不处理
        source == NestedScrollSource.UserInput && available.y < 0 -> onScroll(available)  // 用户下拉
        else -> Offset.Zero
    }

    // 后滚动：子组件滚动到顶后，处理剩余的向上滚动
    //available.y > 0：手指往下滑 → 内容想往上滚（列表在顶部时继续下拉）
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        !state.isIdle() -> Offset.Zero
        source == NestedScrollSource.UserInput && available.y > 0 -> onScroll(available)  // 向上回弹
        else -> Offset.Zero
    }

    // 核心滚动逻辑：计算并更新指示器偏移量
    private fun onScroll(available: Offset): Offset {
        lastSwipeInProgressChangeTimeStamp = Date().time  // 记录时间戳
        state.isSwipeInProgress = true  // 标记正在拖拽

        // 计算新偏移量，应用阻尼系数（0.5），最小为0
        val newOffset = (available.y * DragMultiplier + state.indicatorOffset).coerceAtLeast(0f)
        val dragConsumed = newOffset - state.indicatorOffset  // 实际消耗的拖动距离

        return if (dragConsumed.absoluteValue >= 0.5f) {
            // 更新偏移量
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            // 返回已消费的滚动距离
            Offset(x = 0f, y = dragConsumed / DragMultiplier)
        } else {
            Offset.Zero
        }
    }

    // 处理惯性滚动结束（松手时调用）
    override suspend fun onPreFling(available: Velocity): Velocity {
        val curTimeStamp = Date().time
        val timeDiff = curTimeStamp - lastSwipeInProgressChangeTimeStamp

        // 快速滑动时的防抖处理
        if (lastSwipeInProgressChangeTimeStamp != 0L && timeDiff <= MIN_SWIPE_CHANGE_TIME) {
            lastSwipeInProgressChangeTimeStamp = curTimeStamp
            coroutineScope.launch(Dispatchers.IO) {
                val delay = MIN_SWIPE_CHANGE_TIME - timeDiff
                delay(delay)  // 延迟执行，避免误触发

                if (state.isIdle() && state.indicatorOffset >= refreshTrigger) {
                    onRefresh()  // 触发刷新
                }
                state.isSwipeInProgress = false // 标记不在拖拽
            }
        } else {
            // 正常滑动
            lastSwipeInProgressChangeTimeStamp = curTimeStamp
            if (state.isIdle() && state.indicatorOffset >= refreshTrigger) {
                onRefresh()  // 触发刷新
            }
            state.isSwipeInProgress = false // 标记不在拖拽
        }

        return Velocity.Zero  // 消费所有速度，不传递，让内容列表都不会产生惯性滚动，保证刷新体验的流畅性
    }
}

// 下拉刷新布局组件
@Composable
fun SwipeRefreshLayout(
    state: SwipeRefreshState,  // 刷新状态
    onRefresh: () -> Unit,  // 刷新回调
    onIdle: () -> Unit,  // 空闲回调
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,  // 是否启用下拉刷新
    refreshTriggerRadio: Float = 1.0f,  // 触发阈值比例（相对于指示器高度）
    maxDragRadio: Float = 2f,  // 最大拖动比例
    indicatorHeight: Dp = 60.dp,  // 指示器高度
    indicator: @Composable (state: SwipeRefreshState, refreshTrigger: Float, maxDrag: Float) -> Unit = { state, trigger, maxDrag ->
        ClassicSwipeRefreshIndicator(state, trigger, maxDrag)  // 默认指示器
    },
    content: @Composable () -> Unit,  // 主内容
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)  // 避免重组时重新创建
    val indicatorHeightPx = with(LocalDensity.current) { indicatorHeight.toPx() }  // 转为像素
    val refreshTriggerPx = indicatorHeightPx * refreshTriggerRadio  // 触发刷新阈值
    val maxDrag = indicatorHeightPx * maxDragRadio  // 最大拖动距离

    // 处理指示器偏移量动画
    if (swipeEnabled) {
        HandleSwipeIndicatorOffset(state, indicatorHeightPx, onIdle)
    }

    // 创建嵌套滚动连接器
    val nestedScrollConnection = remember(state, coroutineScope) {
        SwipeRefreshNestedScrollConnection(state, coroutineScope) {
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.enabled = swipeEnabled
        this.refreshTrigger = refreshTriggerPx
    }

    // 布局：指示器在上层，内容在下层
    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {

        // 上层：指示器（固定在顶部）
        Box(Modifier.align(Alignment.TopCenter)
            //clipToBounds()：裁剪超出边界的内容
            .let { if (isHeaderNeedClip(state, indicatorHeightPx)) it.clipToBounds() else it }) {
            indicator(state, refreshTriggerPx, maxDrag)
        }

        // 下层：主内容（随偏移量下移
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        0,
                        state.indicatorOffset
                            .toInt()
                            .coerceAtMost(maxDrag.toInt()) // 限制最大偏移量
                    )
                },
        ) {
            content()
        }

    }
}

// 判断指示器是否需要裁剪（偏移量小于高度时裁剪，防止超出）
// 防止指示会穿透到上方，出现在屏幕边界之外，造成视觉错乱
private fun isHeaderNeedClip(state: SwipeRefreshState, indicatorHeight: Float): Boolean {
    return state.indicatorOffset < indicatorHeight
}

// 处理指示器偏移量的动画逻辑
@Composable
fun HandleSwipeIndicatorOffset(state: SwipeRefreshState, indicatorHeightPx: Float, onIdle: () -> Unit) {
    LaunchedEffect(state.isSwipeInProgress, state.type) {
        if (!state.isSwipeInProgress) {  // 不在拖拽中时才执行动画
            when (state.type) {
                // 刷新中：指示器移动到固定高度
                SwipeRefreshStateType.REFRESHING -> {
                    if (state.indicatorOffset != indicatorHeightPx) {
                        state.animateOffsetTo(indicatorHeightPx)
                    }
                }
                // 空闲：指示器归位
                SwipeRefreshStateType.IDLE -> {
                    if (state.indicatorOffset != 0f) {
                        state.animateOffsetTo(0f)
                    }
                }
                // 成功或失败：先移到固定高度显示结果，停留后归位
                SwipeRefreshStateType.SUCCESS, SwipeRefreshStateType.FAIL -> {
                    state.animateOffsetTo(indicatorHeightPx)
                    delay(50)  // 停留50ms让用户看到结果
                    if (state.indicatorOffset != 0f) {
                        state.animateOffsetTo(0f)
                        onIdle.invoke()  // 回调通知重置状态
                    }
                }
            }
        }
    }
}

