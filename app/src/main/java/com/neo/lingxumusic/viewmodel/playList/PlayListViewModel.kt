package com.neo.lingxumusic.viewmodel.playList

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.selection.SelectionState
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.PlaylistApi
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val playlistApi: PlaylistApi,
    private val songApi: SongApi,
) : BaseViewStateViewModel() {

    lateinit var playlist: PlaylistBrief

    var songCount by mutableIntStateOf(0)

    var songListFlow by mutableStateOf<Flow<PagingData<Song>>?>(null)

    // 选择模式状态
    val selectionState = SelectionState()

    // 删除歌曲请求状态
    val deleteSongsResult = ViewStateMutableLiveData<BaseResult>()

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

    // 从歌单中删除歌曲
    fun deleteSongsFromPlaylist(songs: List<Song>) {
        val listid = playlist.listid.takeIf { it > 0 }
            ?: playlist.list_create_listid.takeIf { it > 0 }
            ?: return

        val fileids = songs.mapNotNull { song ->
            song.fileid.takeIf { it > 0 }
        }.joinToString(",")
        if (fileids.isBlank()) return

        launch(deleteSongsResult) {
            songApi.delSongToPlaylist(listid, fileids)
            BaseResult(status = 1)
        }
    }
}
