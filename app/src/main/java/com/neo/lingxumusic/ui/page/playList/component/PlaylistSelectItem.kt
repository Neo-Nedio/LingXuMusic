package com.neo.lingxumusic.ui.page.playList.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

/*
┌─────────────────────────────────────────────────────────────┐
│ Row                                                         │
│ ┌────────┐ ┌────────────────────────────┐ ┌──────────────┐ │
│ │ Checkbox│ │ 封面 │ 歌单名称            │ │              │
│ │        │ │      │ 共xx首              │ │              │
│ └────────┘ └────────────────────────────┘ └──────────────┘ │
└─────────────────────────────────────────────────────────────┘*/
@Composable
fun PlaylistSelectItem(
    playlist: Playlist?,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.cdp)
            .clickable { onSelectClick() }
            .padding(horizontal = 32.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.cdp)
    ) {
        playlist?.let {
            // 歌单封面
            CommonNetworkImage(
                url = it.pic?.replaceSize(),
                modifier = Modifier
                    .size(100.cdp)
                    .clip(RoundedCornerShape(10.cdp)),
                placeholder = R.drawable.ic_default_place_holder
            )

            // 歌单信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                MarqueeText(
                    text = it.name.orEmpty(),
                    fontSize = 30.csp,
                    color = AppColorsProvider.current.firstText,
                )
                Text(
                    text = "共${it.count}首",
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                    modifier = Modifier.padding(top = 8.cdp)
                )
            }
        }

        // 右侧复选框
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = AppColorsProvider.current.primary,
                uncheckedColor = AppColorsProvider.current.firstIcon,
            ),
        )
    }
}
