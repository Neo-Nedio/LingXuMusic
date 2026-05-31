package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.core.navigation.RoutesConstant
import com.neo.lingxumusic.core.player.PlayMode
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.SeekBar
import com.neo.lingxumusic.ui.page.playMusic.showPlayListSheet
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.playMusic.PlayMusicViewModel
import kotlinx.coroutines.launch

@Composable
fun PlayMusicActionLayout() {
    MiddleActionLayout()
    ProgressLayout()
    BottomActionLayout()
}

//音乐播放器中部的操作按钮栏
@Composable
private fun MiddleActionLayout() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    //页面变化时获取新的评论和歌词
    LaunchedEffect(MusicPlayController.curIndex) {
        viewModel.songCommentResult = null //先把原评论置为空，防止新评论没加载出来之前受原数据影响
        viewModel.getSongComment(MusicPlayController.songList[MusicPlayController.curIndex])
        //先把原歌词为空，防止新歌词没加载出来之前受原数据影响
        viewModel.lyricResult.value = null
        viewModel.lyricModelList.clear()
        viewModel.curLyricIndex = -1
        viewModel.curPlayPosition = 0
        viewModel.getLyric(MusicPlayController.songList[MusicPlayController.curIndex])
    }

    Row(
        modifier = Modifier
            .padding(start = 60.cdp, end = 60.cdp, bottom = 32.cdp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) { // 水平均匀分布
        MiddleActionIcon(R.drawable.ic_like_no, modifier = Modifier.padding(end = 60.cdp))      // 点赞（未点赞状态）
        MiddleActionIcon(R.drawable.ic_download, modifier = Modifier.padding(end = 60.cdp))     // 下载
        MiddleActionIcon(R.drawable.ic_action_sing, modifier = Modifier.padding(end = 60.cdp))  // K歌/唱歌
        Box(modifier = Modifier.width(138.cdp)) { //评论
            MiddleActionIcon(
                R.drawable.ic_comment_count,
            ) {
                //设置参数
                NavController.instance.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(
                        RoutesConstant.SONG,
                        MusicPlayController.songList[MusicPlayController.curIndex]
                    )
                MusicPlayController.showPlayMusicSheet = false
                NavController.instance.navigate(Routes.SONG_COMMENT)
            }
            //显示评论数量
            viewModel.songCommentResult?.let {
                val commentText = StringUtil.friendlyNumber(it.count)
                Text(
                    text = commentText,
                    color = Color.White,
                    fontSize = 18.csp,
                    modifier = Modifier
                        .padding(top = 10.cdp, start = 52.cdp)
                        .align(Alignment.TopStart)
                )
            }
        }
        MiddleActionIcon(R.drawable.ic_song_more)    // 更多

    }
}

@Composable
private fun MiddleActionIcon(resId: Int, modifier: Modifier = Modifier, clickable: () -> Unit = {}) {
    CommonIcon(
        resId,
        tint = Color.White,
        modifier = modifier
            .size(78.cdp) // 图标容器大小 78dp（包含内边距）
            .clip(CircleShape)
            .clickable {
                clickable.invoke()
            }
            .padding(16.cdp)  // 内边距 16dp
    )
}

@Composable
private fun ProgressLayout() {
    Row(
        modifier = Modifier
            .padding(start = 44.cdp, end = 44.cdp, bottom = 32.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //左侧已播放时间
        Text(
            text = MusicPlayController.curPositionStr,
            fontSize = 26.csp,
            color = Color.White,
            modifier = Modifier.width(110.cdp)
        )
        //进度条
        SeekBar(
            progress = MusicPlayController.progress,
            seeking = { MusicPlayController.seeking(it) },
            seekTo = { MusicPlayController.seekTo(it) },
            modifier = Modifier.weight(1f)
        )
        //右侧总时间
        Text(
            text = MusicPlayController.totalDuringStr,
            fontSize = 26.csp,
            color = Color.White,
            modifier = Modifier.width(110.cdp),
            textAlign = TextAlign.End
        )

    }

}

//底部按钮
@Composable
private fun BottomActionLayout() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .padding(start = 20.cdp, end = 20.cdp, bottom = 60.cdp)
            .fillMaxWidth()
            .height(120.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val playModeResId = when (MusicPlayController.playMode) {
            PlayMode.RANDOM -> R.drawable.ic_play_mode_random
            PlayMode.SINGLE -> R.drawable.ic_play_mode_single
            PlayMode.LOOP -> R.drawable.ic_play_mode_loop
        }
        // 播放模式（顺序/随机/单曲循环）
        ActionButton(playModeResId) {
            when (MusicPlayController.playMode) {
                PlayMode.RANDOM -> MusicPlayController.changePlayMode(PlayMode.SINGLE)
                PlayMode.SINGLE -> MusicPlayController.changePlayMode(PlayMode.LOOP)
                PlayMode.LOOP -> MusicPlayController.changePlayMode(PlayMode.RANDOM)
            }
        }
        // 播放上一曲
        ActionButton(R.drawable.ic_action_pre) {
            val newIndex = MusicPlayController.getPreIndex()
            coroutineScope.launch {
                viewModel.sheetDiskRotate.stop()               // 停止旋转
                viewModel.lastSheetDiskRotateAngleForSnap = 0f // 重置角度
                MusicPlayController.play(newIndex)
            }
        }
        // 播放or暂停
        ActionButton(
            if (MusicPlayController.isPlaying()) R.drawable.ic_action_pause else R.drawable.ic_action_play,
            size = 116
        ) {
            if (MusicPlayController.isPlaying()) {
                MusicPlayController.pause()
                coroutineScope.launch {
                    viewModel.sheetNeedleUp = true                      // 唱针抬起
                    viewModel.lastSheetDiskRotateAngleForSnap = viewModel.sheetDiskRotate.value  // 记录角度
                    viewModel.sheetDiskRotate.stop()                    // 停止旋转
                }
            } else {
                // 播放逻辑
                MusicPlayController.resume()
                //这里不需要控制唱片与唱针的状态，DiskPager 有监听
            }
        }
        // 播放下一曲
        ActionButton(R.drawable.ic_action_next) {
            val newIndex = MusicPlayController.getNextIndex()
            viewModel.sheetNeedleUp = true
            coroutineScope.launch {
                viewModel.lastSheetDiskRotateAngleForSnap = 0f
                MusicPlayController.play(newIndex)
            }
        }
        ActionButton(R.drawable.ic_play_list) {
            showPlayListSheet = true
        }
    }
}

@Composable
private fun ActionButton(
    resId: Int,                      // 图标资源 ID
    size: Int = 84,                  // 按钮大小（默认 40dp）
    enable: Boolean = true,          // 是否可用（默认可用）
    onClick: () -> Unit = {}         // 点击回调（默认为空）
) {
    CommonIcon(
        resId,
        tint = if (enable) Color.White else Color(0xFFBBBBBB),
        modifier = Modifier
            .size(size.cdp)
            .clip(CircleShape)
            .clickable(enabled = enable) {
                if (enable) {
                    onClick()
                }
            }
            .padding(16.cdp)
    )
}