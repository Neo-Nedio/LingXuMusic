package com.neo.lingxumusic.viewmodel.singerDetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.google.gson.Gson
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.SingerApi
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
    fun loadSingerDetail(singerId: Long) {
        artistDetail = null

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
    var isSelectionMode by mutableStateOf(false)
    val selectedMap = mutableStateMapOf<Int, Boolean>()
    var isAllSelected by mutableStateOf(false)

    var songCount by mutableIntStateOf(0)
    var songListFlow by mutableStateOf<Flow<PagingData<SingerSongItem>>?>(null)
    private var currentSingerId by mutableStateOf(0L)

    // 添加到歌单
    var showAddToPlaylistSheet by mutableStateOf(false)
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    fun buildSongListPager(singerId: Long) {
        currentSingerId = singerId
        selectedMap.clear()
        isAllSelected = false

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
        if (currentSingerId > 0) {
            buildSongListPager(currentSingerId)
        }
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

    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            clearSelection()
        }
    }

    fun clearSelection() {
        isSelectionMode = false
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    fun toggleSongSelection(index: Int) {
        selectedMap[index] = !(selectedMap[index] ?: false)
    }

    fun selectAll() {
        repeat(songCount) { index ->
            selectedMap[index] = true
        }
        isAllSelected = true
    }

    fun clearSongSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    // ==================== 歌手 MV ====================
    var mvListFlow by mutableStateOf<Flow<PagingData<MvInfo>>?>(null)
    private var currentMvSingerId by mutableStateOf(0L)

    fun buildMvListPager(singerId: Long) {
        if (currentMvSingerId == singerId && mvListFlow != null) return
        currentMvSingerId = singerId
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
                    tag = "all"
                )
            }
        )
    }
}
