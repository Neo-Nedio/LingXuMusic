package com.neo.lingxumusic.core.player


interface IPlayerListener {
    // 播放器状态变化回调
    fun onStatusChanged(status: PlayerStatus)

    // 播放进度回调
    // totalDuring: 总时长（毫秒）
    // currentPosition: 当前播放位置（毫秒）
    // percentage: 进度百分比（0-100）
    fun onProgress(totalDuring: Int, currentPosition: Int, percentage: Int)
}