package com.neo.lingxumusic.ui.page.playList.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.ScenePlaylist
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

@Composable
fun RecommendPlayListItem(
    playlist: ScenePlaylist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.cdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommonNetworkImage(
            url = (playlist.flexible_cover ?: playlist.imgurl)?.replaceSize(),
            modifier = Modifier
                .size(110.cdp)
                .clip(RoundedCornerShape(10.cdp)),
            placeholder = R.drawable.ic_default_place_holder,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.cdp),
            verticalArrangement = Arrangement.Center,
        ) {
            val tagText = playlist.displayTagText()
            if (tagText.isNotEmpty()) {
                MarqueeText(
                    text = tagText,
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                )
            }
            MarqueeText(
                text = playlist.specialname.orEmpty(),
                fontSize = 30.csp,
                color = AppColorsProvider.current.firstText,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private fun ScenePlaylist.displayTagText(): String {
    val result = linkedSetOf<String>()
    show?.takeIf { it.isNotBlank() }?.let { result.add(it) }
    tags?.forEach { tag ->
        tag.tag_name?.takeIf { it.isNotBlank() }?.let { result.add(it) }
    }
    abtags?.forEach { tag ->
        tag.tag_name?.takeIf { it.isNotBlank() }?.let { result.add(it) }
    }
    return result.joinToString(separator = "-")
}
