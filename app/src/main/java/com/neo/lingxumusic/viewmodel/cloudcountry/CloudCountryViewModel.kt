package com.neo.lingxumusic.viewmodel.cloudcountry

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.VideoApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.BrushData
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.model.displayPlayUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudCountryViewModel @Inject constructor(
    private val videoApi: VideoApi
) : BaseViewStateViewModel() {

    // 请求结果状态（用于 ViewState 组件）
    val brushVideoResult = ViewStateMutableLiveData<BaseResult>()

    // 视频列表数据（可观察的可变列表）
    val videoList = mutableStateListOf<BrushVideo>()

    // 是否正在加载更多（防止重复加载）
    var isLoadingMore by mutableStateOf(false)
        private set

    // 首次加载视频列表
    fun loadBrushVideo() {
        launch(
            liveData = brushVideoResult,
            handleResult = { result ->
                videoList.clear()
                videoList.addAll(parseBrushVideoList(result))
            },
            judgeEmpty = { result ->
                parseBrushVideoList(result).isEmpty()
            },
        ) {
            videoApi.getBrushVideo()
        }
    }

    /**
     * 加载更多视频（主动上拉加载更多）
     * @param onSameData 没有新数据时的回调
     * @param onAdded 添加新数据后的回调，参数为新数据的起始索引
     */
    fun loadMoreBrushVideo(onSameData: () -> Unit, onAdded: (startIndex: Int) -> Unit) {
        // 正在加载中，直接返回
        if (isLoadingMore) {
            return
        }

        viewModelScope.launch {
            isLoadingMore = true
            try {
                // 请求新数据
                val result = videoApi.getBrushVideo()
                // 请求失败，触发同数据回调
                if (result.status != 1 || result.error_code != 0) {
                    onSameData()
                    return@launch
                }
                // 解析新数据列表
                val newList = parseBrushVideoList(result)
                // 没有新数据，触发同数据回调
                if (newList.isEmpty()) {
                    onSameData()
                    return@launch
                }
                // 获取已有视频的播放地址集合（用于去重）
                val existingUrls = videoList.mapNotNull { it.displayPlayUrl }.toSet()
                // 过滤出未重复的新视频（按播放地址去重）
                val toAdd = newList.filter { video ->
                    val url = video.displayPlayUrl
                    !url.isNullOrBlank() && url !in existingUrls
                }
                // 去重后没有新数据
                if (toAdd.isEmpty()) {
                    onSameData()
                } else {
                    // 有去重后的新数据，添加到列表末尾
                    val startIndex = videoList.size
                    videoList.addAll(toAdd)
                    onAdded(startIndex)
                }
            } catch (_: Exception) {
                onSameData()
            } finally {
                isLoadingMore = false
            }
        }
    }

    // 解析返回结果，提取视频列表
    private fun parseBrushVideoList(result: BaseResult): List<BrushVideo> {
        return result.dataAs<BrushData>()?.list.orEmpty()
    }
}
