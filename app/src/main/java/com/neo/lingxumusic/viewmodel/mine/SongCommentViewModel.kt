package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.AppPagingConfig
import com.neo.lingxumusic.core.viewState.paging.PagingException
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.core.viewState.paging.pager
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SongCommentViewModel @Inject constructor(
    private val songApi: SongApi
) : BaseViewStateViewModel() {

    // 缓存不同评论类型（推荐/最热/最新）的分页数据流
    // key = 评论类型（1=推荐，2=最热，3=最新），value = 对应的 PagingData 流
    var commentBeanListFlows = mutableStateMapOf<Int, Flow<PagingData<SongCommentItem>>>()

    // 评论排序选项列表（首屏加载后根据热词动态填充）
    var commentSortTabs by mutableStateOf(listOf(CommentSortTab("全部", 1)))

    // 当前选中的评论类型（默认 1 = 全部）
    var curSelectedTabType by mutableIntStateOf(1)

    // 是否显示楼中楼弹窗
    var showFloorCommentSheet by mutableStateOf(false)
    // 当前歌曲
    var song: Song? = null
    // 楼中楼主评论 ID
    var floorOwnerCommentId by mutableStateOf(0L)
    // 楼中楼主评论完整数据（酷狗接口不返回 owner，需外部传入）
    var floorOwnerComment by mutableStateOf<SongCommentItem?>(null)
    // 楼中楼接口 special_id
    var floorOwnerSpecialChildId by mutableStateOf<String?>(null)
    // 楼中楼回复分页数据流
    var floorCommentListFlow by mutableStateOf<Flow<PagingData<SongCommentItem>>?>(null)

    /**
     * 构建评论列表的分页数据流
     * @param song 当前歌曲
     * @param type 评论类型（1=全部）
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

            // 调用 API 获取评论数据（type=1 全部，type>=2 热词搜索）
            val result = if (type == 1) {
                songApi.getSongComment(
                    mixsongid = song.mixsongid.toString(),
                    page = currentPage.toString(),
                    pagesize = pageSize.toString()
                )
            } else {
                val hotWord = commentSortTabs.firstOrNull { it.type == type }?.title.orEmpty()
                songApi.getSongCommentByHotWord(
                    mixsongid = song.mixsongid.toString(),
                    page = currentPage.toString(),
                    pagesize = pageSize.toString(),
                    hot_word = hotWord
                )
            }

            // 请求成功
            if (result.status == 1) {
                // 提取评论列表
                val responseList = result.list.orEmpty()

                // 首屏「全部」Tab 加载时，按频率提取热词生成 Tab
                if (currentPage == 1 && type == 1) {
                    val hotWordTabs = result.hot_word_list.orEmpty()
                        .sortedByDescending { it.count }
                        .mapIndexed { index, item ->
                            CommentSortTab(item.content, index + 2)
                        }
                    commentSortTabs = listOf(CommentSortTab("全部", 1)) + hotWordTabs
                }

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
        // 按评论类型缓存分页数据流，不清空其他 Tab 的缓存
        commentBeanListFlows[type] = commentBeanListFlow
    }

    /**
     * 构建楼中楼回复分页数据流
     */
    fun buildFloorCommentPager(
        commentId: Long,
        mixsongid: Long,
        specialChildId: String? = floorOwnerSpecialChildId
    ) {
        if (commentId <= 0L || mixsongid <= 0L) return

        val ownerComment = floorOwnerComment ?: SongCommentItem(id = commentId)
        floorCommentListFlow = buildPager(
            //转换数据
            transformListBlock = { result ->
                val replies = result?.replies.orEmpty()
                result?.ownerComment?.let { listOf(it) + replies } ?: replies
            },
            //加载数据的方法
            callBlock = { page, pageSize ->
                val apiResult = songApi.getCommentFloor(
                    specialId = specialChildId.orEmpty(),
                    mixsongid = mixsongid.toString(),
                    tid = commentId.toString(),
                    page = page.toString(),
                    pagesize = pageSize.toString()
                )
                FloorCommentSuccessResult(
                    ownerComment = if (page == 1) ownerComment else null,
                    replies = apiResult.list.orEmpty(),
                    status = apiResult.status,
                    error_code = apiResult.err_code
                )
            }
        )
    }
}

data class CommentSortTab(var title: String, var type: Int)

/**
 * 楼中楼分页数据包装，第一页携带原评论
 */
class FloorCommentSuccessResult(
    val ownerComment: SongCommentItem?,
    val replies: List<SongCommentItem>,
    status: Int = 1,
    error_code: Int = 0,
) : BaseResult(status = status, error_code = error_code)
