package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.http.api.SongApi
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.SongCommentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PlayMusicViewModel @Inject constructor(private val api: SongApi) : BaseViewStateViewModel() {

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
