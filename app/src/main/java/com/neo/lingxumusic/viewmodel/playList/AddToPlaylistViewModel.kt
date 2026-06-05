package com.neo.lingxumusic.viewmodel.playList

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.viewmodel.mine.MineViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val mineViewModel: MineViewModel
) : BaseViewStateViewModel() {

    // 路由传入的待添加歌曲列表
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    // 我喜欢的歌单(从mineViewModel获取)
    val favoritePlaylist: Playlist? get() = mineViewModel.favoritePlayList

    // 我创建的歌单列表(从mineViewModel获取)
    val selfCreatePlaylists: List<Playlist> get() = mineViewModel.selfCreatePlayList.orEmpty()

    // 歌单选择状态 Map<playlistId, Boolean>
    val selectedMap = mutableStateMapOf<String, Boolean>()

    // 初始化数据
    fun initData(songs: List<Song>) {
        songsToAdd = songs
        selectedMap.clear()
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
    }

    // 取消全选
    fun clearSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
    }
}