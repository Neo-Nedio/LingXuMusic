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

    fun loadRankList() {
        launch(
            liveData = rankListResult,
            handleResult = { result ->
                starRankList = parseStarRankList(result)
            },
            judgeEmpty = { result ->
                parseStarRankList(result).isEmpty()
            },
        ) {
            rankApi.getRankList()
        }
    }

    private fun parseStarRankList(result: BaseResult): List<RankInfo> {
        return result.dataAs<RankListData>()?.info
            .orEmpty()
            .filter { it.classify == 1 }
    }
}
