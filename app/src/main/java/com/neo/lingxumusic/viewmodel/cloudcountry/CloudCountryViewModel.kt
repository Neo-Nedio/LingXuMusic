package com.neo.lingxumusic.viewmodel.cloudcountry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.paging.AppPagingConfig
import com.neo.lingxumusic.core.viewState.paging.buildPager
import com.neo.lingxumusic.http.api.VideoApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.BrushData
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CloudCountryViewModel @Inject constructor(
    private val videoApi: VideoApi
) : BaseViewStateViewModel() {

    var brushVideoFlow by mutableStateOf<Flow<PagingData<BrushVideo>>?>(null)
    var curPlayVideo by mutableStateOf<BrushVideo?>(null)

    fun buildBrushVideoPager() {
        if (brushVideoFlow != null) {
            return
        }
        brushVideoFlow = buildPager(
            config = AppPagingConfig(pageSize = 2, initialLoadSize = 2),
            transformListBlock = { result ->
                result?.dataAs<BrushData>()?.list
            },
            hasMoreBlock = { result ->
                val brushData = result.dataAs<BrushData>() ?: return@buildPager false
                // list 为空时不继续翻页，避免 has_next=1 但无数据导致无限请求
                brushData.has_next == 1 && !brushData.list.isNullOrEmpty()
            },
        ) { _, _ ->
            videoApi.getBrushVideo()
        }
    }
}
