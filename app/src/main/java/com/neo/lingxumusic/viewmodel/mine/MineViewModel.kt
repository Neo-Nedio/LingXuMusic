package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.api.UserApi
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.PlaylistData
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.collections.forEach
import kotlin.collections.orEmpty


// 视图模型
@HiltViewModel
class MineViewModel @Inject constructor(private val api: UserApi) : BaseViewStateViewModel() {

    var favoritePlayList: Playlist? by mutableStateOf(null)      // 喜欢的音乐歌单
    var selfCreatePlayList: List<Playlist>? by mutableStateOf(null)  // 自己创建的歌单
    var collectPlayList: List<Playlist>? by mutableStateOf(null)      // 收藏的歌单

    // 请求状态
    val userPlaylistResult = ViewStateMutableLiveData()

    // 获取歌单
    fun getUserPlayList() {
        launch(userPlaylistResult, handleResult = {
            val selfCreateList = mutableListOf<Playlist>()
            val collectList = mutableListOf<Playlist>()
            val userId = AppGlobalData.sLoginData?.userid
            val playlistData = it.dataAs<PlaylistData>()

            playlistData?.info.orEmpty().forEach { playlist ->
                if (playlist.list_create_userid == userId) {  // 是自己创建的
                    if (playlist.name?.contains("喜欢") ?: false) { // 是"喜欢的音乐"歌单
                        favoritePlayList = playlist  // → 单独存储
                    } else {
                        selfCreateList.add(playlist) // → 创建的歌单
                    }
                } else {
                    collectList.add(playlist) // → 收藏的歌单
                }
            }
            selfCreatePlayList = selfCreateList
            collectPlayList = collectList
        }) {
            api.getUserPlayList()
        }
    }
}
