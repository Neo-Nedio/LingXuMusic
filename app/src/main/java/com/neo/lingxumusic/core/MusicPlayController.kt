package com.neo.lingxumusic.core

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.core.player.IPlayerListener
import com.neo.lingxumusic.core.player.Player
import com.neo.lingxumusic.core.player.PlayerStatus
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.core.player.PlayMode

object MusicPlayController  : IPlayerListener {
    var songList = mutableStateListOf<Song>()     // 歌单列表（可观察）
    var curIndex by mutableStateOf(0)             // 当前播放索引
        private set
    var progress by mutableStateOf(0)             // 进度百分比
    var curPositionStr by mutableStateOf("00:00") // 当前播放时间
    var totalDuringStr by mutableStateOf("00:00") // 总时长

    private var totalDuring = 0                   // 总时长（毫秒）
    private var seeking = false                   // 是否正在拖动进度条
    private var playing by mutableStateOf(false)  // 是否正在播放，onStatusChanged()维护

    var playMode by mutableStateOf<PlayMode>(PlayMode.LOOP)
        private set

    init {
        Player.addListener(this)  // 注册监听器
    }

    //直接设置歌单并播放
    fun setDataSource(songs: List<Song>,  hash: String?) {
        // 过滤出有 hash 的歌曲
        val validSongs = songs.filter { !it.hash.isNullOrEmpty() }

        if (validSongs.isEmpty()) {
            showToast("没有可播放的歌曲")
            return
        }

        //  根据传入的 hash 查找 curIndex
        val newIndex = if (hash != null) {
            validSongs.indexOfFirst { it.hash == hash }.coerceAtLeast(0)
        } else {
            0
        }

        // 一次性更新歌单和索引
        songList.clear()
        songList.addAll(validSongs)
        curIndex = newIndex

        //播放
        Player.setDataSource(songList[curIndex])
        Player.start()
    }

    fun play(index: Int) {
        if (songList.isEmpty()) {
            showToast("歌单为空")
            return
        }

        // 检查索引是否有效
        if (index !in songList.indices) {
            showToast("歌曲不存在")
            return
        }

        // 播放
        curIndex = index
        Player.setDataSource(songList[curIndex])
        Player.start()
    }

    fun pause() {
        Player.pause()      // 调用底层播放器的暂停方法
    }

    fun resume() {
        Player.resume()     // 调用底层播放器的恢复方法
    }

    fun isPlaying(): Boolean {
        return playing      // 返回当前播放状态（由 onStatusChanged 更新）
    }

    // 拖动中
    fun seeking(progress: Int) {
        seeking = true                           // 标记正在拖动
        this.progress = progress                 // 更新进度百分比（0-100）
        if(totalDuring != 0) {
            // 根据拖动进度计算当前时间
            val currentMs = totalDuring * progress / 100
            this.curPositionStr = StringUtil.formatMilliseconds(currentMs)
        }
    }

    //拖动结束
    fun seekTo(progress: Int) {
        this.progress = progress                 // 更新最终进度
        if (totalDuring != 0) {
            Player.seekTo(progress * totalDuring / 100)  // 移动播放器播放到的地方
        }
        seeking = false                          // 释放锁
    }

    //判断某个歌曲是否正在播放
    fun isPlaying(song: Song) = songList.getOrNull(curIndex)?.hash == song.hash

    //上一首歌曲
    fun getPreIndex(): Int {
        return when (playMode) {
            PlayMode.RANDOM -> {
                if (songList.size <= 1) {
                    curIndex
                } else {
                    var randomIndex = (songList.indices).random()
                    while (randomIndex == curIndex) {
                        randomIndex = (songList.indices).random()
                    }
                    randomIndex
                }
            }
            else -> {
                if (curIndex == 0) songList.size - 1 else curIndex - 1
            }
        }
    }

    //下一首歌曲
    fun getNextIndex(): Int {
        return when (playMode) {
            PlayMode.RANDOM -> {
                if (songList.size <= 1) {
                    curIndex
                } else {
                    var randomIndex = (songList.indices).random()
                    while (randomIndex == curIndex) {
                        randomIndex = (songList.indices).random()
                    }
                    randomIndex
                }
            }
            else -> {
                if (curIndex == songList.size - 1) 0 else curIndex + 1
            }
        }
    }

    fun changePlayMode(playMode: PlayMode) {
        this.playMode = playMode
    }

    //状态变化回调
    override fun onStatusChanged(status: PlayerStatus) {
        // 1. 更新播放状态
        playing = status == PlayerStatus.STARTED

        // 2. 根据状态执行不同逻辑
        when (status) {
            PlayerStatus.COMPLETED -> autoPlayNext(fromError = false) // 播放完成，自动下一首
            PlayerStatus.ERROR -> {
                showToast("播放失败")
                Handler(Looper.getMainLooper()) //延迟400ms后变化，防止过快ui来不及更新而请求错误
                    .postDelayed({ autoPlayNext(fromError = true) }, //自动播放下一首
                        400)
            }
            PlayerStatus.STOPPED -> {
                totalDuringStr = "00:00"                   // 重置总时长显示
                curPositionStr = "00:00"                   // 重置当前时间显示
                progress = 0                               // 重置进度条
            }
            else -> {}                                      // 其他状态（PREPARED、STARTED、PAUSED 等）
        }
    }

    private fun autoPlayNext(fromError: Boolean) {
        if (playMode == PlayMode.SINGLE && !fromError) { //单曲循环遇到错误时，应该继续下一首，而不是一直错误播放
            resume()
        } else {
            play(getNextIndex())
        }
    }

    //进度更新回调
    override fun onProgress(totalDuring: Int, currentPosition: Int, percentage: Int) {
        if(!seeking) { // 不在拖动状态才更新
            this.totalDuring = totalDuring
            totalDuringStr = StringUtil.formatMilliseconds(totalDuring)
            curPositionStr = StringUtil.formatMilliseconds(currentPosition)
            progress = percentage
        }
    }
}