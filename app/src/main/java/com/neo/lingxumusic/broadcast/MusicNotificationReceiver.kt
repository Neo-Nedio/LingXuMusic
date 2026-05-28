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
                ACTION_NEXT -> {
                    // 下一曲
                    val newIndex =MusicPlayController.getNextIndex()
                    MusicPlayController.play(newIndex)
                }
                ACTION_PRE -> {
                    // 上一曲
                    val newIndex = MusicPlayController.getPreIndex()
                    MusicPlayController.play(newIndex)
                }
            }
        }
    }

}