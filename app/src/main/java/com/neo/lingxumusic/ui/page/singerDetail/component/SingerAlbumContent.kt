package com.neo.lingxumusic.ui.page.singerDetail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

fun LazyListScope.singerAlbumItems(
    albums: List<ArtistAlbum>,
    onAlbumClick: (ArtistAlbum) -> Unit = {}
) {
    items(count = albums.size, key = { albums[it].album_id }) { index ->
        AlbumItem(
            album = albums[index],
            onClick = { onAlbumClick(albums[index]) }
        )
    }
}

@Composable
private fun AlbumItem(
    album: ArtistAlbum,
    onClick: () -> Unit
) {
    val colors = AppColorsProvider.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.cdp, vertical = 10.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧专辑封面
        CommonNetworkImage(
            url = album.sizable_cover?.replaceSize(),
            modifier = Modifier
                .size(120.cdp)
                .clip(RoundedCornerShape(12.cdp)),
            placeholder = com.neo.lingxumusic.R.drawable.ic_default_placeholder_video,
            error = com.neo.lingxumusic.R.drawable.ic_default_placeholder_video
        )

        // 封面与文字之间的间隔
        Spacer(modifier = Modifier.width(20.cdp))

        // 右侧文字
        Column(modifier = Modifier.weight(1f)) {
            // 专辑名
            Text(
                text = album.album_name.orEmpty(),
                color = colors.firstText,
                fontSize = 28.csp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.cdp))

            // 发布时间 · 曲目数
            val info = buildString {
                if (!album.publish_date.isNullOrBlank()) {
                    append(album.publish_date)
                }
                if (album.sum_ownercount > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("${album.sum_ownercount}首")
                }
            }
            if (info.isNotEmpty()) {
                Text(
                    text = info,
                    color = colors.thirdText,
                    fontSize = 22.csp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
