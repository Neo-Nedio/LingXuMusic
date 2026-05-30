package com.neo.lingxumusic.ui.page.playMusic.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.player.PlayMode
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.ui.page.playMusic.showPlayListSheet
import com.neo.lingxumusic.utils.replaceSize

@Composable
fun CurrentPlayList() {
    val lazyListState = rememberLazyListState()

    // 播放列表展示时，自动滚动到当前播放歌曲
    LaunchedEffect(showPlayListSheet) {
        if (showPlayListSheet) {
            lazyListState.animateScrollToItem(MusicPlayController.curIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth() // 宽度填满父容器
            .heightIn(0.cdp, 1080.cdp) // 高度限制，防止过高
            .clip(RoundedCornerShape(topStart = 40.cdp, topEnd = 40.cdp)) // 顶部圆角
            .background(AppColorsProvider.current.background)
            .padding(top = 48.cdp) // 顶部内边距
    ) {
        PlayListHeader()

        //歌曲列表 LazyColumn
        LazyColumn(
            modifier = Modifier
                .padding(top = 32.cdp) // 顶部内边距
                .fillMaxWidth(), // 宽度填满父容器
            state = lazyListState
        ) {
            itemsIndexed(MusicPlayController.songList) { index, song ->
                PlayListItem(index, song)
            }
        }
    }
}

//标题
@Composable
private fun PlayListHeader() {
    Row(
        modifier = Modifier
            .padding(start = 48.cdp, end = 32.cdp)  // 左右边距
            .fillMaxWidth(),                         // 填满宽度
        verticalAlignment = Alignment.CenterVertically,  // 垂直居中
        horizontalArrangement = Arrangement.SpaceBetween // 左右两端对齐
    ) {
        // 左侧内容（标题 + 数量）
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "当前播放",
                fontSize = 36.csp,
                fontWeight = FontWeight.Bold,
                color = AppColorsProvider.current.firstText
            )
            Text(
                text = "(${MusicPlayController.songList.size})",
                fontSize = 28.csp,
                fontWeight = FontWeight.Bold,
                color = AppColorsProvider.current.secondText
            )
        }

        // 右侧内容
        Row(
            modifier = Modifier
                .clickable {  // 点击切换模式
                when(MusicPlayController.playMode) {
                    PlayMode.RANDOM -> MusicPlayController.changePlayMode(PlayMode.SINGLE)
                    PlayMode.SINGLE -> MusicPlayController.changePlayMode(PlayMode.LOOP)
                    PlayMode.LOOP -> MusicPlayController.changePlayMode(PlayMode.RANDOM)
                }
            }.padding(horizontal = 16.cdp, vertical = 8.cdp), // 内边距增大点击区域
            verticalAlignment = Alignment.CenterVertically
        ) {
            val playModeText = when (MusicPlayController.playMode) {
                PlayMode.RANDOM -> "随机播放"
                PlayMode.SINGLE -> "单曲循环"
                PlayMode.LOOP -> "列表循环"
            }
            val playModeResId = when (MusicPlayController.playMode) {
                PlayMode.RANDOM -> R.drawable.ic_play_mode_random
                PlayMode.SINGLE -> R.drawable.ic_play_mode_single
                PlayMode.LOOP -> R.drawable.ic_play_mode_loop
            }
            Text( // 文字
                text = playModeText,
                fontSize = 32.csp,
                color = AppColorsProvider.current.firstText,
                modifier = Modifier.padding(end = 16.cdp)
            )
            CommonIcon( //图标
                resId = playModeResId,
                modifier = Modifier.size(36.cdp),
                tint = AppColorsProvider.current.firstText
            )
        }
    }
}

@Composable
private fun PlayListItem(index : Int, song: Song) {
    val (singer, songName) = StringUtil.parseSongName(song.name.orEmpty())
    Row(
        modifier = Modifier
            .fillMaxWidth() // 宽度填满
            .height(100.cdp) // 高度固定100
            .clickable {
                // 点击歌曲播放
                MusicPlayController.play(index)
            }
            .padding(horizontal = 48.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //左侧封面，富含动脉脉冲效果
        Box(
            modifier = Modifier
                .padding(end = 32.cdp)
                .size(80.cdp),
        ) {
            CommonNetworkImage(
                url = song.cover?.replaceSize(),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.cdp))
                    .graphicsLayer {
                        alpha = if (MusicPlayController.isPlaying(song)) 0.7f else 1f
                    },
            )
            if (MusicPlayController.isPlaying(song)) {
                PlayingMark(
                    playing = MusicPlayController.isPlaying(),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        // 富文本显示 "歌曲名 - 歌手"
        MarqueeText(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = if (!MusicPlayController.isPlaying(song))
                            AppColorsProvider.current.firstText
                        else
                            AppColorsProvider.current.primary,
                        fontSize = 32.csp
                    )
                ) {
                    append(songName.ifEmpty { song.name.orEmpty() })
                }
                withStyle(
                    style = SpanStyle(
                        color = if (!MusicPlayController.isPlaying(song))
                            AppColorsProvider.current.secondText
                        else
                            AppColorsProvider.current.secondary,
                        fontSize = 24.csp
                    )
                ) {
                    append(" - $singer")
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

