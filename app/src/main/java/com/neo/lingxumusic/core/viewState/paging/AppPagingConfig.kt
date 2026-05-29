package com.neo.lingxumusic.core.viewState.paging


/**
 * 分页配置数据类
 * 用于配置 Paging 3 库的分页行为
 */
// 默认常量
const val DEFAULT_EVERY_PAGE_SIZE = 30        // 默认每页大小
const val DEFAULT_INITIAL_LOAD_SIZE = 30      // 默认首次加载大小
const val DEFAULT_PREFETCH_DISTANCE = 4       // 默认预加载距离
data class AppPagingConfig(
    // 每页加载的数据量，默认 20 条
    val pageSize: Int = DEFAULT_EVERY_PAGE_SIZE,

    // 首次加载的数据量，默认 20 条，建议与 pageSize 保持一致
    val initialLoadSize: Int = DEFAULT_INITIAL_LOAD_SIZE,

    // 预加载距离，默认 4。当滚动到距离列表末尾还有 4 条时，自动触发加载下一页
    val prefetchDistance: Int = DEFAULT_PREFETCH_DISTANCE,

    // 最大缓存数据量，默认无限制。达到限制时会淘汰最旧的数据
    val maxSize: Int = Int.MAX_VALUE,

    // 是否显示占位符，默认 false。true = 加载中显示占位，false = 加载完成才显示
    val enablePlaceholders: Boolean = false,

    // 是否启用加载更多，默认 true。false = 只加载一次，不分页
    val enableLoadMore: Boolean = true,

    // 最小请求周期（毫秒），默认 500ms。避免短时间内频繁请求
    val minRequestCycle: Long = 500L
)
