package com.neo.lingxumusic.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.neo.lingxumusic.MainActivity
import com.neo.lingxumusic.R
import com.neo.lingxumusic.broadcast.MusicNotificationReceiver
import com.neo.lingxumusic.core.LingxuApplication
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.player.event.ChangeSongEvent
import com.neo.lingxumusic.core.player.event.PauseSongEvent
import com.neo.lingxumusic.core.player.event.PlaySongEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

object MusicNotificationHelper {

    const val CHANNEL_ID = "channel_id_music"
    const val CHANNEL_NAME = "聆序音乐"
    const val NOTIFICATION_ID = 100

    private var mNotification: Notification? = null
    private var mRemoteViews: RemoteViews? = null
    private var mNotificationManager: NotificationManager? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 注册 EventBus
    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun init(callback: () -> Unit) {
        val context = LingxuApplication.getAppContext()
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initNotification()  // 创建通知栏
        callback.invoke()   // 回调启动前台服务
    }

    private fun initNotification() {
        initRemoteViews() // 创建 RemoteViews

        // 2. 创建点击通知栏的 PendingIntent（跳转到 MainActivity）
        val context = LingxuApplication.getAppContext()
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            pendingIntentFlags() // flags（FLAG_UPDATE_CURRENT + FLAG_IMMUTABLE）
        )

        // 3. Android 8.0+ 创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,                          // 渠道 ID
                CHANNEL_NAME,                        // 渠道名称（显示给用户）
                NotificationManager.IMPORTANCE_HIGH  // 重要性：高（会弹窗、出声）
            ).apply {
                enableLights(false)      // 关闭呼吸灯
                enableVibration(false)   // 关闭震动
            }
            mNotificationManager!!.createNotificationChannel(channel)
        }

        // 4. 构建通知对象
        mNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_music_notification)
            .setCustomContentView(mRemoteViews)
            .setOngoing(true)
            .build()

        // 5. 更新通知栏 UI
        updateNotificationUI()
    }

    private fun initRemoteViews() {
        val context = LingxuApplication.getAppContext()

        //按钮：发送广播给MusicNotificationReceiver处理

        // 1. 创建 RemoteViews（绑定自定义布局文件）
        mRemoteViews = RemoteViews(context.packageName, R.layout.layout_music_notification)

        // 2. 播放/暂停按钮
        val playIntent = Intent(MusicNotificationReceiver.ACTION_MUSIC_NOTIFICATION).apply {
            putExtra(MusicNotificationReceiver.KEY_EXTRA, MusicNotificationReceiver.ACTION_PLAY)
        }
        mRemoteViews?.setOnClickPendingIntent(
            R.id.ivPlay,
            PendingIntent.getBroadcast(context, 1, playIntent, pendingIntentFlags())
        )

        // 3. 上一曲按钮
        val preIntent = Intent(MusicNotificationReceiver.ACTION_MUSIC_NOTIFICATION).apply {
            putExtra(MusicNotificationReceiver.KEY_EXTRA, MusicNotificationReceiver.ACTION_PRE)
        }
        mRemoteViews?.setOnClickPendingIntent(
            R.id.ivPre,
            PendingIntent.getBroadcast(context, 2, preIntent, pendingIntentFlags())
        )

        // 4. 下一曲按钮
        val nextIntent = Intent(MusicNotificationReceiver.ACTION_MUSIC_NOTIFICATION).apply {
            putExtra(MusicNotificationReceiver.KEY_EXTRA, MusicNotificationReceiver.ACTION_NEXT)
        }
        mRemoteViews?.setOnClickPendingIntent(
            R.id.ivNext,
            PendingIntent.getBroadcast(context, 3, nextIntent, pendingIntentFlags())
        )
    }

    // EventBus 的事件订阅者，用于监听播放/暂停事件并更新通知栏按钮图标
    @Subscribe(threadMode = ThreadMode.MAIN) //回调在主线程执行（可以更新 UI）
    fun onEvent(event: PauseSongEvent) {
        // 收到暂停事件 → 更新通知栏图标为"播放"
        mRemoteViews?.setImageViewResource(R.id.ivPlay, R.drawable.ic_music_notification_play)
        mNotificationManager?.notify(NOTIFICATION_ID, mNotification) //刷新通知栏
    }
    @Subscribe(threadMode = ThreadMode.MAIN) //回调在主线程执行（可以更新 UI）
    fun onEvent(event: PlaySongEvent) {
        // 收到播放事件 → 更新通知栏图标为"暂停"
        mRemoteViews?.setImageViewResource(R.id.ivPlay, R.drawable.ic_music_notification_pause)
        mNotificationManager?.notify(NOTIFICATION_ID, mNotification) //刷新通知栏
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChangeSongEvent) {
        updateNotificationUI()
    }

    fun getNotification() = mNotification

    fun getNotificationManager() = mNotificationManager

    //更新文字和图标
    private fun updateNotificationUI() {
        // 1. 获取当前播放的歌曲，如果没有则返回
        val song = MusicPlayController.songList.getOrNull(MusicPlayController.curIndex) ?: return

        // 2. 更新 RemoteViews 中的 UI
        mRemoteViews?.run {
            val name = song.name.orEmpty()
            val (singer, songName) = StringUtil.parseSongName(name)

            // 设置歌名
            setTextViewText(R.id.tvSongName, songName)

            // 设置歌手名
            setTextViewText(R.id.tvAuthor, singer)

            // 设置播放/暂停按钮图标
            setImageViewResource(
                R.id.ivPlay,
                if (MusicPlayController.isPlaying()) {
                    R.drawable.ic_music_notification_pause  // 播放中 → 显示"暂停"图标
                } else {
                    R.drawable.ic_music_notification_play   // 暂停中 → 显示"播放"图标
                }
            )
        }

        // 3. 加载封面图片
        loadCover(song.cover?.replaceSize())

        // 4. 刷新通知栏
        mNotificationManager?.notify(NOTIFICATION_ID, mNotification)
    }

    private fun loadCover(url: String?) {
        // 1. URL 无效则直接返回
        if (url.isNullOrBlank()) return

        val context = LingxuApplication.getAppContext()

        // 2. 启动协程（主线程）
        scope.launch {
            // 3. 切换到 IO 线程加载图片
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    // 创建 Coil 图片加载器
                    val loader = ImageLoader(context)

                    // 构建图片请求
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false)  // 禁止硬件位图，因为 RemoteViews 需要 Bitmap
                        .build()

                    // 执行请求
                    val result = loader.execute(request)

                    // 成功则转为 Bitmap，并处理圆角
                    if (result is SuccessResult) {
                        BitmapUtil.getRoundedCornerBitmap(result.image.toBitmap(), 30)
                    } else {
                        null
                    }
                }.getOrNull()  // 失败返回 null
            }

            // 4. 加载失败则返回
            if (bitmap == null) return@launch

            // 5. 切换到主线程更新 UI（已经在主线程，直接执行）
            mRemoteViews?.setImageViewBitmap(R.id.ivCover, bitmap)
            mNotificationManager?.notify(NOTIFICATION_ID, mNotification) //刷新通知栏
        }
    }

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }
}
