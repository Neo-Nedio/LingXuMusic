package com.neo.lingxumusic.viewmodel.singerDetail.albumDetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.SingerApi
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.model.toSongList
import com.neo.lingxumusic.model.AlbumSongsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val singerApi: SingerApi,
) : BaseViewStateViewModel() {

    var album by mutableStateOf<ArtistAlbum?>(null)
        private set

    var songCount by mutableIntStateOf(0)

    var songListFlow by mutableStateOf<Flow<PagingData<Song>>?>(null)

    // ==================== 选择模式 ====================
    var isSelectionMode by mutableStateOf(false)
    var lastBottomPlayState by mutableStateOf(false)
    val selectedMap = mutableStateMapOf<Int, Boolean>()
    var isAllSelected by mutableStateOf(false)

    // 添加到歌单
    var showAddToPlaylistSheet by mutableStateOf(false)
    var songsToAdd by mutableStateOf<List<Song>>(emptyList())

    fun initAlbum(album: ArtistAlbum) {
        if (this.album?.album_id == album.album_id) return
        this.album = album
        songCount = album.sum_ownercount
        songListFlow = null
    }

    fun buildSongListPager(albumId: Long) {
        if (songListFlow != null) return
        songListFlow = buildPager(
            transformListBlock = { result ->
                result?.dataAs<AlbumSongsData>()?.songs?.toSongList()
            },
            callBlock = { page, pageSize ->
                singerApi.getAudiosSongs(
                    id = albumId.toInt(),
                    page = page.toString(),
                    pagesize = pageSize.toString()
                )
            }
        )
    }

    suspend fun loadAllSongs(): List<Song> {
        val albumId = album?.album_id ?: return emptyList()
        if (songCount <= 0) return emptyList()
        val result = singerApi.getAudiosSongs(
            id = albumId.toInt(),
            page = "1",
            pagesize = songCount.toString()
        )
        return if (result.status == 1 && result.error_code == 0) {
            result.dataAs<AlbumSongsData>()?.songs?.toSongList().orEmpty()
        } else emptyList()
    }

    fun initSelectedMap(count: Int) {
        selectedMap.clear()
        repeat(count) { selectedMap[it] = false }
    }

    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            selectedMap.keys.forEach { selectedMap[it] = false }
        }
    }

    fun clearSelection() {
        isSelectionMode = false
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }

    fun selectAll() {
        repeat(songCount) { selectedMap[it] = true }
        isAllSelected = true
    }

    fun clearSongSelection() {
        selectedMap.keys.forEach { selectedMap[it] = false }
        isAllSelected = false
    }
}
