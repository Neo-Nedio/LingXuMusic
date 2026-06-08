package com.neo.lingxumusic.viewmodel.singerDetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.SingerApi
import com.neo.lingxumusic.model.ArtistDetail
import com.neo.lingxumusic.model.BaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SingerDetailViewModel @Inject constructor(
    private val singerApi: SingerApi,
) : BaseViewStateViewModel() {

    // 歌手详情数据
    var artistDetail by mutableStateOf<ArtistDetail?>(null)
        private set

    // 页面状态 LiveData
    val singerResult = ViewStateMutableLiveData<SingerResult>()

    /**
     * 加载歌手详情
     */
    fun loadSingerDetail(singerId: Long) {
        artistDetail = null

        launch(
            liveData = singerResult,
            handleResult = { result ->
                val detail = result.data?.let {
                    Gson().fromJson(it, ArtistDetail::class.java)
                }
                artistDetail = detail
            },
            judgeEmpty = { result ->
                result.data == null
            }
        ) {
            SingerResult(
                status = 1,
                error_code = 0,
                data = singerApi.getArtistDetail(singerId.toInt()).data
            )
        }
    }
}

/**
 * 歌手详情结果（内部类，用于传递数据）
 */
class SingerResult(
    status: Int = 0,
    error_code: Int = 0,
    data: com.google.gson.JsonElement? = null
) : BaseResult(status, error_code, data)
