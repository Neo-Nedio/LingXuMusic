package com.neo.lingxumusic.viewmodel.discovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    fun buildSongListPager(rankInfo: RankInfo) {
        this.rankInfo = rankInfo
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
