package com.neo.lingxumusic.viewmodel.playMusic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.VideoApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.BrushSong
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.HpMvInfo
import com.neo.lingxumusic.model.MvSimpleInfo
import com.neo.lingxumusic.model.MvVideo
import com.neo.lingxumusic.model.PlayInfo
import com.neo.lingxumusic.model.SongMv
import com.neo.lingxumusic.model.SrcFile
import com.neo.lingxumusic.model.VideoUrl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MvPlayViewModel @Inject constructor(
    private val videoApi: VideoApi,
) : BaseViewStateViewModel() {

    // MV 视频数据（转成 BrushVideo 复用播放组件）
    var mvVideo by mutableStateOf<BrushVideo?>(null)
        private set

    // 页面状态 LiveData（供 ViewStateComponent 使用）
    val mvResult = ViewStateMutableLiveData<BaseResult>()

    /**
     * 加载歌曲 MV
     * 1. 先调用 getSongMv 获取 MV 信息（含 hash）
     * 2. 再调用 getVideoUrl 获取播放地址
     * 3. 组装成 BrushVideo
     */
    fun loadMv(albumAudioId: Long, songName: String?, singerName: String?) {
        launch(mvResult) {
            // 1. 获取 MV 信息
            val mvResult = videoApi.getSongMv(albumAudioId)
            val nestedListType = object : TypeToken<List<List<SongMv>>>() {}.type
            val nestedList = mvResult.data?.let { Gson().fromJson<List<List<SongMv>>>(it, nestedListType) }
            val songMv = nestedList?.firstOrNull()?.firstOrNull { it.getVideoHash()?.isNotBlank() == true }
            val videoHash = songMv?.getVideoHash()

            if (videoHash.isNullOrBlank()) {
                throw IllegalStateException("该歌曲暂无 MV")
            }

            // 2. 获取视频播放 URL
            val urlResult = videoApi.getVideoUrl(videoHash)
            val videoUrlInfo = urlResult.data?.entries?.firstOrNull {
                it.key.equals(videoHash, ignoreCase = true)
            }?.value
            val playUrl = videoUrlInfo?.getPlayUrl()

            if (playUrl.isNullOrBlank()) {
                throw IllegalStateException("获取 MV 播放地址失败")
            }

            // 3. 组装成 BrushVideo
            mvVideo = BrushVideo(
                song = BrushSong(
                    songname = songMv.mv_name ?: songName,
                    author_name = songMv.singer ?: singerName,
                    author_cover = songMv.authors?.firstOrNull()?.sizable_avatar,
                    album_cover = songMv.getCoverUrl(),
                ),
                hp_mv_info = HpMvInfo(
                    play_info = PlayInfo(
                        src_file = SrcFile(
                            h264 = VideoUrl(play_url = playUrl)
                        )
                    ),
                    mv_info = MvSimpleInfo(
                        cover = songMv.getCoverUrl(),
                        video_list = listOf(MvVideo(video_bss_img = songMv.getCoverUrl()))
                    )
                )
            )

            mvResult
        }
    }
}
