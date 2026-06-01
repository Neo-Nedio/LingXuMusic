package com.neo.lingxumusic.core.viewState.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.neo.lingxumusic.model.BaseResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.*


// 简化版分页构建器（专用于 BaseResult 响应）对下面的pager进行封装
fun <R : BaseResult, I : Any> ViewModel.buildPager(
    config: AppPagingConfig = AppPagingConfig(),           // 分页配置
    transformListBlock: (r: R?) -> List<I>?,               // 从响应中提取列表
    hasMoreBlock: ((R) -> Boolean)? = null,                // 自定义是否还有下一页（如 has_next 字段）
    callBlock: suspend (page: Int, config: Int) -> R      // 实际网络请求
): Flow<PagingData<I>> {

    return pager(config, 1) {
        val currentPage = it.key ?: 1 // 当前页码
        val result = callBlock.invoke(currentPage,
            if (currentPage == 1)   // 第一页用 initialLoadSize，后续页用 pageSize
                config.initialLoadSize
            else
                config.pageSize)

        if (result.status == 1) { // 请求成功
            //转换数据
            val responseList = transformListBlock.invoke(result) ?: emptyList()

            val everyPageSize = config.pageSize
            val initPageSize = config.initialLoadSize

            // 上一页的 key（第一页没有上一页）
            val preKey = if (currentPage == 1) null else currentPage.minus(1)

            // 下一页的 key
            var nextKey: Int? = if (currentPage == 1) {
                (initPageSize / everyPageSize).plus(1) // 第一页时根据加载量计算
            } else {
                currentPage.plus(1)  // 后续页直接 +1
            }

            // 如果返回数据不足一页 或 禁用了加载更多，则没有下一页
            val hasMore = hasMoreBlock?.invoke(result)
                ?: (responseList.size >= everyPageSize && config.enableLoadMore)
            if (!hasMore) {
                nextKey = null
            }

            // 返回成功结果
            PagingSource.LoadResult.Page(
                data = responseList,
                prevKey = preKey,
                nextKey = nextKey
            )
        } else {
            // 请求失败，返回错误
            PagingSource.LoadResult.Error(PagingException(result.status.toString(),  "请求错误"))
        }
    }
}


fun <K : Any, V : Any> ViewModel.pager(
    config: AppPagingConfig = AppPagingConfig(),           // 分页配置
    initialKey: K? = null,                                 // 初始 key（如第一页的页码）
    errorBlock: (() -> Unit)? = null,                      // 错误回调
    loadData: suspend (PagingSource.LoadParams<K>) -> PagingSource.LoadResult<K, V>  // 实际加载函数
): Flow<PagingData<V>> {
    // 转换为 Paging 3 标准配置
    val baseConfig = PagingConfig(
        config.pageSize,                           // 每页大小
        initialLoadSize = config.initialLoadSize,  // 首次加载大小
        prefetchDistance = config.prefetchDistance, // 预加载距离
        maxSize = config.maxSize,                  // 最大缓存
        enablePlaceholders = config.enablePlaceholders // 是否显示占位符
    )

    return Pager(
        config = baseConfig,
        initialKey = initialKey
    ) {
        object : PagingSource<K, V>() {
            // PagingSource 的核心加载方法
            // 当 Paging 库需要加载数据时（首次加载、滚动预加载、刷新时），会自动调用此方法
            //
            // @param params 加载参数，包含：
            //   - key: 当前页码或加载锚点（如第 1 页、第 2 页）
            //   - loadSize: 本次需要加载的数量
            //   - placeholdersEnabled: 是否启用占位符
            //   - 其他参数...
            //
            // @return LoadResult 三种类型：
            //   - LoadResult.Page: 加载成功，包含数据、上一页 key、下一页 key
            //   - LoadResult.Error: 加载失败，包含异常信息
            //   - LoadResult.Invalid: 数据无效，需要刷新（如 token 过期）
            // 核心加载方法
            override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
                val startRequestTime = Date().time  // 记录开始时间
                return try {
                    val result = loadData.invoke(params) // 执行加载
                    val requestTimeCost = Date().time - startRequestTime // 计算耗时
                    // 计算需要延迟的时间
                    val delayTime = 0L.coerceAtLeast(config.minRequestCycle - requestTimeCost)
                    delay(delayTime) // 确保两次请求间隔 >= minRequestCycle
                    result // 返回成功结果
                } catch (e: Exception) {
                    e.printStackTrace()
                    val requestTimeCost = Date().time - startRequestTime
                    val delayTime = 0L.coerceAtLeast(config.minRequestCycle - requestTimeCost)
                    delay(delayTime)
                    errorBlock?.invoke()  // 错误回调
                    LoadResult.Error(e)   // 返回错误结果
                }
            }

            // 刷新时获取 key 直接获取初始的key
            override fun getRefreshKey(state: PagingState<K, V>): K? {
                return initialKey
            }

        }
    }.flow.cachedIn(viewModelScope)  // 缓存到 ViewModel 生命周期
}

class PagingException(val errorCode: String, val errorMessage: String) : Exception("PagingException")
