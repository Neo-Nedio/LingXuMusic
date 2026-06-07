package com.neo.lingxumusic.viewmodel.discovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.RankApi
import com.neo.lingxumusic.model.RankAudioData
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.model.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RankAudioViewModel @Inject constructor(
    private val rankApi: RankApi,
) : BaseViewStateViewModel() {

    //排行信息
    lateinit var rankInfo: RankInfo

    //歌曲数量
    var songCount by mutableIntStateOf(0)

    //歌曲列表
    var songListFlow by mutableStateOf<Flow<PagingData<Song>>?>(null)

    // 选择模式状态
    var isSelectionMode by mutableStateOf(false)

    // 记录进入选择模式前底部播放弹窗的状态
    var lastBottomPlayState by mutableStateOf(false)

    // 歌曲选择状态 Map<index, Boolean>，根据歌曲数量初始化
    val selectedMap = mutableStateMapOf<Int, Boolean>()

    // 是否全部选中
    var isAllSelected by mutableStateOf(false)

    // 是否显示添加到歌单弹窗
    var showAddToPlaylistSheet by mutableStateOf(false)

    // 待添加的歌曲列表
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    // 初始化选中状态
    fun initSelectedMap(count: Int) {
        selectedMap.clear()
        repeat(count) { selectedMap[it] = false }
    }

    // 切换选择模式
    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            selectedMap.keys.forEach { selectedMap[it] = false }
        }
    }

    // 退出选择模式并清空选择
    fun clearSelection() {
        isSelectionMode = false
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    // 全选
    fun selectAll() {
        repeat(songCount) { index ->
            selectedMap[index] = true
        }
        isAllSelected = true
    }

    // 取消全选
    fun clearSongSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    fun buildSongListPager(rankInfo: RankInfo) {
        this.rankInfo = rankInfo
        this.songCount = rankInfo.extra?.resp?.all_total ?: 0
        songListFlow = buildPager(
            transformListBlock = { result ->
                result?.dataAs<RankAudioData>()?.songlist?.map { it.toSong() }
            },
            callBlock = { page, pageSize ->
                rankApi.getRankSong(
                    rankid = rankInfo.rankid,
                    page = page,
                    pagesize = pageSize,
                )
            }
        )
    }

    suspend fun loadAllSongs(): List<Song> {
        if (songCount <= 0) {
            return emptyList()
        }
        val result = rankApi.getRankSong(
            rankid = rankInfo.rankid,
            page = 1,
            pagesize = songCount,
        )
        return if (result.status == 1 && result.error_code == 0) {
            result.dataAs<RankAudioData>()?.songlist?.map { it.toSong() }.orEmpty()
        } else {
            emptyList()
        }
    }
}
