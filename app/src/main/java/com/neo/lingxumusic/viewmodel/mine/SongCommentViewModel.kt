package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.AppPagingConfig
import com.neo.lingxumusic.core.viewState.paging.PagingException
import com.neo.lingxumusic.core.viewState.paging.pager
import com.neo.lingxumusic.hilt.entrypoint.EntryPointFinder
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentItem
import kotlinx.coroutines.flow.Flow


class SongCommentViewModel : BaseViewStateViewModel() {

    // 缓存不同评论类型（推荐/最热/最新）的分页数据流
    // key = 评论类型（1=推荐，2=最热，3=最新），value = 对应的 PagingData 流
    var commentBeanListFlows = mutableStateMapOf<Int, Flow<PagingData<SongCommentItem>>>()


    // 评论排序选项列表
    val commentSortTabs = listOf(
        CommentSortTab("推荐", 1),
        CommentSortTab("最热", 2),
        CommentSortTab("最新", 3)
    )

    // 当前选中的评论类型（默认 1 = 推荐）
    var curSelectedTabType by mutableStateOf(1)

    /**
     * 构建评论列表的分页数据流
     * @param song 当前歌曲
     * @param type 评论类型（1=推荐，2=最热，3=最新）
     */
    fun buildNewCommentListPager(song: Song, type: Int) {
        // 分页配置
        val config = AppPagingConfig()
        // 创建 Paging 数据流
        val commentBeanListFlow = pager(config, initialKey = 1) { params ->

            // 当前页码，默认为 1
            val currentPage = params.key ?: 1

            // 第一页用 initialLoadSize，后续页用 pageSize
            val pageSize = if (currentPage == 1) config.initialLoadSize else config.pageSize

            // 调用 API 获取评论数据
            val result = EntryPointFinder.getSongApi().getSongComment(
                mixsongid = song.mixsongid.toString(),
                page = currentPage.toString(),
                pagesize = pageSize.toString()
            )

            // 请求成功
            if (result.status == 1) {
                // 提取评论列表
                val responseList = result.list.orEmpty()

                val everyPageSize = config.pageSize
                val initPageSize = config.initialLoadSize

                // 上一页的 key（第一页没有上一页）
                val preKey = if (currentPage == 1) null else currentPage - 1

                // 下一页的 key
                var nextKey: Int? = if (currentPage == 1) {
                    (initPageSize / everyPageSize) + 1  // 第一页时根据加载量计算
                } else {
                    currentPage + 1                       // 后续页直接 +1
                }

                // 如果返回数据不足一页 或 禁用了加载更多，则没有下一页
                if (responseList.size < everyPageSize || !config.enableLoadMore) {
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
                PagingSource.LoadResult.Error(
                    PagingException(result.status.toString(), "请求错误")
                )
            }
        }
        // 清空旧缓存，存入新数据流
        commentBeanListFlows.clear()
        commentBeanListFlows[type] = commentBeanListFlow
    }
}

data class CommentSortTab(var title: String, var type: Int)
