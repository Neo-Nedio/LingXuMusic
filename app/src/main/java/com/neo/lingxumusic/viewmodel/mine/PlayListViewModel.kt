package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(private val userApi: UserApi)
    : BaseViewStateViewModel() {

    lateinit var playlist: PlaylistBrief

    var songListFlow by mutableStateOf<Flow<PagingData<Song>>?>(null)

    //构建歌单歌曲分页数据流
    fun buildSongListPager(playlist: PlaylistBrief) {
        this.playlist = playlist
        songListFlow = buildPager(
            transformListBlock = { result ->
                result?.dataAs<PlaylistDetailData>()?.songs
            },
            callBlock = { page, pageSize ->
                userApi.getPlaylistSong(
                    id = playlist.global_collection_id,
                    page = page,
                    pagesize = pageSize,
                )
            }
        )
    }
}
