package com.neo.lingxumusic.viewmodel.singerDetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.google.gson.Gson
import com.neo.lingxumusic.core.viewState.selection.SelectionState
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.SingerApi
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.model.ArtistDetail
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.MvInfo
import com.neo.lingxumusic.model.SingerSongItem
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SingerDetailViewModel @Inject constructor(
    private val singerApi: SingerApi,
) : BaseViewStateViewModel() {

    // ==================== 歌手详情 ====================
    var artistDetail by mutableStateOf<ArtistDetail?>(null)
        private set

    val singerResult = ViewStateMutableLiveData<BaseResult>()

    /**
     * 加载歌手详情
     */
    fun loadSingerDetail(singerId: Long, forceReload: Boolean = false) {
        // 同一 singerId 已加载过数据 → 跳过，避免无谓的重新请求和界面闪烁
        if (!forceReload && artistDetail != null && currentSingerId == singerId) {
            return
        }
        artistDetail = null
        currentSingerId = singerId

        launch(
            liveData = singerResult,
            handleResult = { result ->
                val detail = result.data?.let {
                    Gson().fromJson(it, ArtistDetail::class.java)
                }
                artistDetail = detail
                detail?.let {
                    songCount = it.song_count
                }
            },
            judgeEmpty = { result ->
                result.data == null
            }
        ) {
            singerApi.getArtistDetail(singerId.toInt())
        }
    }

    // ==================== 歌手歌曲 ====================
    var sortType by mutableStateOf(1)
    var showSortDropdown by mutableStateOf(false)

    // 选择模式状态
    val selectionState = SelectionState()

    var songCount by mutableIntStateOf(0)
    var songListFlow by mutableStateOf<Flow<PagingData<SingerSongItem>>?>(null)
    var currentSingerId by mutableStateOf(0L)

    fun buildSongListPager() {
        val singerId = currentSingerId
        if (singerId <= 0) return
        selectionState.selectedMap.clear()
        selectionState.isAllSelected = false

        songListFlow = buildPager(
            transformListBlock = { result ->
                result?.data?.let {
                    Gson().fromJson(it, Array<SingerSongItem>::class.java)?.toList()
                }
            },
            callBlock = { page, pageSize ->
                val sortStr = if (sortType == 1) "hot" else "new"
                singerApi.getArtistAudios(
                    id = singerId.toInt(),
                    page = page.toString(),
                    pagesize = pageSize.toString(),
                    sort = sortStr
                )
            }
        )
    }

    fun changeSortType(type: Int) {
        if (sortType == type) {
            showSortDropdown = false
            return
        }
        sortType = type
        showSortDropdown = false
        // 重新构建分页，让列表按新的排序刷新
        buildSongListPager()
    }

    // 一次获取全部歌曲
    suspend fun loadAllSongs(): List<Song> {
        if (songCount <= 0) return emptyList()
        val result = singerApi.getArtistAudios(
            id = currentSingerId.toInt(),
            page = "1",
            pagesize = songCount.toString(),
            sort = if (sortType == 1) "hot" else "new"
        )
        return if (result.status == 1 && result.error_code == 0) {
            result.data?.let {
                Gson().fromJson(it, Array<SingerSongItem>::class.java)?.map { it.toSong() }
            }.orEmpty()
        } else emptyList()
    }

    // ==================== 歌手 MV ====================
    var mvListFlow by mutableStateOf<Flow<PagingData<MvInfo>>?>(null)
    var currentMvTag by mutableStateOf("all")

    fun buildMvListPager() {
        val singerId = currentSingerId
        if (singerId <= 0 || mvListFlow != null) return
        mvListFlow = buildPager(
            transformListBlock = { result ->
                result?.data?.let {
                    Gson().fromJson(it, Array<MvInfo>::class.java)?.toList()
                }
            },
            callBlock = { page, pageSize ->
                singerApi.getArtistVideos(
                    id = singerId.toInt(),
                    page = page.toString(),
                    pagesize = pageSize.toString(),
                    tag = currentMvTag
                )
            }
        )
    }

    // ==================== 歌手专辑 ====================
    var albumListFlow by mutableStateOf<Flow<PagingData<ArtistAlbum>>?>(null)

    fun buildAlbumListPager() {
        val singerId = currentSingerId
        if (singerId <= 0 || albumListFlow != null) return
        albumListFlow = buildPager(
            transformListBlock = { result ->
                result?.data?.let {
                    Gson().fromJson(it, Array<ArtistAlbum>::class.java)?.toList()
                }
            },
            callBlock = { page, pageSize ->
                singerApi.getArtistAlbums(
                    id = singerId.toInt(),
                    page = page.toString(),
                    pagesize = pageSize.toString()
                )
            }
        )
    }
}
