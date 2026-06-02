package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.ui.page.playMusic.component.PlayingMark

@Composable
fun SongItem(
    index: Int,
    song: Song,
    onClick: (index: Int) -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick.invoke(index)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.cdp)  // 子元素之间间距
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

        //歌曲信息
        val (singer, songName) = StringUtil.parseSongName(song.name ?: "")
        Column(
            modifier = Modifier
                .padding(vertical = 26.cdp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            MarqueeText(
                text = songName.ifEmpty { song.name ?: "已下架" },
                fontSize = 32.csp,
                color = AppColorsProvider.current.firstText,
            )
            Text(
                text = singer,
                fontSize = 24.csp,
                color = AppColorsProvider.current.secondText,
                modifier = Modifier.padding(top = 10.cdp)
            )
        }


        //右侧图标
        trailingIcon?.invoke() ?: CommonIcon(
            resId = R.drawable.ic_sheet_menu,
            modifier = Modifier.size(32.cdp)
                .clip(RoundedCornerShape(4.cdp))
        )

    }
}
