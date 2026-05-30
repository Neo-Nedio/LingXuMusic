package com.neo.lingxumusic.ui.page.commemt.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.neo.lingxumusic.R
import com.neo.lingxumusic.model.SongCommentItem
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.TimeUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.parseReply

//头像右侧的内边距
//用于让评论内容和回复按钮与头像对齐
private val AvatarEndPadding = 104.cdp

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

    val (replyText, userName, quotedText) = comment.content.orEmpty().parseReply()
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = commentTime,
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            //回复对象名字
            if (!userName.isNullOrBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = AppColorsProvider.current.secondText,
                                fontSize = 28.csp
                            )
                        ) {
                            append("回复")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = AppColorsProvider.current.firstText,
                                fontSize = 28.csp
                            )
                        ) {
                            append(userName)
                        }
                    },
                    modifier = Modifier.padding(end = 12.cdp)
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
            text = replyText,
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

        comment.getImages().firstOrNull()?.let { image ->
            CommonNetworkImage(
                url = image.url?.replaceSize(),
                modifier = Modifier
                    .padding(start = AvatarEndPadding)
                    .padding(bottom = if (!isFloorComment && replyCount > 0) 0.cdp else 24.cdp)
                    .fillMaxWidth()
                    .aspectRatio(
                        if (image.width > 0 && image.height > 0) {
                            image.width.toFloat() / image.height
                        } else {
                            1f
                        }
                    )
                    .clip(RoundedCornerShape(12.cdp))
            )
        }

        //回复入口（仅非楼层评论）
        if (!isFloorComment && replyCount > 0) {
            Text(
                text = "展开${replyCount}条回复 v",
                fontSize = 24.csp,
                color = Color.Blue,
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
