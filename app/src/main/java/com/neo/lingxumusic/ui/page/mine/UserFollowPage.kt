package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.UserFollow
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.mine.UserFollowViewModel
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun UserFollowPage() {
    val viewModel: UserFollowViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
            .statusBarsPadding()
    ) {
        // 顶部导航栏
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.cdp),
            backgroundColor = AppColorsProvider.current.background,
            title = "关注",
            contentColor = AppColorsProvider.current.firstText,
        )

        // 搜索框占位
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.cdp, vertical = 16.cdp)
                .height(72.cdp)
                .clip(RoundedCornerShape(36.cdp))
                .background(AppColorsProvider.current.card),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "搜索",
                fontSize = 28.csp,
                color = AppColorsProvider.current.secondText
            )
        }

        // 关注列表
        ViewStateComponent(
            viewStateLiveData = viewModel.userFollowResult,
            loadDataBlock = { viewModel.getUserFollow() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.followList) { follow ->
                    UserFollowItem(follow)
                }
            }
        }
    }
}

@Composable
private fun UserFollowItem(follow: UserFollow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp)
            .height(100.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧头像
        CommonNetworkImage(
            url = follow.pic,
            placeholder = R.drawable.ic_default_avator,
            error = R.drawable.ic_default_avator,
            modifier = Modifier
                .size(80.cdp)
                .clip(RoundedCornerShape(50))
        )

        // 右侧名字
        Text(
            text = follow.nickname.orEmpty(),
            fontSize = 30.csp,
            fontWeight = FontWeight.Medium,
            color = AppColorsProvider.current.firstText,
            modifier = Modifier.padding(start = 24.cdp)
        )
    }
}
