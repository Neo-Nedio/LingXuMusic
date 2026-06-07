package com.neo.lingxumusic.ui.page.playList.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.UserPlaylistController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.core.navigation.RoutesConstant
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.toBrief
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.showToast

/*
┌─────────────────────────────────────────────────┐
│ Row                                             │
│ ┌──────┐ ┌────────────────────────┐ ┌──────┐   │
│ │ 封面 │ │ 歌单名称               │ │  ⋮   │   │
│ │ 图片 │ │ 共xx首                │ │ 图标 │   │
│ └──────┘ └────────────────────────┘ └──────┘   │
└─────────────────────────────────────────────────┘*/
@Composable
fun UserPlaylistItem(platListItem: Playlist?, horizontalPadding: Dp = 32.cdp) {
    var showDeleteOverlay by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
            .height(126.cdp)
    ) {
        // 原始内容
        Row(
            Modifier
                .fillMaxSize()
                .background(AppColorsProvider.current.card)
                .clickable {
                    if (!showDeleteOverlay) {
                        platListItem?.let {
                            NavController.instance.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(RoutesConstant.KEY_PLAY_LIST_BRIEF, it.toBrief())
                            NavController.instance.navigate(Routes.PLAY_LIST)
                        }
                    }
                }
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
                        .weight(1f),
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
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                // 右侧：更多菜单图标
                Icon(
                    painter = painterResource(id = R.drawable.ic_sheet_menu),
                    contentDescription = "",
                    modifier = Modifier
                        .height(30.cdp)
                        .clickable { showDeleteOverlay = true }
                )
            }
        }

        // 从底部滑入的删除操作栏（与 Item 同高，覆盖在上方）
        AnimatedVisibility(
            visible = showDeleteOverlay,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300),
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColorsProvider.current.card),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：取消按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { showDeleteOverlay = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "取消",
                        fontSize = 30.csp,
                        color = AppColorsProvider.current.firstText,
                    )
                }

                // 中间：竖线分隔
                HorizontalDivider(
                    modifier = Modifier
                        .width(1.cdp)
                        .fillMaxHeight(0.5f),
                    color = AppColorsProvider.current.divider
                )

                // 右侧：删除按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            showDeleteOverlay = false
                            showDeleteDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "删除",
                        fontSize = 30.csp,
                        color = Color.Red,
                    )
                }
            }
        }

        // 删除确认 Dialog
        DeleteConfirmDialog(
            visible = showDeleteDialog,
            playlistName = platListItem?.name.orEmpty(),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                platListItem?.listid?.let { listid ->
                    if (listid > 0) {
                        UserPlaylistController.removePlaylist(
                            listid = listid,
                            globalCollectionId = platListItem.global_collection_id
                        )
                        showToast("已删除")
                    }
                }
            }
        )
    }
}

// 删除确认 Dialog（原生 AlertDialog）
@Composable
private fun DeleteConfirmDialog(
    visible: Boolean,
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AppColorsProvider.current.card,
            titleContentColor = AppColorsProvider.current.firstText,
            textContentColor = AppColorsProvider.current.firstText,
            title = {
                Text(text = "删除歌单")
            },
            text = {
                Text(text = "确定要删除歌单「${playlistName}」吗？")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消", color = AppColorsProvider.current.firstText)
                }
            }
        )
    }
}
