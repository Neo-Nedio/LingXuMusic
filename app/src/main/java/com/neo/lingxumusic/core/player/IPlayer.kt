package com.neo.lingxumusic.core.player

import com.neo.lingxumusic.model.Song


interface IPlayer {
    fun setDataSource(songBean: Song)  // 设置播放源（歌曲信息）
    fun start()                         // 开始播放
    fun pause()                         // 暂停播放
    fun resume()                        // 恢复播放（从暂停处继续）
    fun stop()                          // 停止播放（释放资源）
    fun seekTo(position: Int)           // 跳转到指定位置（毫秒）
}