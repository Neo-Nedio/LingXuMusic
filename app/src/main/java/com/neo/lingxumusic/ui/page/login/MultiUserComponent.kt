package com.neo.lingxumusic.ui.page.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.neo.lingxumusic.model.MultiUser
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun MultiUserColumn(
    users: List<MultiUser>,
    modifier: Modifier = Modifier,
    onUserClick: (MultiUser) -> Unit
) {
    Column(modifier = modifier) {
        users.forEach { user ->
            MultiUserItem(
                user = user,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.cdp)
                    .clickable { onUserClick(user) }
            )
        }
    }
}

@Composable
fun MultiUserItem(
    user: MultiUser,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFE5E5E5)
) {
    Row(
        modifier = modifier
            .border(
                width = 1.cdp,
                color = borderColor,
                shape = RoundedCornerShape(12.cdp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.cdp)
            )
            .padding(vertical = 12.cdp, horizontal = 16.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧头像
        CommonNetworkImage(
            url = user.pic,
            modifier = Modifier
                .size(50.cdp)
                .clip(CircleShape)
        )

        // 右侧名字
        Text(
            text = user.nickname ?: user.username ?: "未知用户",
            fontSize = 16.csp,
            modifier = Modifier
                .padding(start = 12.cdp)
                .weight(1f)
        )
    }
}