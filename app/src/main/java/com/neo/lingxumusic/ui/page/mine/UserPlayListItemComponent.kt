package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

/*
┌─────────────────────────────────────────────────┐
│ Row                                             │
│ ┌──────┐ ┌────────────────────────┐ ┌──────┐   │
│ │ 封面 │ │ 歌单名称               │ │  ⋮   │   │
│ │ 图片 │ │ 共xx首                │ │ 图标 │   │
│ └──────┘ └────────────────────────┘ └──────┘   │
└─────────────────────────────────────────────────┘*/
@Composable
fun UserPlaylistItem(platListItem: Playlist?) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { }             //todo  点击事件（目前为空）
            .padding(start = 32.cdp, end = 32.cdp, top = 8.cdp, bottom = 8.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        platListItem?.let {
            // 左侧：歌单封面图
            CommonNetworkImage(
                url = it.pic?.replaceSize(),
                modifier = Modifier
                    .padding(end = 20.cdp)
                    .size(110.cdp)
                    .clip(RoundedCornerShape(10.cdp)),
                placeholder = R.drawable.ic_default_place_holder
            )

            // 中间：歌单信息
            Column(
                modifier = Modifier
                    .weight(1f), // 占据剩余空间
                verticalArrangement = Arrangement.Center
            ) {
                // 歌单名称
                Text(
                    text = it.name.orEmpty(),
                    fontSize = 30.csp,
                    color = AppColorsProvider.current.firstText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 歌曲数量
                Text(
                    text = "共${it.count}首",
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 右侧：更多菜单图标
            Icon(
                painter = painterResource(id = R.drawable.ic_sheet_menu),
                contentDescription = "",
                modifier = Modifier
                    .height(30.cdp)
            )
        }
    }
}

// url定义替换函数
fun String.replaceSize(size: Int = 480): String {
    return replace("{size}", size.toString())
}