package com.neo.lingxumusic.core.viewState

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshLayout
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshStateType
import com.neo.lingxumusic.ui.common.refresh.footer.CommonLoadFooter
import com.neo.lingxumusic.ui.common.refresh.rememberSwipeRefreshState
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.csp


// 通用列表组件，支持页面状态切换、下拉刷新、上拉加载更多
@Composable
fun <T : Any> ViewStateListPagingComponent(
    modifier: Modifier = Modifier,
    enableRefresh: Boolean = true,  // 是否允许下拉刷新
    showNoMoreDataFooter: Boolean = true,  // 没有更多数据时，是否显示footer
    collectAsLazyPagingItems: LazyPagingItems<T>,  // 分页数据
    specialRetryBlock: (() -> Unit)? = null,  // 重试代码块，默认执行refresh()
    specialRefreshBlock: (() -> Unit)? = null,  // 刷新代码块，默认执行refresh()
    lifeCycleListener: ComposeLifeCycleListener? = null,  // 生命周期监听
    lazyListContentPadding: PaddingValues = PaddingValues(0.dp),
    lazyListState: LazyListState = rememberLazyListState(),
    viewStateComponentModifier: Modifier = Modifier.fillMaxSize(),
    viewStateContentAlignment: Alignment = Alignment.Center,
    customEmptyComponent: @Composable (() -> Unit)? = null,  // 自定义空布局
    customFailComponent: @Composable (() -> Unit)? = null,  // 自定义失败布局
    listContent: LazyListScope.() -> Unit,  // 正常页面内容
) {

    //// 生命周期监听处理
    lifeCycleListener?.let { listener ->
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(Unit) {
            listener.onEnterCompose(lifecycleOwner)

            val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        listener.onCreate(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_START -> {
                        listener.onStart(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        listener.onResume(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        listener.onPause(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_STOP -> {
                        listener.onStop(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        listener.onDestroy(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_ANY -> Unit
                }
            }

            lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

            onDispose {
                listener.onExitCompose(lifecycleOwner)
                lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
            }
        }
    }

    // 刷新状态
    var refreshStateType by remember {
        mutableStateOf<SwipeRefreshStateType>(SwipeRefreshStateType.IDLE)
    }
    val refreshState = rememberSwipeRefreshState(refreshStateType)


    // 首次进入改组件，数据还没加载成功，显示状态页面
    val showViewState = remember {
        mutableStateOf(true)
    }

    // 显示状态页面（加载中、空数据、错误）
    if (showViewState.value) {
        HandlerViewStateComponent(
            collectAsLazyPagingItems,
            showViewState,
            specialRetryBlock,
            viewStateContentAlignment,
            viewStateComponentModifier,
            customEmptyComponent,
            customFailComponent
        )
    } else {
        // 显示正常列表页面
        SwipeRefreshLayout(
            state = refreshState,
            swipeEnabled = enableRefresh,
            onRefresh = {
                specialRefreshBlock?.invoke() ?: collectAsLazyPagingItems.refresh()
            },
            onIdle = {
                refreshStateType = SwipeRefreshStateType.IDLE
            }
        ) {
            // 处理下拉刷新状态（根据分页加载状态更新刷新指示器状态）
            if (refreshState.isRefreshing()) {
                collectAsLazyPagingItems.apply {
                    when (loadState.refresh) {
                        is LoadState.Error -> {
                            refreshStateType = SwipeRefreshStateType.FAIL  // 刷新失败
                        }
                        is LoadState.NotLoading -> {
                            refreshStateType = SwipeRefreshStateType.SUCCESS  // 刷新成功
                        }
                        else -> {}
                    }
                }
            } else {
                // 非刷新状态，如果正在加载则设置为刷新中（用于手动触发刷新时的同步）
                if (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading) {
                    refreshStateType = SwipeRefreshStateType.REFRESHING
                }
            }



            LazyColumn(
                modifier = modifier,
                contentPadding = lazyListContentPadding,
                state = lazyListState
            ) {

                listContent()

                //处理列表分页加载（上拉加载更多）
                if (!refreshState.isRefreshing()) {
                    handleListPaging(
                        collectAsLazyPagingItems,
                        showNoMoreDataFooter
                    )
                }
            }
        }
    }
}

// 处理状态页面（加载中、空数据、错误）
@Composable
private fun <T : Any> HandlerViewStateComponent(
    collectAsLazyPagingItems: LazyPagingItems<T>,
    showViewState: MutableState<Boolean>,  // 控制是否显示状态页面（true=显示状态页，false=显示列表）
    specialRetryBlock: (() -> Unit)? = null,  // 自定义重试逻辑
    viewStateContentAlignment: Alignment = Alignment.Center,
    viewStateComponentModifier: Modifier = Modifier.fillMaxSize(),
    customEmptyComponent: @Composable (() -> Unit)? = null,  // 自定义空布局
    customFailComponent: @Composable (() -> Unit)? = null,  // 自定义失败布局
) {
    // 标记是否已经显示过加载中页面（用于判断首次加载完成后是否显示空页面）
    var hasShowLoadState by remember {
        mutableStateOf(false)
    }

    collectAsLazyPagingItems.apply {
        when (loadState.refresh) {  // refresh：首次加载或下拉刷新时的状态

            // 1. 加载失败
            is LoadState.Error -> {
                val error = (loadState.refresh as LoadState.Error).error
                val errorMessagePair = getErrorMessagePair(error)  // 获取错误文案和图标

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = viewStateComponentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        customFailComponent?.invoke()
                            ?: NoSuccessComponent(
                                message = errorMessagePair.first,
                                iconResId = errorMessagePair.second,
                                contentAlignment = viewStateContentAlignment,
                                specialRetryBlock = specialRetryBlock,
                                loadDataBlock = { collectAsLazyPagingItems.retry() }  // 默认重试
                            )
                    }
                }
            }

            // 2. 加载完成（非加载中、非错误）
            is LoadState.NotLoading -> {
                // 首次加载完成后，没有数据 → 显示空页面
                if (collectAsLazyPagingItems.itemCount == 0 && hasShowLoadState) {
                    Column(modifier = viewStateComponentModifier) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            customEmptyComponent?.invoke()
                                ?: NoSuccessComponent(
                                    message = "暂无数据展示",
                                    iconResId = R.drawable.ic_empty,
                                    contentAlignment = viewStateContentAlignment,
                                    specialRetryBlock = specialRetryBlock,
                                    loadDataBlock = { collectAsLazyPagingItems.refresh() }
                                )
                        }
                    }
                }
                // 有数据 → 切换到正常列表页面
                else if (collectAsLazyPagingItems.itemCount > 0) {
                    showViewState.value = false
                }
            }

            // 3. 加载中（首次）
            is LoadState.Loading -> {
                // 首次加载且没有数据时，显示加载中页面
                if (collectAsLazyPagingItems.itemCount <= 0) {
                    Column(modifier = viewStateComponentModifier) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingComponent(contentAlignment = viewStateContentAlignment)
                        }
                        hasShowLoadState = true  // 标记已显示过加载页
                    }
                }
            }
        }
    }
}

// 处理列表分页加载（上拉加载更多）
private fun <T : Any> LazyListScope.handleListPaging(
    collectAsLazyPagingItems: LazyPagingItems<T>,
    showNoMoreDataFooter: Boolean = true,
) {
    collectAsLazyPagingItems.apply {
        when (loadState.append) {
            // append：加载更多状态（滚动到底部时的加载状态）
            is LoadState.Loading -> {
                item {
                    CommonLoadFooter() // 显示加载中Footer（转圈图标 + "正在加载..."）
                }
            }
            // 加载更多失败
            is LoadState.Error -> {
                item {
                    LoadMoreDataErrorFooter {
                        collectAsLazyPagingItems.retry() // 点击重试
                    }
                }
            }
            // 没有更多数据（endOfPaginationReached = true 表示已加载完所有数据）
            LoadState.NotLoading(endOfPaginationReached = true) -> {
                // 有数据时才显示"没有更多数据"，避免空列表时也显示
                if (collectAsLazyPagingItems.itemCount > 0 && showNoMoreDataFooter) {
                    item {
                        NoMoreDataFooter()
                    }
                }
            }
            else -> {}  // 其他状态（如 NotLoading 但未到末尾）不显示任何Footer
        }
    }
}

/**
 * 底部加载更多失败处理
 * */
@Composable
private fun LoadMoreDataErrorFooter(retry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), contentAlignment = Alignment.Center
    ) {
        Text(text = "--加载失败,点击重试--",
            fontSize = 30.csp,
            color = AppColorsProvider.current.secondText,
            modifier = Modifier.clickable {
                retry.invoke()
            })
    }
}


/**
 * 没有更多数据footer
 */
@Composable
private fun NoMoreDataFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "--没有更多数据啦--",
            fontSize = 30.csp,
            color = AppColorsProvider.current.secondText
        )
    }
}


