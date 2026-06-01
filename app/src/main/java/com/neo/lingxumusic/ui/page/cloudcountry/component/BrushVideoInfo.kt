package com.neo.lingxumusic.ui.page.cloudcountry.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.displayAuthor
import com.neo.lingxumusic.model.displayAuthorAvatar
import com.neo.lingxumusic.model.displayTitle
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

val cpnBottomPadding = 32.cdp
val seekBarTouchHeight = 80.cdp

//刷刷视频信息
@Composable
fun BoxScope.BrushVideoInfo(video: BrushVideo) {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.cdp)
            .padding(bottom = cpnBottomPadding + seekBarTouchHeight)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        //作者头像与名字
        Row(verticalAlignment = Alignment.CenterVertically) {
            CommonNetworkImage(
                url = video.displayAuthorAvatar,
                placeholder = R.drawable.ic_default_avator,
                error = R.drawable.ic_default_avator,
                modifier = Modifier
                    .size(55.cdp)
                    .clip(RoundedCornerShape(50))
            )
            Text(
                text = video.displayAuthor,
                fontSize = 32.csp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 16.cdp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        //视频标题
        Text(
            text = video.displayTitle,
            maxLines = 4,
            modifier = Modifier.padding(top = 24.cdp),
            fontSize = 28.csp,
            color = Color.White,
        )
    }
}
