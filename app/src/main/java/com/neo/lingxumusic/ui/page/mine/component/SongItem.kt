package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.utils.replaceSize

@Composable
fun SongItem(index: Int, song: Song) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { //todo
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.cdp)  // 子元素之间间距
    ) {

        // 左侧：歌曲封面
        CommonNetworkImage(
            url = song.cover?.replaceSize(),
            modifier = Modifier
                .size(80.cdp)
                .clip(RoundedCornerShape(16.cdp))
        )

        //歌曲信息
        val (singer, songName) = parseSongName(song.name ?: "")
        Column(
            modifier = Modifier
                .padding(vertical = 26.cdp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = songName.ifEmpty { song.name ?: "已下架" },
                fontSize = 32.csp,
                color = AppColorsProvider.current.firstText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = singer,
                fontSize = 24.csp,
                color = AppColorsProvider.current.secondText,
                modifier = Modifier.padding(top = 10.cdp)
            )
        }


        //菜单图标
        CommonIcon(
            resId = R.drawable.ic_sheet_menu,
            modifier = Modifier.size(32.cdp)
                .clip(RoundedCornerShape(4.cdp))
        )

    }
}

/**
 * 解析歌曲名字符串，提取歌名和歌手名
 * 格式： "洛天依、乐正绫、言和 - 酆都冥司记"
 * 结果： 歌手 = "洛天依、乐正绫、言和"，歌名 = "酆都冥司记"
 */
fun parseSongName(fullName: String): Pair<String, String> {
    val separatorIndex = fullName.indexOf(" - ")
    return if (separatorIndex != -1) {
        val singer = fullName.substring(0, separatorIndex)
        val songName = fullName.substring(separatorIndex + 3)
        Pair(singer, songName)
    } else {
        // 如果没有分隔符，歌手设为空或 "未知"
        Pair("", fullName)
    }
}