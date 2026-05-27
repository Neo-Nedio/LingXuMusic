package com.neo.lingxumusic.core.player

import android.media.MediaPlayer
import android.util.Log
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.hilt.entrypoint.EntryPointFinder
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*

object Player : IPlayer,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnErrorListener {

    private var mStatus: PlayerStatus = PlayerStatus.IDLE     // 当前播放状态
    var mCurSong: Song? = null                                 // 当前播放的歌曲

    private val mMediaPlayer = MediaPlayer()                   // 底层播放器
    /*
        监听器实现了onStatusChanged和onProgress方法
        在player中，当状态改变和进度改变时会把当前状态或进度调用不同方法传递过去
        ui层操作的是监听器，监听器调用这个静态类player控制MediaPlayer
    */
    private val mListeners = ArrayList<IPlayerListener>()     // 状态监听器列表


    private val mTimer: Timer = Timer()
    private var mUpdateDuringTask: TimerTask? = null          // 定时更新进度的任务
    private var mJob: Job? = null                             // 协程任务（获取播放地址）

/*  CoroutineScope	协程作用域，用于启动协程
    SupervisorJob()	一个特殊的 Job，子协程失败不影响其他子协程
    Dispatchers.Main	在主线程执行（UI 线程）
    +	将两个元素组合成一个 CoroutineContext*/
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val songApi = EntryPointFinder.getSongApi()
    private val loginApi = EntryPointFinder.getLoginApi()

    //为 MediaPlayer 设置各种监听器，由 Player 自身实现
    init {
        // this 就是 Player 实例本身
        // 因为 Player 实现了这些接口，所以可以直接传给 setXxxListener
        mMediaPlayer.setOnCompletionListener(this)      // 播放完成
        mMediaPlayer.setOnPreparedListener(this)        // 准备就绪
        mMediaPlayer.setOnBufferingUpdateListener(this) // 缓冲中
        mMediaPlayer.setOnErrorListener(this)           // 错误
    }

    //监听器管理
    fun addListener(listener: MusicPlayController) {
        mListeners.add(listener)
    }
    fun removeListener(listener: IPlayerListener) {
        mListeners.remove(listener)
    }

    // 设置数据源
    override fun setDataSource(songBean: Song) {
        mCurSong = songBean
    }

    //从零开始播放
    override fun start() {
        if (mStatus == PlayerStatus.STARTED
            || mStatus == PlayerStatus.PREPARED
            || mStatus == PlayerStatus.PAUSED
            || mStatus == PlayerStatus.COMPLETED
        ) {
            stop()
        }
        mCurSong?.let {
            getSongUrl(it.hash)
        }
    }

    //todo 加入音质选择
    private fun getSongUrl(hash: String?) {
        if (hash.isNullOrBlank()) {
            setStatus(PlayerStatus.ERROR)
            return
        }

        //取消之前的加载任务
        mJob?.cancel()

        //启动新的协程
        mJob = playerScope.launch {
            try {
                getSongUrlAndPlay(hash)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string().orEmpty()
                val needVerify = runCatching {
                    JSONObject(body).optString("error").contains("验证")
                }.getOrDefault(false)
                if (needVerify) {
                    runCatching {
                        withContext(Dispatchers.IO) { loginApi.AuthData() }
                        if (mCurSong?.hash == hash) getSongUrlAndPlay(hash)
                    }.onFailure { setStatus(PlayerStatus.ERROR) }
                } else {
                    setStatus(PlayerStatus.ERROR)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setStatus(PlayerStatus.ERROR)
            }
        }
    }

    private suspend fun getSongUrlAndPlay(hash: String) {
        val url = withContext(Dispatchers.IO) { //todo 检查播放地址是否可用
            songApi.getSongUrl(hash).url?.firstOrNull()
        }
        if (url.isNullOrBlank() || mCurSong?.hash != hash) {
            if (url.isNullOrBlank()) setStatus(PlayerStatus.ERROR)
            return
        }
        mMediaPlayer.reset()
        mMediaPlayer.setDataSource(url)
        mMediaPlayer.prepareAsync()
    }

    //暂停播放
    override fun pause() {
        if (mStatus == PlayerStatus.STARTED) {
            mUpdateDuringTask?.cancel() //取消定时任务，停止更新进度
            setStatus(PlayerStatus.PAUSED)
            mMediaPlayer.pause() //调用 MediaPlayer 暂停播放
        }
    }

    //恢复播放
    override fun resume() {
        if (mStatus == PlayerStatus.PAUSED) {
            innerStartPlay()
        }
    }

    //停止播放
    override fun stop() {
        mUpdateDuringTask?.cancel()
        mMediaPlayer.stop() //停止播放器（释放资源）
        setStatus(PlayerStatus.STOPPED) //先设置为 STOPPED
        setStatus(PlayerStatus.IDLE) //再设置为 IDLE
    }

    //跳转到指定位置
    override fun seekTo(position: Int) {
        mMediaPlayer.seekTo(position)
    }

    //播放完成回调
    override fun onCompletion(mp: MediaPlayer) {
        mUpdateDuringTask?.cancel() //	停止定时更新进度的任务
        setStatus(PlayerStatus.COMPLETED) //通过改变状态告诉监听器，让监听器处理
        //自动播放下一首在listener里的onStatusChanged实现
    }

    //准备完成回调
    //当调用 prepareAsync() 后，MediaPlayer 准备完成时，系统会自动调用此方法
    override fun onPrepared(mp: MediaPlayer?) {
        setStatus(PlayerStatus.PREPARED)
        innerStartPlay()
    }

    //开始播放音乐，并启动定时任务更新播放进度
    private fun innerStartPlay() {
        mMediaPlayer.start()                      // 1. 开始播放
        setStatus(PlayerStatus.STARTED)           // 2. 更新状态
        mUpdateDuringTask?.cancel()               // 3. 取消旧的定时任务
        mUpdateDuringTask = object : TimerTask() { // 4. 创建新的定时任务
            override fun run() {
                setProgress()                     // 5. 更新进度
            }
        }.apply {
            mTimer.schedule(this, 0, 1000)        // 6. 立即执行，然后每秒执行一次
        }
    }

    //缓冲进度更新
    //当网络流媒体（如在线播放）的缓冲进度更新时调用。
    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mUpdateDuringTask?.cancel()      // 1. 取消进度更新任务
        setStatus(PlayerStatus.ERROR)    // 2. 设置错误状态
        setStatus(PlayerStatus.IDLE)     // 3. 重置为空闲状态
        showToast("播放失败")             // 4. 提示用户
        return true                      // 5. 表示错误已处理
    }

    //设置状态
    private fun setStatus(status: PlayerStatus) {
        mStatus = status                          // 1. 更新内部状态
        mListeners.forEach {                      // 2. 遍历所有监听器
            it.onStatusChanged(mStatus)           // 3. 通知状态变化
        }
    }

    //更新播放进度
    private fun setProgress() {
        mListeners.forEach {
            //计算当前播放进度百分比
            val percentage = ((mMediaPlayer.currentPosition * 100f / mMediaPlayer.duration) + 0.5f).toInt()
            it.onProgress(mMediaPlayer.duration, mMediaPlayer.currentPosition, percentage)
        }
    }
}