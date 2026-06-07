package com.neo.lingxumusic.viewmodel.playList

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.PlaylistApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.buildData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val playlistApi: PlaylistApi,
    private val songApi: SongApi,
) : BaseViewStateViewModel() {

    // 路由传入的待添加歌曲列表
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    // 我喜欢的歌单
    var favoritePlaylist by mutableStateOf<Playlist?>(null)

    // 我创建的歌单列表
    var selfCreatePlaylists by mutableStateOf<List<Playlist>>(emptyList())

    // 歌单选择状态 Map<playlistId, Boolean>
    val selectedMap = mutableStateMapOf<String, Boolean>()

    // 是否全部选中
    var isAllSelected by mutableStateOf(false)

    // 是否显示创建歌单弹窗
    var showCreatePlaylistDialog by mutableStateOf(false)

    // 新歌单名称
    var newPlaylistName by mutableStateOf("")

    // 是否为私密歌单
    var isPrivatePlaylist by mutableStateOf(false)

    // 创建歌单请求状态
    val createPlaylistResult = ViewStateMutableLiveData<BaseResult>()

    // 添加歌曲到歌单请求状态
    val addSongsResult = ViewStateMutableLiveData<BaseResult>()

    // 初始化数据
    fun initData(
        songs: List<Song>,
        favorite: Playlist?,
        selfCreate: List<Playlist>,
    ) {
        songsToAdd = songs
        favoritePlaylist = favorite
        selfCreatePlaylists = selfCreate
        selectedMap.clear()
        isAllSelected = false
    }

    // 切换歌单选中状态
    fun toggleSelection(playlistId: String) {
        selectedMap[playlistId] = !(selectedMap[playlistId] ?: false)
    }

    // 全选
    fun selectAll() {
        favoritePlaylist?.global_collection_id?.let {
            selectedMap[it] = true
        }
        selfCreatePlaylists.forEach {
            it.global_collection_id?.let { id ->
                selectedMap[id] = true
            }
        }
        isAllSelected = true
    }

    // 取消全选
    fun clearSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    // 创建歌单
    fun createPlaylist() {
        val name = newPlaylistName.trim()
        if (name.isEmpty()) return

        launch(createPlaylistResult) {
            playlistApi.addPlaylist(
                name = name,
                listCreateUserid = AppGlobalData.userId,
                listCreateListid = 0,
                type = 0,
                isPri = if (isPrivatePlaylist) 1 else null,
            )
        }
    }

    // 添加歌曲到选中的歌单
    fun addSongsToSelectedPlaylists() {
        val selectedPlaylists = mutableListOf<Playlist>()
        favoritePlaylist?.takeIf { selectedMap[it.global_collection_id] == true }?.let { selectedPlaylists.add(it) }
        selfCreatePlaylists.filter { selectedMap[it.global_collection_id] == true }.let { selectedPlaylists.addAll(it) }

        if (selectedPlaylists.isEmpty()) return

        val data = songsToAdd.mapNotNull { it.buildData() }.joinToString(",")
        if (data.isBlank()) return

        launch(addSongsResult) {
            // 依次添加到每个选中的歌单
            for (playlist in selectedPlaylists) {
                val listid = playlist.listid.takeIf { it > 0 }
                    ?: playlist.list_create_listid.takeIf { it > 0 }
                    ?: continue
                songApi.addSongToPlaylist(listid, data)
            }
            BaseResult(status = 1)
        }
    }
}
