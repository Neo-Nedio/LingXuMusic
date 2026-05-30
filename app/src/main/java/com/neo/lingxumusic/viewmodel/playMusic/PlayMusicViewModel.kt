package com.neo.lingxumusic.viewmodel.playMusic

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentResult
import com.neo.lingxumusic.model.LyricResult
import com.neo.lingxumusic.utils.LyricUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayMusicViewModel @Inject constructor(private val api: SongApi) : BaseViewStateViewModel() {
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
    var lyricResult by mutableStateOf<LyricResult?>(null)
    //歌词解析后的结果
    val lyricModelList = mutableListOf<LyricModel>()


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
        viewModelScope.launch {
            runCatching {
                val searchResult = api.getLyricSearch(hash)
                if (searchResult.status != 200 || searchResult.errcode != 200) {
                    return@runCatching null
                }
                val candidate = searchResult.candidates
                    ?.firstOrNull { !it.id.isNullOrBlank() && !it.accesskey.isNullOrBlank() }
                    ?: return@runCatching null
                api.getLyric(
                    id = candidate.id!!,
                    accesskey = candidate.accesskey!!,
                )
            }.onSuccess { result ->
                if (!result?.decodeContent.isNullOrBlank()) {
                    lyricResult = result
                    lyricModelList.clear()
                    lyricModelList.addAll(LyricUtil.parse(result))
                    lyricModelList.forEach {
                        Log.e("neo", "getLyric $it")
                    }
                }
            }
        }
    }

}

/**
 * 歌词模型（支持整行和逐字两种模式）
 */
data class LyricModel(
    val time: Long,                          // 开始时间（毫秒）
    val lyric: String? = null,               // 整行歌词内容
    var tLyric: String? = null,              // 翻译歌词（可选）
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