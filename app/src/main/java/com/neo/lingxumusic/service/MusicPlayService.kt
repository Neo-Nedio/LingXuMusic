package com.neo.lingxumusic.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.neo.lingxumusic.broadcast.MusicNotificationReceiver
import com.neo.lingxumusic.core.LingxuApplication
import com.neo.lingxumusic.utils.MusicNotificationHelper

/*
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                              音乐播放器通知栏系统架构图                                        │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                                    UI 层 (Compose)                                   │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │  播放按钮    │  │  暂停按钮    │  │  上一曲      │  │  下一曲      │                 │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │   │
│  └─────────┼────────────────┼────────────────┼────────────────┼─────────────────────────┘   │
│            │                │                │                │                           │
│            ▼                ▼                ▼                ▼                           │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                           MusicPlayController                                       │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │  play()  pause()  resume()  playNext()  playPrev()  seekTo()  seeking()     │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  │                                      │                                              │   │
│  │                                      ▼                                              │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                         状态变量                                             │   │   │
│  │  │  songList, curIndex, progress, curPositionStr, totalDuringStr, playing     │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────┬──────────────────────────────────────────┘   │
│                                              │                                              │
│                                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                                    Player                                           │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                          MediaPlayer 操作                                     │   │   │
│  │  │  start()  pause()  stop()  seekTo()  setDataSource()  prepareAsync()        │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                          回调事件                                            │   │   │
│  │  │  onPrepared() → 开始播放                                                    │   │   │
│  │  │  onCompletion() → 播放完成                                                  │   │   │
│  │  │  onError() → 播放错误                                                       │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                        EventBus 事件发送                                     │   │   │
│  │  │  play() 时 → post(PlaySongEvent)                                           │   │   │
│  │  │  pause() 时 → post(PauseSongEvent)                                         │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────┬──────────────────────────────────────────┘   │
│                                              │                                              │
│                                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                              EventBus 事件总线                                       │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────┐    ┌─────────────────────────┐                        │   │
│  │  │     PlaySongEvent       │    │     PauseSongEvent      │                        │   │
│  │  └────────────┬────────────┘    └────────────┬────────────┘                        │   │
│  │               │                              │                                      │   │
│  │               └──────────────┬───────────────┘                                      │   │
│  │                              ▼                                                      │   │
│  │                    MusicNotificationHelper                                          │   │
│  │                    (订阅者，接收事件)                                                │   │
│  └──────────────────────────────────────────┬──────────────────────────────────────────┘   │
│                                              │                                              │
│                                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                         MusicNotificationHelper                                     │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                         初始化 (init)                                         │   │   │
│  │  │  1. 创建通知渠道 (Android 8.0+)                                              │   │   │
│  │  │  2. 创建 RemoteViews (自定义布局)                                            │   │   │
│  │  │  3. 设置按钮的 PendingIntent (广播)                                          │   │   │
│  │  │  4. 构建 Notification                                                       │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                       EventBus 事件处理                                       │   │   │
│  │  │                                                                             │   │   │
│  │  │  收到 PlaySongEvent ──────────────────────────────────────────┐             │   │   │
│  │  │       │                                                      │             │   │   │
│  │  │       ▼                                                      ▼             │   │   │
│  │  │  设置按钮图标为"暂停"                                   设置按钮图标为"播放"  │   │   │
│  │  │       │                                                      │             │   │   │
│  │  │       └──────────────────────┬───────────────────────────────┘             │   │   │
│  │  │                              ▼                                              │   │   │
│  │  │                    notify() 刷新通知栏                                      │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                        updateNotificationUI()                               │   │   │
│  │  │                                                                             │   │   │
│  │  │  1. 获取当前歌曲 → 更新歌名、歌手名                                          │   │   │
│  │  │  2. 根据播放状态更新按钮图标                                                │   │   │
│  │  │  3. loadCover() 异步加载封面                                                │   │   │
│  │  │  4. notify() 刷新通知栏                                                     │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────┬──────────────────────────────────────────┘   │
│                                              │                                              │
│                                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                         MusicPlayService (前台服务)                                  │   │
│  │                                                                                     │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │                         生命周期                                             │   │   │
│  │  │                                                                             │   │   │
│  │  │  onCreate() ──────────────────────────────────────────────────────────────┐ │   │   │
│  │  │       │                                                                   │ │   │   │
│  │  │       ▼                                                                   │ │   │   │
│  │  │  registerBroadcastReceiver() ── 注册 MusicNotificationReceiver            │ │   │   │
│  │  │                                                                             │ │   │   │
│  │  │  onStartCommand()                                                           │ │   │   │
│  │  │       │                                                                     │ │   │   │
│  │  │       ▼                                                                     │ │   │   │
│  │  │  MusicNotificationHelper.init() ── 初始化通知栏                             │ │   │   │
│  │  │       │                                                                     │ │   │   │
│  │  │       ▼                                                                     │ │   │   │
│  │  │  startForeground() ── 成为前台服务，显示通知栏                              │ │   │   │
│  │  │                                                                             │ │   │   │
│  │  │  onDestroy()                                                                │ │   │   │
│  │  │       │                                                                     │ │   │   │
│  │  │       ▼                                                                     │ │   │   │
│  │  │  unRegisterBroadcastReceiver() ── 注销广播接收器                            │ │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘ │   │   │
│  └──────────────────────────────────────────┬──────────────────────────────────────────┘   │
│                                              │                                              │
│                                              ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐   │
│  │                      MusicNotificationReceiver (广播接收器)                          │   │
│  │                                                                                     │   │
│  │  onReceive()                                                                        │   │
│  │       │                                                                             │   │
│  │       ▼                                                                             │   │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐   │   │
│  │  │  根据 KEY_EXTRA 判断:                                                         │   │   │
│  │  │                                                                             │   │   │
│  │  │  ACTION_PLAY  →  MusicPlayController.isPlaying()? pause() : resume()       │   │   │
│  │  │  ACTION_PRE   →  MusicPlayController.play(prevIndex)                        │   │   │
│  │  │  ACTION_NEXT  →  MusicPlayController.play(nextIndex)                       │   │   │
│  │  └─────────────────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                             │
└─────────────────────────────────────────────────────────────────────────────────────────────┘*/
class MusicPlayService : Service() {

    private var mReceiver: MusicNotificationReceiver? = null  // 广播接收器

    companion object {
        const val ACTION_START_MUSIC_SERVICE = "ACTION_START_MUSIC_SERVICE"
        fun start() {
            // 1. 创建 Intent，指定要启动的 Service
            val intent = Intent(LingxuApplication.getAppContext(), MusicPlayService::class.java).apply {
                action = ACTION_START_MUSIC_SERVICE  // 设置 action，用于在 onStartCommand 中识别
            }

            // 2. 根据 Android 版本选择启动方式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上：必须使用 startForegroundService
                LingxuApplication.getAppContext().startForegroundService(intent)
            } else {
                // Android 7.0 及以下：普通 startService
                LingxuApplication.getAppContext().startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerBroadcastReceiver() // 注册广播接收器
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            if (it == ACTION_START_MUSIC_SERVICE) {
                MusicNotificationHelper.init {
                    startForeground(
                        MusicNotificationHelper.NOTIFICATION_ID,
                        MusicNotificationHelper.getNotification()
                    )
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterBroadcastReceiver()  // 注销广播接收器
    }

    override fun onBind(intent: Intent?) = null // 不需要绑定，返回 null

    private fun registerBroadcastReceiver() {
        if (mReceiver == null) {
            mReceiver = MusicNotificationReceiver()
            registerReceiver(mReceiver, IntentFilter().apply {
                addAction(MusicNotificationReceiver.ACTION_MUSIC_NOTIFICATION)
            })
        }
    }

    private fun unRegisterBroadcastReceiver() {
        mReceiver?.let {
            unregisterReceiver(it)
        }
    }

}