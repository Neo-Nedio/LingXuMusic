package com.neo.lingxumusic.viewmodel.playMusic

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.core.player.IPlayerListener
import com.neo.lingxumusic.core.player.Player
import com.neo.lingxumusic.core.player.PlayerStatus
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewState
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentResult
import com.neo.lingxumusic.model.LyricResult
import com.neo.lingxumusic.utils.LyricUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayMusicViewModel @Inject constructor(private val api: SongApi) : BaseViewStateViewModel(),
    IPlayerListener {
    // disk旋转动画
    val sheetDiskRotate by mutableStateOf(Animatable(0f))
    // 上一次disk旋转角度
    var lastSheetDiskRotateAngleForSnap = 0f
    // 是否抬起磁针
    var sheetNeedleUp by mutableStateOf(true)

    var songCommentResult by mutableStateOf<SongCommentResult?>(null)

    //是否展示歌词
    var showLyric by mutableStateOf(false)

    //歌词原始结果
    val lyricResult = ViewStateMutableLiveData<LyricResult>()
    //歌词解析后的结果
    val lyricModelList = mutableListOf<LyricModel>()
    //当前播放索引
    var curLyricIndex by mutableIntStateOf(-1)
    //当前播放时间
    var curPlayPosition = 0

    init {
        Player.addListener(this)
    }

    override fun onCleared() {
        Player.removeListener(this)
        super.onCleared()
    }


    fun getSongComment(song: Song) {
        viewModelScope.launch {
            runCatching {
                api.getSongComment(
                    mixsongid = song.mixsongid.toString()
                )
            }.onSuccess { result ->
                if (result.status == 1 && result.err_code == 0) {
                    songCommentResult = result
                }
            }
        }
    }

    fun getLyric(song: Song) {
        val hash = song.hash ?: return
        lyricResult.value = ViewState.Loading
        viewModelScope.launch {
            runCatching {
                val searchResult = api.getLyricSearch(hash)
                if (searchResult.status != 200 || searchResult.errcode != 200) {
                    return@runCatching null
                }
                val candidate = searchResult.candidates
                    ?.firstOrNull { !it.id.isNullOrBlank() && !it.accesskey.isNullOrBlank() }
                    ?: return@runCatching null
                //重置当前播放的歌词索引
                curLyricIndex = -1
                api.getLyric(
                    id = candidate.id!!,
                    accesskey = candidate.accesskey!!,
                )
            }.onSuccess { result ->
                if (!result?.decodeContent.isNullOrBlank()) {
                    //设置观察用的viewState
                    lyricResult.value = ViewState.Success(result)
                    //处理数据
                    lyricModelList.clear()
                    lyricModelList.addAll(LyricUtil.parse(result))
                    //计算当前播放位置的歌词索引
                    curLyricIndex = lyricModelList.indexOfFirst { lyricModel ->
                        curPlayPosition < lyricModel.time //找到第一个开始时间大于播放时间的位置
                    } - 1 //得到上一个歌词的索引（当前正在唱的歌词）
                } else {
                    lyricResult.value = ViewState.Empty
                }
            }.onFailure {
                lyricResult.value = ViewState.Error(it)
            }
        }
    }

    override fun onStatusChanged(status: PlayerStatus) {
    }

    //将viewModel作为监听器放入player，播放进度变化时的回调
    override fun onProgress(
        totalDuring: Int,      // 总时长（毫秒）
        currentPosition: Int,  // 当前播放位置（毫秒）
        percentage: Int        // 播放百分比（0-100）
    ) {
        //更新当前播放位置
        curPlayPosition = currentPosition
        //计算当前歌词索引
        curLyricIndex = lyricModelList.indexOfFirst {
            currentPosition < it.time //当前时间小于歌词的开始时间
        } - 1 //得到上一个歌词的索引（当前正在唱的歌词）
        //当播放位置超过最后一句歌词的时间时
        if(currentPosition > (lyricModelList.lastOrNull()?.time ?: 0)) {
            curLyricIndex = lyricModelList.size - 1
        }
    }

}

/**
 * 歌词模型（支持整行和逐字两种模式）
 */
data class LyricModel(
    val time: Long,                          // 开始时间（毫秒）
    val lyric: String? = null,               // 整行歌词内容
    val duration: Long = 0,                  // 持续时间（毫秒，用于整行）
    val words: List<LyricWordModel>? = null  // 逐字歌词列表（为空时表示整行模式）
)

/**
 * 逐字歌词模型
 */
data class LyricWordModel(
    val text: String,                        // 单个字
    val startTime: Long,                     // 该字开始时间（毫秒）
    val duration: Int                        // 该字持续时间（毫秒）
)