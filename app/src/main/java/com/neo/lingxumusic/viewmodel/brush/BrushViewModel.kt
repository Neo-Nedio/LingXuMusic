package com.neo.lingxumusic.viewmodel.brush

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
class BrushViewModel @Inject constructor(
    private val videoApi: VideoApi
) : BaseViewStateViewModel() {

    val brushVideoResult = ViewStateMutableLiveData<BaseResult>()

    val videoList = mutableStateListOf<BrushVideo>()

    var isLoadingMore by mutableStateOf(false)
        private set

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

    fun loadMoreBrushVideo(onSameData: () -> Unit, onAdded: (startIndex: Int) -> Unit) {
        if (isLoadingMore) {
            return
        }
        viewModelScope.launch {
            isLoadingMore = true
            try {
                val result = videoApi.getBrushVideo()
                if (result.status != 1 || result.error_code != 0) {
                    onSameData()
                    return@launch
                }
                val newList = parseBrushVideoList(result)
                if (newList.isEmpty()) {
                    onSameData()
                    return@launch
                }
                val existingUrls = videoList.mapNotNull { it.displayPlayUrl }.toSet()
                val toAdd = newList.filter { video ->
                    val url = video.displayPlayUrl
                    !url.isNullOrBlank() && url !in existingUrls
                }
                if (toAdd.isEmpty()) {
                    onSameData()
                } else {
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

    private fun parseBrushVideoList(result: BaseResult): List<BrushVideo> {
        return result.dataAs<BrushData>()?.list.orEmpty()
    }
}
