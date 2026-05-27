package com.neo.lingxumusic.core.player


sealed class PlayerStatus {
    object IDLE: PlayerStatus()       // 空闲/初始状态
    object PREPARED: PlayerStatus()   // 已准备就绪（可以播放）
    object STARTED: PlayerStatus()    // 播放中
    object PAUSED: PlayerStatus()     // 已暂停
    object STOPPED: PlayerStatus()    // 已停止
    object COMPLETED: PlayerStatus()  // 播放完成
    object ERROR: PlayerStatus()      // 错误状态
}