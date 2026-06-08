package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.UserFollow
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.mine.UserFollowViewModel
import androidx.compose.foundation.layout.statusBarsPadding
import com.neo.lingxumusic.core.navigation.NavController

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

        // 搜索框区域
        SearchBar()

        // 关注列表
        ViewStateComponent(
            viewStateLiveData = viewModel.userFollowResult,
            loadDataBlock = { viewModel.getUserFollow() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.filteredFollowList) { follow ->
                    UserFollowItem(follow)
                }
            }
        }
    }
}

@Composable
private fun SearchBar() {
    val viewModel: UserFollowViewModel = hiltViewModel()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // 进入搜索模式时自动聚焦并弹出键盘
    LaunchedEffect(viewModel.isSearchMode) {
        if (viewModel.isSearchMode) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
            viewModel.searchKeyword = ""
        }
    }

    if (viewModel.isSearchMode) {
        // 搜索输入状态
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.cdp, vertical = 16.cdp)
                .height(72.cdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = viewModel.searchKeyword,
                onValueChange = { viewModel.searchKeyword = it },
                textStyle = TextStyle(
                    fontSize = 28.csp,
                    color = AppColorsProvider.current.firstText
                ),
                cursorBrush = SolidColor(AppColorsProvider.current.primary),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(36.cdp))
                    .background(AppColorsProvider.current.card)
                    .padding(horizontal = 32.cdp)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CommonIcon(
                            resId = R.drawable.ic_search,
                            tint = AppColorsProvider.current.secondText,
                            modifier = Modifier.size(36.cdp)
                        )
                        Spacer(modifier = Modifier.width(16.cdp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (viewModel.searchKeyword.isEmpty()) {
                                Text(
                                    text = "搜索",
                                    fontSize = 28.csp,
                                    color = AppColorsProvider.current.secondText
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            Text(
                text = "取消",
                fontSize = 28.csp,
                color = AppColorsProvider.current.primary,
                modifier = Modifier
                    .padding(start = 24.cdp)
                    .clickable {
                        viewModel.isSearchMode = false
                    }
            )
        }
    } else {
        // 搜索占位状态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.cdp, vertical = 16.cdp)
                .height(72.cdp)
                .clip(RoundedCornerShape(36.cdp))
                .background(AppColorsProvider.current.card)
                .clickable { viewModel.isSearchMode = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "搜索",
                fontSize = 28.csp,
                color = AppColorsProvider.current.secondText
            )
        }
    }
}

@Composable
private fun UserFollowItem(follow: UserFollow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.cdp, vertical = 16.cdp)
            .height(100.cdp)
            .clickable{
                NavController.instance.navigate("singerDetail/${follow.singerid}")
            },
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
