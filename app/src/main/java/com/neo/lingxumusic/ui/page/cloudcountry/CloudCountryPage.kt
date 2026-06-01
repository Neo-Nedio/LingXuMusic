package com.neo.lingxumusic.ui.page.cloudcountry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.ViewStateGridPagingComponent
import com.neo.lingxumusic.model.BrushVideo
import com.neo.lingxumusic.model.displayAuthor
import com.neo.lingxumusic.model.displayAuthorAvatar
import com.neo.lingxumusic.model.displayCover
import com.neo.lingxumusic.model.displayLikes
import com.neo.lingxumusic.model.displayTitle
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.cloudcountry.CloudCountryViewModel

@Composable
fun CloudCountryPage(onToggleDrawer: () -> Unit) {
    Column(
        Modifier
            .statusBarsPadding() // 适配状态栏高度
            .fillMaxSize()
    ) {
        // 顶部导航栏
        CommonTopAppBar(
            title = "云村",
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = onToggleDrawer,  // 点击打开侧边栏
        )

        val viewModel: CloudCountryViewModel = hiltViewModel()
        // 如果分页数据流未初始化，则初始化
        if (viewModel.brushVideoFlow == null) {
            viewModel.buildBrushVideoPager()
        }

        // 显示视频网格列表
        viewModel.brushVideoFlow?.let { flow ->
            val brushVideoItems = flow.collectAsLazyPagingItems()

            ViewStateGridPagingComponent(
                modifier = Modifier
                    .padding(horizontal = 24.cdp)
                    .weight(1f) // 占满剩余空间
                    .fillMaxWidth()
                    .background(AppColorsProvider.current.background),
                columns = 2, // 每行2列
                collectAsLazyPagingItems = brushVideoItems,
            ) {
                items(count = brushVideoItems.itemCount) { index ->
                    brushVideoItems[index]?.let { item ->
                        BrushVideoItem(item, viewModel)
                    }
                }
            }
        }
    }
}

//// 单个视频项卡片
@Composable
private fun BrushVideoItem(item: BrushVideo, viewModel: CloudCountryViewModel) {
    Column(
        modifier = Modifier
            .padding(10.cdp)
            .fillMaxWidth()
            .height(550.cdp)
            .clip(RoundedCornerShape(24.cdp)) // 圆角卡片
            .background(AppColorsProvider.current.card)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 无涟漪效果
            ) {
                viewModel.curPlayVideo = item // 点击设置当前播放视频
            }
    ) {
        // 视频封面图
        CommonNetworkImage(
            url = item.displayCover,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.cdp),
            placeholder = R.drawable.ic_default_placeholder_video,
            error = R.drawable.ic_default_placeholder_video
        )

        // 视频信息区域
        Column(
            modifier = Modifier
                .padding(horizontal = 20.cdp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 视频标题
            Text(
                text = item.displayTitle,
                color = AppColorsProvider.current.firstText,
                fontSize = 28.csp,
                maxLines = 2,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis
            )

            // 作者信息和点赞数行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.cdp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 作者头像
                CommonNetworkImage(
                    url = item.displayAuthorAvatar,
                    placeholder = R.drawable.ic_default_avator,
                    error = R.drawable.ic_default_avator,
                    modifier = Modifier
                        .size(40.cdp)
                        .clip(RoundedCornerShape(50))
                )
                // 作者名称
                Text(
                    text = item.displayAuthor,
                    fontSize = 24.csp,
                    color = AppColorsProvider.current.secondText,
                    modifier = Modifier
                        .padding(start = 10.cdp)
                        .weight(1f), // 占满剩余空间，将点赞数推到右边
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 点赞数行
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 点赞图标
                    CommonIcon(
                        resId = R.drawable.ic_fabulous,
                        modifier = Modifier.size(28.cdp),
                        tint = AppColorsProvider.current.secondIcon
                    )
                    // 点赞数（格式化：万、亿）
                    Text(
                        text = StringUtil.friendlyNumber(item.displayLikes),
                        fontSize = 24.csp,
                        color = AppColorsProvider.current.secondText,
                        modifier = Modifier.padding(start = 10.cdp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
