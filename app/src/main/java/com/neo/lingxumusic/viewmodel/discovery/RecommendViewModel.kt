package com.neo.lingxumusic.viewmodel.discovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.RecommendApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.PersonalFmData
import com.neo.lingxumusic.model.RecommendSong
import com.neo.lingxumusic.model.ScenePlaylist
import com.neo.lingxumusic.model.SceneRecommendData
import com.neo.lingxumusic.model.SongRecommendData
import com.neo.lingxumusic.model.coverUrl
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.collections.orEmpty

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val api: RecommendApi
) : BaseViewStateViewModel() {

    val everyDayResult = ViewStateMutableLiveData<BaseResult>()
    val guessLikeResult = ViewStateMutableLiveData<BaseResult>()
    val recommendSongsResult = ViewStateMutableLiveData<BaseResult>()
    val recommendPlayListResult = ViewStateMutableLiveData<BaseResult>()

    var everyDaySongList by mutableStateOf<List<RecommendSong>>(emptyList())
    var guessLikeSongList by mutableStateOf<List<RecommendSong>>(emptyList())
    var recommendSongList by mutableStateOf<List<RecommendSong>>(emptyList())
    var recommendPlayList by mutableStateOf<List<ScenePlaylist>>(emptyList())
    var everyDayCover by mutableStateOf<String?>(null)
    var guessLikeCover by mutableStateOf<String?>(null)

    val recommendCardTabs = listOf(
        RecommendCardTab("精选好歌随心听", "1"),
        RecommendCardTab("经典怀旧金曲", "2"),
        RecommendCardTab("热门好歌精选", "3"),
        RecommendCardTab("小众宝藏佳作", "4"),
        RecommendCardTab("VIP专属推荐", "6"),
    )

    fun loadEveryDayRecommend() {
        launch(everyDayResult, handleResult = { result ->
            everyDaySongList = result.dataAs<SongRecommendData>()?.song_list.orEmpty()
            everyDayCover = everyDaySongList.firstOrNull()?.coverUrl()
        }) {
            api.getEveryDayRecommend()
        }
    }

    fun loadGuessYourLike() {
        launch(guessLikeResult, handleResult = { result ->
            guessLikeSongList = result.dataAs<PersonalFmData>()?.song_list.orEmpty()
            guessLikeCover = guessLikeSongList.firstOrNull()?.coverUrl()
        }) {
            api.guessYourLike()
        }
    }

    //获取推荐歌曲
    fun loadRecommendSongs(cardId: String) {
        launch(recommendSongsResult, handleResult = { result ->
            recommendSongList = result.dataAs<SongRecommendData>()?.song_list.orEmpty()
        }) {
            api.getSongRecommend(cardId)
        }
    }

    fun loadRecommendPlaylists() {
        launch(recommendPlayListResult, handleResult = { result ->
            recommendPlayList = result.dataAs<SceneRecommendData>()?.special_list.orEmpty()
        }) {
            api.getPlaylistRecommend()
        }
    }
}

data class RecommendCardTab(val title: String, val cardId: String)

