package com.neo.lingxumusic.core.viewState

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshLayout
import com.neo.lingxumusic.ui.common.refresh.SwipeRefreshStateType
import com.neo.lingxumusic.ui.common.refresh.footer.CommonLoadFooter
import com.neo.lingxumusic.ui.common.refresh.rememberSwipeRefreshState
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.R

/**
 * Description->通用列表组件，支持页面状态切换、下拉刷新、上拉加载更多
 * @param modifier：页面布局修饰
 * @param enableRefresh： 是否允许下拉刷新
 * @param columns: 每行列数
 * @param showNoMoreDataFooter： 没有更多数据时，是否显示没有更多数据footer
 * @param specialRetryBlock：首次加载失败或者数据为空时，点击重试按钮执行的代码块，没设置的话，默认执行collectAsLazyPagingItems.refresh()
 * @param specialRefreshBlock：刷新代码块，没设置的话，默认执行collectAsLazyPagingItems.refresh()
 * @param collectAsLazyPagingItems：分页数据
 * @param lifeCycleListener：生命周期监听
 * @param customEmptyComponent：自定义空布局,没设置则使用默认空布局
 * @param customFailComponent：自定义失败布局,没设置则使用默认失败布局
 * @param gridContent：正常页面内容
 */

@Composable
fun <T : Any> ViewStateGridPagingComponent(
    modifier: Modifier = Modifier,
    enableRefresh: Boolean = true,                    // 是否允许下拉刷新
    columns: Int,                                     // 每行列数
    showNoMoreDataFooter: Boolean = true,             // 是否显示"没有更多数据"footer
    collectAsLazyPagingItems: LazyPagingItems<T>,     // 分页数据
    specialRetryBlock: (() -> Unit)? = null,          // 自定义重试逻辑
    specialRefreshBlock: (() -> Unit)? = null,        // 自定义刷新逻辑
    lifeCycleListener: ComposeLifeCycleListener? = null,  // 生命周期监听
    lazyListContentPadding: PaddingValues = PaddingValues(0.dp),
    lazyGridState: LazyGridState = rememberLazyGridState(),
    viewStateComponentModifier: Modifier = Modifier.fillMaxSize(),
    viewStateContentAlignment: Alignment = Alignment.Center,
    customEmptyComponent: @Composable (() -> Unit)? = null,   // 自定义空布局
    customFailComponent: @Composable (() -> Unit)? = null,    // 自定义失败布局
    gridContent: LazyGridScope.() -> Unit,            // 正常页面内容
) {
    // 生命周期监听处理
    lifeCycleListener?.let { listener ->
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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
                    else -> {}
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
        // 显示正常网格列表页面
        SwipeRefreshLayout(
            state = refreshState,
            swipeEnabled = enableRefresh,
            onRefresh = {
                if (specialRefreshBlock != null) {
                    specialRefreshBlock.invoke()
                } else {
                    collectAsLazyPagingItems.refresh()
                }
            },
            onIdle = {
                refreshStateType = SwipeRefreshStateType.IDLE
            }
        ) {
            // 处理下拉刷新状态
            if (refreshState.isRefreshing()) {
                collectAsLazyPagingItems.apply {
                    when (loadState.refresh) {
                        is LoadState.Error -> {
                            refreshStateType = SwipeRefreshStateType.FAIL
                        }
                        is LoadState.NotLoading -> {
                            refreshStateType = SwipeRefreshStateType.SUCCESS
                        }
                        else -> {}
                    }
                }
            } else {
                if (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading) {
                    refreshStateType = SwipeRefreshStateType.REFRESHING
                }
            }
            // 网格列表
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = modifier,
                contentPadding = lazyListContentPadding,
                state = lazyGridState
            ) {
                // 正常列表内容
                gridContent()

                // 非刷新状态下才显示加载更多footer
                if (!refreshState.isRefreshing()) {
                    handleListPaging(
                        collectAsLazyPagingItems,
                        columns,
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
    showViewState: MutableState<Boolean>,
    specialRetryBlock: (() -> Unit)? = null,
    viewStateContentAlignment: Alignment = Alignment.Center,
    viewStateComponentModifier: Modifier = Modifier.fillMaxSize(),
    customEmptyComponent: @Composable (() -> Unit)? = null,
    customFailComponent: @Composable (() -> Unit)? = null,
) {
    var hasShowLoadState by remember {
        mutableStateOf(false)
    }
    collectAsLazyPagingItems.apply {
        when (loadState.refresh) {
            is LoadState.Error -> {
                // 首次加载异常
                val errorMessagePair = getErrorMessagePair((loadState.refresh as LoadState.Error).error)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = viewStateComponentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        if (customFailComponent != null) {
                            customFailComponent.invoke()
                        } else {
                            NoSuccessComponent(message = errorMessagePair.first,
                                iconResId = errorMessagePair.second,
                                contentAlignment = viewStateContentAlignment,
                                specialRetryBlock = specialRetryBlock,
                                loadDataBlock = { collectAsLazyPagingItems.retry() })
                        }
                    }
                }
            }
            // 加载完成
            is LoadState.NotLoading -> {
                if (collectAsLazyPagingItems.itemCount == 0 && hasShowLoadState) {
                    // 首次加载数据为null
                    Column(modifier = viewStateComponentModifier) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customEmptyComponent != null) {
                                customEmptyComponent.invoke()
                            } else {
                                NoSuccessComponent(message = "暂无数据展示",
                                    iconResId = R.drawable.ic_empty,
                                    contentAlignment = viewStateContentAlignment,
                                    specialRetryBlock = specialRetryBlock,
                                    loadDataBlock = { collectAsLazyPagingItems.refresh() })
                            }
                        }
                    }
                } else if (collectAsLazyPagingItems.itemCount > 0) {
                    // 有数据 → 切换到正常列表页面
                    showViewState.value = false
                }
            }
            // 加载中（首次）
            is LoadState.Loading -> {

                if (collectAsLazyPagingItems.itemCount <= 0) {
                    // 首次加载数据中
                    Column(modifier = viewStateComponentModifier) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingComponent(contentAlignment = viewStateContentAlignment)
                        }
                        hasShowLoadState = true
                    }
                }
            }
        }
    }
}

// 处理网格列表分页加载（上拉加载更多）
private fun <T : Any> LazyGridScope.handleListPaging(
    collectAsLazyPagingItems: LazyPagingItems<T>,
    columns: Int,
    showNoMoreDataFooter: Boolean = true,
) {
    //span = { GridItemSpan(columns) }: 让某个 item 横跨指定的列数，而不是只占一列
    collectAsLazyPagingItems.apply {
        when (loadState.append) {
            // 加载更多中
            is LoadState.Loading -> {
                //加载更多，底部loading
                item(span = { GridItemSpan(columns) }) {
                    CommonLoadFooter()  // 显示加载中footer
                }
            }
            // 加载更多失败
            is LoadState.Error -> {
                item(span = { GridItemSpan(columns) }) {
                    LoadMoreDataErrorFooter {
                        collectAsLazyPagingItems.retry()  // 点击重试
                    }
                }
            }
            // 没有更多数据
            LoadState.NotLoading(endOfPaginationReached = true) -> {
                if (collectAsLazyPagingItems.itemCount > 0 && showNoMoreDataFooter) {
                    // 已经没有更多数据了
                    item(span = { GridItemSpan(columns) }) {
                        NoMoreDataFooter() // 显示"没有更多数据"
                    }
                }
            }
            else -> {}
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


