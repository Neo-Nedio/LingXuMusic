package com.neo.lingxumusic.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Timer
import java.util.TimerTask

enum class VideoPlayerState {
    IDLE,       // 空闲状态
    BUFFERING,  // 缓冲中
    READY       // 准备就绪，可以播放
}

object VideoPlayController {

    // 当前播放的视频索引
    var curVideoIndex by mutableIntStateOf(-1)
    // 当前播放的视频 URL
    var curVideoUrl by mutableStateOf<String?>(null)
    // 播放器状态（空闲/缓冲/就绪）
    var playStatus by mutableStateOf(VideoPlayerState.IDLE)
    // 是否正在播放
    var videoPlaying by mutableStateOf(false)
    // 播放进度（0-100）
    var videoProgress by mutableIntStateOf(0)

    // 底层 MediaPlayer 实例
    private var mediaPlayer: MediaPlayer? = null
    // 是否正在拖动进度条
    private var isSeeking = false
    // 待加载的视频 URL（Surface 未就绪时暂存）
    private var pendingVideoUrl: String? = null
    // 正在加载的视频 URL
    private var loadingVideoUrl: String? = null
    // Surface 是否已准备好
    private var surfaceReady = false
    // 当前绑定的 Surface
    private var currentSurface: Surface? = null

    // 定时器，用于更新播放进度
    private var timer = Timer()
    // 进度更新任务
    private var updateDuringTask: TimerTask? = null
    // 主线程 Handler，用于更新 UI
    private val mainHandler = Handler(Looper.getMainLooper())

    // 初始化 MediaPlayer
    fun initIfNeeded(context: Context) {
        if (mediaPlayer != null) {
            return
        }
        timer = Timer()
        mediaPlayer = MediaPlayer().apply {
            // 设置音频属性（视频播放）
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)  // 内容类型：视频
                    .setUsage(AudioAttributes.USAGE_MEDIA)              // 用途：媒体播放
                    .build()
            )
            // 视频准备完成监听
            setOnPreparedListener { player ->
                playStatus = VideoPlayerState.READY      // 状态变为就绪
                loadingVideoUrl = null                   // 清除加载标记
                player.isLooping = true                  // 设置循环播放
                // 如果音乐正在播放，先暂停音乐（避免冲突）
                if (MusicPlayController.isPlaying()) {
                    MusicPlayController.pause()
                }
                player.start()                           // 开始播放
                videoPlaying = true                      // 标记播放中
                startUpdateDuringTask()                  // 启动进度更新定时器
            }
            // 错误监听
            setOnErrorListener { _, _, _ ->
                playStatus = VideoPlayerState.IDLE       // 状态变为空闲
                loadingVideoUrl = null                   // 清除加载标记
                videoPlaying = false                     // 标记未播放
                updateDuringTask?.cancel()               // 停止进度更新
                true                                      // 表示错误已处理
            }
        }
    }

    // 绑定视频渲染 Surface（当显示视频的组件准备好时调用）
    fun attachSurface(surface: Surface) {
        surfaceReady = true                     // 标记 Surface 已就绪
        currentSurface = surface               // 保存当前 Surface
        mediaPlayer?.setSurface(surface)       // 将 Surface 设置给 MediaPlayer

        // 如果有待加载的视频 URL，或者已有当前视频 URL，则开始加载
        val url = pendingVideoUrl ?: curVideoUrl ?: return
        loadVideo(url)
    }

    // 解绑视频渲染 Surface（当显示视频的组件销毁时调用）
    fun detachSurface(surface: Surface) {
        // 如果当前绑定的 Surface 不是传入的 Surface，直接返回
        if (currentSurface != surface) {
            return
        }
        surfaceReady = false                    // 标记 Surface 已失效
        currentSurface = null                  // 清空当前 Surface
        mediaPlayer?.setSurface(null)          // 从 MediaPlayer 中移除 Surface
    }

    //滑动进度条
    fun onSeeking(progress: Int) {
        isSeeking = true
        videoProgress = progress
    }

    //滑动进度条松手回调
    fun seekTo(progress: Int) {
        isSeeking = false
        val player = mediaPlayer ?: return
        if (playStatus != VideoPlayerState.READY || player.duration <= 0) {
            return
        }
        player.seekTo(player.duration * progress / 100)
    }

    // 暂停视频播放
    fun pauseVideo() {
        // 播放器未就绪 或 没有视频URL → 直接返回
        if (playStatus != VideoPlayerState.READY || curVideoUrl == null) {
            return
        }
        // 如果正在播放，则暂停
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        videoPlaying = false           // 标记为非播放状态
        updateDuringTask?.cancel()     // 停止进度更新任务
    }

    // 恢复视频播放
    fun resumeVideo() {
        // 播放器未就绪 或 没有视频URL → 直接返回
        if (playStatus != VideoPlayerState.READY || curVideoUrl == null) {
            return
        }
        mediaPlayer?.start()           // 开始播放
        videoPlaying = true            // 标记为播放状态
        startUpdateDuringTask()        // 重新启动进度更新任务
    }

    // 加载指定 URL 的视频
    fun loadVideo(url: String) {
        // MediaPlayer 未初始化 → 直接返回
        val player = mediaPlayer ?: return

        // 正在加载同一个视频且处于缓冲状态 → 直接返回（避免重复加载）
        if (loadingVideoUrl == url && playStatus == VideoPlayerState.BUFFERING) {
            return
        }

        // Surface 未就绪 → 暂存 URL，等待 Surface 准备好后再加载
        if (!surfaceReady) {
            pendingVideoUrl = url
            return
        }

        // 开始加载视频
        pendingVideoUrl = null          // 清除待加载标记
        loadingVideoUrl = url           // 标记正在加载的视频
        playStatus = VideoPlayerState.BUFFERING  // 状态设为缓冲中

        try {
            player.reset()              // 重置播放器（清除之前的资源）
            currentSurface?.let { player.setSurface(it) }  // 重新绑定 Surface
            player.setDataSource(url)   // 设置视频数据源
            player.prepareAsync()       // 异步准备（不会阻塞主线程）
        } catch (_: Exception) {
            // 加载失败，清除状态
            loadingVideoUrl = null
            playStatus = VideoPlayerState.IDLE
        }
    }

    // 切换到新的视频
    fun switchVideo(newIndex: Int, url: String?) {
        // 索引相同 → 直接返回，不处理
        if (newIndex == curVideoIndex) {
            return
        }

        // 当前有视频正在播放 → 先重置播放器
        if (curVideoIndex >= 0) {
            resetPlayer()
        }

        // 更新当前视频索引
        curVideoIndex = newIndex

        // 如果 URL 有效，更新当前视频 URL
        if (!url.isNullOrBlank()) {
            curVideoUrl = url
        }
    }

    //释放资源
    fun release() {
        updateDuringTask?.cancel()
        timer.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        surfaceReady = false
        currentSurface = null
        pendingVideoUrl = null
        loadingVideoUrl = null
        curVideoIndex = -1
        curVideoUrl = null
        playStatus = VideoPlayerState.IDLE
        videoPlaying = false
        videoProgress = 0
    }

    // 重置播放器（清空状态，释放当前视频资源）
    private fun resetPlayer() {
        videoProgress = 0               // 进度归零
        videoPlaying = false            // 标记未播放
        loadingVideoUrl = null          // 清除正在加载的 URL
        updateDuringTask?.cancel()      // 停止进度更新任务

        // 重置 MediaPlayer（清除当前数据源，回到空闲状态）
        mediaPlayer?.run {
            try {
                reset()                 // 重置播放器
            } catch (_: IllegalStateException) {
                // 处于无效状态时忽略异常
            }
        }

        playStatus = VideoPlayerState.IDLE   // 状态设为空闲
    }

    // 启动进度更新任务（每秒更新一次播放进度）
    private fun startUpdateDuringTask() {
        // 取消之前的更新任务
        updateDuringTask?.cancel()

        // 创建新的定时任务
        updateDuringTask = object : TimerTask() {
            override fun run() {
                // 条件1：正在拖动进度条 → 不更新（避免冲突）
                if (isSeeking) return
                // 条件2：播放器状态不是就绪 → 不更新
                if (playStatus != VideoPlayerState.READY) return

                // 获取 MediaPlayer 实例
                val player = mediaPlayer ?: return

                // 获取视频总时长（可能抛异常）
                val duration = try {
                    player.duration
                } catch (_: IllegalStateException) {
                    return
                }

                // 总时长无效 → 不更新
                if (duration <= 0) return

                // 切换到主线程更新 UI 进度
                mainHandler.post {
                    // 计算当前进度百分比 = (当前位置 × 100) / 总时长
                    videoProgress = (player.currentPosition.toFloat() * 100 / duration).toInt()
                }
            }
        }.apply {
            // 立即启动，每 1000ms（1秒）执行一次
            timer.schedule(this, 0, 1000)
        }
    }
}
