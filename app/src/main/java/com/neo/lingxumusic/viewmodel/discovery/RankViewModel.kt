package com.neo.lingxumusic.viewmodel.discovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.RankApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.RankListData
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RankViewModel @Inject constructor(
    private val rankApi: RankApi,
) : BaseViewStateViewModel() {

    val rankListResult = ViewStateMutableLiveData<BaseResult>()

    var starRankList by mutableStateOf<List<RankInfo>>(emptyList())
    var regionRankList by mutableStateOf<List<RankInfo>>(emptyList())
    var featureRankList by mutableStateOf<List<RankInfo>>(emptyList())
    var globalRankList by mutableStateOf<List<RankInfo>>(emptyList())
    var genreRankList by mutableStateOf<List<RankInfo>>(emptyList())

    fun loadRankList() {
        launch(
            liveData = rankListResult,
            handleResult = { result ->
                starRankList = parseRankListByClassify(result, 1)
                regionRankList = parseRankListByClassify(result, 2)
                featureRankList = parseRankListByClassify(result, 3)
                globalRankList = parseRankListByClassify(result, 4)
                genreRankList = parseRankListByClassify(result, 5)
            },
            judgeEmpty = { result ->
                parseRankListByClassify(result, 1).isEmpty()
            },
        ) {
            rankApi.getRankList()
        }
    }

    private fun parseRankListByClassify(result: BaseResult, classify: Int): List<RankInfo> {
        return result.dataAs<RankListData>()?.info
            .orEmpty()
            .filter { it.classify == classify }
    }
}
