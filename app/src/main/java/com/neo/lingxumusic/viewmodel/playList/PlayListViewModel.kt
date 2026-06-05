package com.neo.lingxumusic.viewmodel.playList

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.PlaylistApi
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(private val playlistApi: PlaylistApi)
    : BaseViewStateViewModel() {

    lateinit var playlist: PlaylistBrief

    var songCount by mutableIntStateOf(0)

    var songListFlow by mutableStateOf<Flow<PagingData<Song>>?>(null)

    // 选择模式状态
    var isSelectionMode by mutableStateOf(false)

    // 记录进入选择模式前底部播放弹窗的状态
    var lastBottomPlayState by mutableStateOf(false)

    // 歌曲选择状态 Map<index, Boolean>，根据歌曲数量初始化
    val selectedMap = mutableStateMapOf<Int, Boolean>()

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

    // 是否全部选中
    var isAllSelected by mutableStateOf(false)

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

    // 是否显示添加到歌单弹窗
    var showAddToPlaylistSheet by mutableStateOf(false)

    // 待添加的歌曲列表
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    //构建歌单歌曲分页数据流
    fun buildSongListPager(playlist: PlaylistBrief) {
        this.playlist = playlist
        loadPlaylistCount()
        songListFlow = buildPager(
            transformListBlock = { result ->
                result?.dataAs<PlaylistDetailData>()?.songs
            },
            callBlock = { page, pageSize ->
                playlistApi.getPlaylistSong(
                    id = playlist.list_create_gid,
                    page = page,
                    pagesize = pageSize,
                )
            }
        )
    }

    //获取歌单歌曲总数
    fun loadPlaylistCount() {
        launch(handleResult = { result ->
            songCount = result.data?.asJsonArray?.firstOrNull()?.asJsonObject?.get("count")?.asInt ?: 0
        }) {
            playlistApi.getPlaylistDetail(playlist.list_create_gid)
        }
    }

    //通过songCount一次获取全部歌曲
    suspend fun loadAllSongs(): List<Song> {
        if (songCount <= 0) {
            return emptyList()
        }
        val result = playlistApi.getPlaylistSong(
            id = playlist.global_collection_id,
            page = 1,
            pagesize = songCount,
        )
        return if (result.status == 1 && result.error_code == 0) {
            result.dataAs<PlaylistDetailData>()?.songs.orEmpty()
        } else {
            emptyList()
        }
    }
}
