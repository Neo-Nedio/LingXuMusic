package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.ui.page.mine.component.CommentItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.mine.FloorCommentSuccessResult
import com.neo.lingxumusic.viewmodel.mine.SongCommentViewModel

@Composable
fun FloorCommentSheet() {
    val viewModel: SongCommentViewModel = hiltViewModel()
    AnimatedVisibility(
        visible = viewModel.showFloorCommentSheet,  // 控制显示
        enter = slideInVertically(                  // 进入动画：从底部滑入
            initialOffsetY = { fullHeight -> fullHeight },  // 起始位置：屏幕底部下方
            animationSpec = tween(durationMillis = 300),
        ),
        exit = slideOutVertically(                  // 退出动画：滑出到底部
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300),
        ),
    ) {
        FloorCommentSheetContent()
    }
}

@Composable
private fun FloorCommentSheetContent() {
    val viewModel: SongCommentViewModel = hiltViewModel()

    // 返回键处理：弹窗打开时，按返回键关闭弹窗
    BackHandler(viewModel.showFloorCommentSheet) {
        viewModel.showFloorCommentSheet = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 遮罩层（半透明背景，点击关闭）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))  // 半透明黑色
                .clickable { viewModel.showFloorCommentSheet = false },
        )

        // 2. 内容层（底部面板）
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            FloorCommentList()
        }
    }
}

@Composable
private fun FloorCommentList() {
    val viewModel: SongCommentViewModel = hiltViewModel()
    val mixsongid = viewModel.song?.mixsongid ?: 0L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f) // 高度为屏幕的75%
            .clip(RoundedCornerShape(topStart = 40.cdp, topEnd = 40.cdp))  // 顶部圆角
            .background(AppColorsProvider.current.pure)
            .padding(top = 48.cdp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .padding(horizontal = 48.cdp)
                .fillMaxWidth()
                .height(80.cdp)
        ) {
            Text(
                text = "回复",
                color = AppColorsProvider.current.firstText,
                fontSize = 36.csp,
                fontWeight = FontWeight.Medium
            )
        }

        // 监听参数变化，重新加载数据
        LaunchedEffect(viewModel.floorOwnerCommentId, mixsongid, viewModel.floorOwnerSpecialChildId) {
            viewModel.getFloorCommentResult(
                commentId = viewModel.floorOwnerCommentId,
                mixsongid = mixsongid,
                specialChildId = viewModel.floorOwnerSpecialChildId
            )
        }

        // 状态处理组件（加载中/空数据/错误/成功）
        ViewStateComponent(
            viewStateLiveData = viewModel.floorCommentResult,
            specialRetryBlock = { // 自定义重试逻辑
                viewModel.getFloorCommentResult(
                    commentId = viewModel.floorOwnerCommentId,
                    mixsongid = mixsongid,
                    specialChildId = viewModel.floorOwnerSpecialChildId
                )
            }
        ) { result ->
            val data = result as? FloorCommentSuccessResult ?: return@ViewStateComponent
            LazyColumn {
                // 主评论（被回复的那条）
                data.ownerComment.let { ownerComment ->
                    item {
                        Column {
                            CommentItem(comment = ownerComment, isFloorComment = true)
                            Divider(
                                color = AppColorsProvider.current.divider.copy(0.6f),
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 20.cdp
                            )
                        }
                    }
                }

                // 楼中楼回复列表
                items(data.replies) {
                    CommentItem(comment = it, isFloorComment = true)
                }
            }
        }
    }
}
