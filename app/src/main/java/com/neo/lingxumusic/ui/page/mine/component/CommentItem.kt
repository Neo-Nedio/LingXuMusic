package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.SongCommentItem
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.TimeUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

//头像右侧的内边距
//用于让评论内容和回复按钮与头像对齐
private val AvatarEndPadding = 104.cdp
/*
┌────────────────────────────────────────────────────┐
│ [头像]  用户名                          123  [❤]   │
│         时间                                       │
│                                                    │
│         这是一条评论内容，可以很长，会......         │
│                                                    │
│         3条回复 >                                  │
├────────────────────────────────────────────────────┤
└────────────────────────────────────────────────────┘*/

@Composable
fun CommentItem(
    comment: SongCommentItem,           // 评论数据
    isFloorComment: Boolean = false,    // 是否是楼层评论（子评论）
    onFloorCommentClick: ((comment: SongCommentItem) -> Unit)? = null  // 点击回复回调
) {
    val replyCount = comment.reply_num // 回复数量
    // 时间格式化：支持时间戳或直接字符串
    val commentTime = comment.addtime?.toLongOrNull()?.let { TimeUtil.parse(it) }
        ?: comment.addtime.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.cdp, end = 32.cdp, top = 24.cdp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //头像区域
            CommonNetworkImage(
                comment.user_pic,
                placeholder = R.drawable.ic_default_avator,
                error = R.drawable.ic_default_avator,
                modifier = Modifier
                    .padding(end = 24.cdp)  // 头像右边距24dp
                    .size(80.cdp)           // 头像尺寸80x80
                    .clip(CircleShape)      // 圆形裁剪
            )

            // 用户名和时间
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(80.cdp),
                verticalArrangement = Arrangement.SpaceBetween // 上下分布
            ) {
                Text(
                    text = comment.user_name.orEmpty(),
                    fontSize = 28.csp,
                    color = AppColorsProvider.current.firstText,
                )

                Text(
                    text = commentTime,
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                )
            }

            //点赞区域
            Text(
                text = (comment.like?.count ?: 0).toString(),
                fontSize = 28.csp,
                color = AppColorsProvider.current.thirdText,
                modifier = Modifier.padding(end = 12.cdp)
            )
            CommonIcon(
                resId = R.drawable.ic_like_no, // 未点赞图标
                tint = AppColorsProvider.current.thirdIcon,
                modifier = Modifier.size(32.cdp)
            )
        }

        //评论内容
        Text(
            text = comment.content.orEmpty(),
            fontSize = 32.csp,
            color = AppColorsProvider.current.firstText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AvatarEndPadding)  // 左对齐头像
                .padding(
                    top = 20.cdp,
                    // 楼层评论且有回复时，底部内边距为0（避免重复间距）
                    bottom = if (isFloorComment && replyCount > 0) 0.cdp else 24.cdp
                )
        )

        //回复入口（仅非楼层评论）
        if (!isFloorComment && replyCount > 0) {
            Text(
                text = "${replyCount}条回复 >",
                fontSize = 24.csp,
                color = AppColorsProvider.current.secondText,
                modifier = Modifier
                    .padding(start = AvatarEndPadding)
                    .padding(vertical = 14.cdp)
                    .clickable {
                        onFloorCommentClick?.invoke(comment)
                    }
                    .padding(10.cdp)
            )
        }

        //分割线
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AvatarEndPadding), // 分割线从头像右侧开始
            color = AppColorsProvider.current.divider,
            thickness = 1.5.cdp
        )
    }
}
