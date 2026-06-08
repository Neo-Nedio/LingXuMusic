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
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MvPlayViewModel @Inject constructor(
    private val videoApi: VideoApi,
) : BaseViewStateViewModel() {

    // 复用的 TypeToken，避免重复创建
    private val nestedListType = object : TypeToken<List<List<SongMv>>>() {}.type

    // MV 视频数据（转成 BrushVideo 复用播放组件）
    var mvVideo by mutableStateOf<BrushVideo?>(null)
        private set

    // 页面状态 LiveData（供 ViewStateComponent 使用）
    val mvResult = ViewStateMutableLiveData<MvResult>()

    /**
     * 加载歌曲 MV
     */
    fun loadMv(albumAudioId: Long, songName: String?, singerName: String?) {
        // 每次加载前清空旧数据
        mvVideo = null

        launch(
            liveData = mvResult,
            handleResult = { result ->
                // 解析 MV 信息（兼容 [ {} ] 空对象格式）
                val nestedList = try {
                    result.data?.let {
                        Gson().fromJson<List<List<SongMv>>>(it, nestedListType)
                    }
                } catch (e: Exception) {
                    null
                }
                val songMv = nestedList?.firstOrNull()?.firstOrNull {
                    it.getVideoHash()?.isNotBlank() == true
                }

                val playUrl = result.playUrl

                if (playUrl.isNullOrBlank()) {
                    throw IllegalStateException("获取 MV 播放地址失败")
                }

                // 组装成 BrushVideo
                mvVideo = BrushVideo(
                    song = BrushSong(
                        songname = songMv?.mv_name ?: songName,
                        author_name = songMv?.singer ?: singerName,
                        author_cover = songMv?.authors?.firstOrNull()?.sizable_avatar,
                        album_cover = songMv?.getCoverUrl(),
                    ),
                    hp_mv_info = HpMvInfo(
                        play_info = PlayInfo(
                            src_file = SrcFile(
                                h264 = VideoUrl(play_url = playUrl)
                            )
                        ),
                        mv_info = MvSimpleInfo(
                            cover = songMv?.getCoverUrl(),
                            video_list = listOf(MvVideo(video_bss_img = songMv?.getCoverUrl()))
                        )
                    )
                )
            },
            judgeEmpty = { result ->
                val nestedList = try {
                    result.data?.let {
                        Gson().fromJson<List<List<SongMv>>>(it, nestedListType)
                    }
                } catch (e: Exception) {
                    null
                }
                val songMv = nestedList?.firstOrNull()?.firstOrNull {
                    it.getVideoHash()?.isNotBlank() == true
                }
                songMv?.getVideoHash().isNullOrBlank()
            }
        ) {
            // 1. 获取 MV 信息
            val mvResponse = videoApi.getSongMv(albumAudioId)
            val nestedList = try {
                mvResponse.data?.let {
                    Gson().fromJson<List<List<SongMv>>>(it, nestedListType)
                }
            } catch (e: Exception) {
                null
            }
            //获取mv的hash
            val songMv = nestedList?.firstOrNull()?.firstOrNull {
                it.getVideoHash()?.isNotBlank() == true
            }
            val videoHash = songMv?.getVideoHash()
            if (videoHash.isNullOrBlank()) {
                return@launch MvResult(
                    status = mvResponse.status,
                    error_code = mvResponse.error_code,
                    data = mvResponse.data
                )
            }

            // 2. 获取视频播放 URL
            val urlResponse = videoApi.getVideoUrl(videoHash)
            val videoUrlInfo = urlResponse.data?.entries?.firstOrNull {
                it.key.equals(videoHash, ignoreCase = true)
            }?.value
            val playUrl = videoUrlInfo?.getPlayUrl()

            // 3. 返回 MvResult，携带 playUrl
            MvResult(
                status = mvResponse.status,
                error_code = mvResponse.error_code,
                data = mvResponse.data,
                playUrl = playUrl
            )
        }
    }
}


class MvResult(
    status: Int = 0,
    error_code: Int = 0,
    data: JsonElement? = null,
    val playUrl: String? = null
) : BaseResult(status, error_code, data)
