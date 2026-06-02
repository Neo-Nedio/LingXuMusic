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

    var everyDaySongList by mutableStateOf<List<RecommendSong>>(emptyList())
    var guessLikeSongList by mutableStateOf<List<RecommendSong>>(emptyList())
    var everyDayCover by mutableStateOf<String?>(null)
    var guessLikeCover by mutableStateOf<String?>(null)

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
}

