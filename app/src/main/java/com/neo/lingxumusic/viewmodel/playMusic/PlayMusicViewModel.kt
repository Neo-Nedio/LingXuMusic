package com.neo.lingxumusic.viewmodel.playMusic

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentResult
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
}