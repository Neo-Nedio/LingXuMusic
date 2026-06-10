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
import androidx.paging.compose.LazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize

fun LazyListScope.singerAlbumItems(
    albumList: LazyPagingItems<ArtistAlbum>,
    onAlbumClick: (ArtistAlbum) -> Unit = {}
) {
    items(count = albumList.itemCount, key = { albumList[it]?.album_id ?: it }) { index ->
        val album = albumList[index] ?: return@items
        AlbumItem(
            album = album,
            onClick = { onAlbumClick(album) }
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
            placeholder = R.drawable.ic_default_place_holder,
            error = R.drawable.ic_default_place_holder
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

            // 发布时间
            if (!album.publish_date.isNullOrBlank()) {
                Text(
                    text = album.publish_date,
                    color = colors.thirdText,
                    fontSize = 22.csp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
