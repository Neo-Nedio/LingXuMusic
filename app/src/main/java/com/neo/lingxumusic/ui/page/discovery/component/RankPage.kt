package com.neo.lingxumusic.ui.page.discovery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.RankSong
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

@Composable
fun RankPage() {
}

//单个排行榜卡片
@Composable
fun RankInfoCard(
    rankInfo: RankInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 12.cdp)
            .clip(RoundedCornerShape(16.cdp))
            .background(AppColorsProvider.current.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.cdp, vertical = 20.cdp),
    ) {
        //上侧
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //标题
            Text(
                text = rankInfo.rankname.orEmpty(),
                color = AppColorsProvider.current.firstText,
                fontSize = 34.csp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.cdp),
            )
            //tag
            rankInfo.rankTagDesc()?.let { tagText ->
                Text(
                    text = tagText,
                    color = AppColorsProvider.current.primary,
                    fontSize = 22.csp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.cdp))
                        .background(AppColorsProvider.current.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 12.cdp, vertical = 6.cdp),
                )
            }
        }
        //下侧
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.cdp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //封面图
            RankBlurCover(
                coverUrl = rankInfo.songCover,
                playTimes = rankInfo.play_times,
                modifier = Modifier
                    .width(240.cdp)
                    .aspectRatio(1f),
            )
            //歌曲预览
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.cdp),
                verticalArrangement = Arrangement.spacedBy(16.cdp),
            ) {
                rankInfo.songinfo.orEmpty().take(3).forEachIndexed { index, song ->
                    RankPreviewSongRow(
                        index = index + 1,
                        song = song,
                    )
                }
            }
        }
    }
}

//模糊封面 + 播放量 + 播放图标
@Composable
fun RankBlurCover(
    coverUrl: String?,
    playTimes: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.cdp)),
    ) {
        //封面
        CommonNetworkImage(
            url = coverUrl?.replaceSize(),
            modifier = Modifier
                .fillMaxSize()
                .blur(6.cdp),
        )
        //左下角播放量
        Text(
            text = StringUtil.friendlyNumber(playTimes),
            color = Color.White,
            fontSize = 22.csp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.cdp)
                .clip(RoundedCornerShape(6.cdp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 8.cdp, vertical = 4.cdp),
        )
        //右下角播放图标
        CommonIcon(
            resId = R.drawable.ic_action_play,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.cdp)
                .size(40.cdp),
            tint = Color.White,
        )
    }
}

//歌曲预览
@Composable
private fun RankPreviewSongRow(
    index: Int,
    song: RankSong,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString(),
            color = AppColorsProvider.current.thirdText,
            fontSize = 28.csp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(36.cdp),
        )
        Text(
            text = song.displayText(),
            color = AppColorsProvider.current.firstText,
            fontSize = 26.csp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun RankInfo.rankTagDesc(): String? =
    extra?.resp?.rank_tag?.firstOrNull()?.desc?.takeIf { it.isNotBlank() }

private fun RankSong.displayText(): String {
    val (singer, songName) = StringUtil.parseSongName(songname.orEmpty())
    val displaySinger = author?.takeIf { it.isNotBlank() } ?: singer
    val displaySongName = songName.ifEmpty { songname.orEmpty() }
    return if (displaySinger.isNotEmpty()) {
        "$displaySongName - $displaySinger"
    } else {
        displaySongName
    }
}
