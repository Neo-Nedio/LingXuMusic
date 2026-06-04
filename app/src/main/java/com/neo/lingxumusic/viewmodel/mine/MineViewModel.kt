package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.UserFavoriteSongsController
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.PlaylistData
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.BaseResult
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
    val userPlaylistResult = ViewStateMutableLiveData<BaseResult>()

    // 当前选中的 Tab 索引（0=创建歌单，1=收藏歌单，2=歌单助手）
    var selectedTabIndex by mutableStateOf(0)

    // LazyColumn 中各模块的 item 下标（从 0 起算，含 stickyHeader、header、列表项、footer）
    // 用于：① 点击 Tab 时 animateScrollToItem 滚到对应区块；② 滚动时根据 firstVisibleItemIndex 反推当前 Tab

    // 「创建歌单」Tab 滚动目标：滚到 item 0（吸顶 Tab 位置，其下即为创建歌单区域）
    var selfCreatePlayListHeaderIndex = 0
    // 「收藏歌单」Tab 滚动目标：创建区块占 (size + 2) 个 item 后的 header 下标
    var collectPlayListHeaderIndex = 0
    // 「歌单助手」Tab 滚动目标：收藏区块结束后的歌单助手 item 下标
    var songHelperIndex = 0

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
                        //把喜欢歌单的两个id存入全局变量
                        AppGlobalData.favoritePlaylistGlobalCollectionId =
                            playlist.global_collection_id.orEmpty()
                        AppGlobalData.favoritePlaylistListId = playlist.listid
                        // 存储喜欢歌单歌曲总数
                        UserFavoriteSongsController.favoriteSongCount = playlist.getCount()
                        //判断是否为空，为空则加载（防止应用第一次启动时没有喜欢歌单的id，加载失败，这里就重新加载）
                        if(UserFavoriteSongsController.favoriteSongList.isEmpty()){
                            UserFavoriteSongsController.loadFavoriteSongs()
                        }
                    } else {
                        selfCreateList.add(playlist) // → 创建的歌单
                    }
                } else {
                    collectList.add(playlist) // → 收藏的歌单
                }
            }
            selfCreatePlayList = selfCreateList
            collectPlayList = collectList

            // 按 LazyColumn 实际 item 数量计算各 Tab 对应的滚动下标（与 MinePage.PlayList 中 item 顺序一致）
            selfCreatePlayListHeaderIndex = 0
            // 创建区：header(1) + 列表(size-1) + footer(1) = size + 2
            collectPlayListHeaderIndex = selfCreatePlayListHeaderIndex + selfCreateList.size + 2
            // 收藏区再占 size + 2，助手在收藏 footer 之后 +1
            songHelperIndex = collectPlayListHeaderIndex + collectList.size + 1
        }) {
            api.getUserPlayList()
        }
    }
}
