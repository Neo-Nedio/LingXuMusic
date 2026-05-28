package com.neo.lingxumusic.ui.page.mine.component

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.page.mine.showPlayMusicSheet
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.ui.page.mine.showPlayListSheet

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
            .background(Color.White) // 白色背景
            .navigationBarsPadding()
            .padding(top = 48.cdp) // 顶部内边距
    ) {
        //标题区域 Row
        Row(modifier = Modifier.padding(horizontal = 48.cdp), // 水平内边距
            verticalAlignment = Alignment.Bottom) // 文字底部对齐
         {
            Text(
                text = "当前播放",
                fontSize = 36.csp, fontWeight = FontWeight.Bold, color = AppColorsProvider.current.firstText
            )
            Text(
                text = "(${MusicPlayController.songList.size})",
                fontSize = 28.csp, fontWeight = FontWeight.Bold, color = AppColorsProvider.current.secondText
            )
        }

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

@Composable
private fun PlayListItem(index : Int, song: Song) {
    val (singer, songName) = StringUtil.parseSongName(song.name.orEmpty())
    Row(
        modifier = Modifier
            .fillMaxWidth() // 宽度填满
            .height(100.cdp) // 高度固定100
            .clickable {
                // 点击歌曲播放
                MusicPlayController.play(index, delegateByPageState = showPlayMusicSheet)
            }
            .padding(horizontal = 48.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 富文本显示 "歌曲名 - 歌手"
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = if (!MusicPlayController.isPlaying(index))
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
                        color = if (!MusicPlayController.isPlaying(index))
                            AppColorsProvider.current.secondText
                        else
                            AppColorsProvider.current.secondary,
                        fontSize = 24.csp
                    )
                ) {
                    append(" - $singer")
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

