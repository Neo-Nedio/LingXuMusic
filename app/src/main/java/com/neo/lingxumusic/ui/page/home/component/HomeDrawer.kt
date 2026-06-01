package com.neo.lingxumusic.ui.page.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import kotlinx.coroutines.launch

@Composable
fun HomeDrawer(drawerState: DrawerState) {
    // 整体布局：用户信息 + 设置区域
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background), // 背景色跟随主题
    ) {
        // 顶部：用户信息区域
        UserInfoComponent(drawerState)
        // 底部：设置区域（包含主题设置）
        SettingComponent()
    }
}

// 用户信息区域，点击可跳转个人主页
@Composable
private fun UserInfoComponent(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .statusBarsPadding()  // 适配状态栏高度
            .padding(top = 24.cdp)
            .fillMaxWidth()
            .height(100.cdp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 无涟漪效果
            ) {
                scope.launch {
                    drawerState.close()              // 先关闭抽屉
                    NavController.instance.navigate(Routes.PROFILE)  // 再跳转个人主页
                }
            }
            .padding(horizontal = 32.cdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧：用户头像
        CommonNetworkImage(
            url = AppGlobalData.sLoginData?.pic,
            placeholder = R.drawable.ic_default_avator,
            error = R.drawable.ic_default_avator,
            modifier = Modifier
                .size(60.cdp)
                .clip(
                    RoundedCornerShape(60)// 圆形裁剪
                )
        )
        // 中间：用户昵称
        Text(
            text = AppGlobalData.sLoginData?.nickname.orEmpty(),
            fontSize = 36.csp,
            color = AppColorsProvider.current.firstText,
            modifier = Modifier.padding(start = 20.cdp)
        )
        // 右侧：右箭头图标（表示可点击）
        CommonIcon(
            resId = R.drawable.ic_arrow_right,
            modifier = Modifier
                .padding(8.cdp)
                .size(30.cdp)
        )
    }
}

// 设置区域组件
@Composable
private fun SettingComponent() {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.cdp, vertical = 16.cdp)
            .background(AppColorsProvider.current.card, RoundedCornerShape(24.cdp))
    ) {
        // "设置"标题
        Text(
            text = "设置",
            modifier = Modifier.padding(horizontal = 32.cdp, vertical = 24.cdp),
            fontSize = 28.csp,
            color = AppColorsProvider.current.secondText
        )
        // 分割线
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.cdp,
            color = AppColorsProvider.current.divider
        )
        // 主题设置组件
        DrawerThemeSetting()
    }
}
