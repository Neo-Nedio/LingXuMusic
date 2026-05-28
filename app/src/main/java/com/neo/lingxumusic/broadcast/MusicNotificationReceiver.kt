package com.neo.lingxumusic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neo.lingxumusic.core.LingxuApplication
import com.neo.lingxumusic.core.MusicPlayController

class MusicNotificationReceiver : BroadcastReceiver() {

    companion object {
        // 动态生成广播 Action（包名 + ".NOTIFICATION_ACTIONS"）
        val ACTION_MUSIC_NOTIFICATION: String = LingxuApplication.getAppContext().packageName + ".NOTIFICATION_ACTIONS"
        const val KEY_EXTRA = "action_extra"      // Intent 中存储动作的 key
        const val ACTION_PLAY = "action_play"     // 播放/暂停动作
        const val ACTION_NEXT = "action_next"     // 下一曲动作
        const val ACTION_PRE = "action_previous"  // 上一曲动作
    }

    //接受广播
    override fun onReceive(context: Context?, intent: Intent?) {
        // 从 Intent 中取出动作标识
        intent?.getStringExtra(KEY_EXTRA)?.run {
            when(this) {
                // 播放/暂停：根据当前状态切换
                ACTION_PLAY -> {
                    if (MusicPlayController.isPlaying()) {
                        MusicPlayController.pause()
                    }else {
                        MusicPlayController.resume()
                    }
                }
                //todo 循环播放时上下播放不应该有边缘，最后一首的下一首直接从0开始，第一首的上一首直接到末尾
                //todo 以上是循环播放的，还要根据播放选择的播放情况判断
                ACTION_NEXT -> {
                    // 下一曲：不能超过列表末尾
                    val newIndex = (MusicPlayController.songList.size - 1).coerceAtMost(MusicPlayController.curIndex + 1)
                    MusicPlayController.play(newIndex)
                }
                ACTION_PRE -> {
                    // 上一曲：不能小于 0
                    val newIndex = 0.coerceAtLeast(MusicPlayController.curIndex - 1)
                    MusicPlayController.play(newIndex)
                }
            }
        }
    }

}