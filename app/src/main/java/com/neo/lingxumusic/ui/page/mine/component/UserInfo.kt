package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

/*
┌─────────┐
│  头像   │  ← 圆形头像，悬空在卡片上方
└────┬────┘
┌────────┴────────┐
│                 │
│   ssk_evan      │  ← 白色卡片
│ 2关注｜2粉丝｜Lv.8│
│                 │
└─────────────────┘*/
@Composable
fun UserInfoComponent(modifier: Modifier = Modifier) {
    val userDetail = AppGlobalData.userDetail

    val follows = userDetail?.follows ?: 0
    val fans = userDetail?.fans ?: 0
    val visitors = userDetail?.visitors ?: 0

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .padding(top = 60.cdp, start = 32.cdp, end = 32.cdp)
                .fillMaxWidth()
                .height(240.cdp)
                .clip(RoundedCornerShape(24.cdp))
                .background(AppColorsProvider.current.card),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = userDetail?.nickname ?: AppGlobalData.sLoginData?.nickname ?: "无",
                fontSize = 40.csp,
                color = AppColorsProvider.current.firstText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 64.cdp)
            )
            Text(
                text = "$follows 关注  ｜  $fans 粉丝  ｜  $visitors 访客",
                fontSize = 32.csp,
                color = AppColorsProvider.current.secondText,
                modifier = Modifier.padding(top = 36.cdp)
            )
        }

        CommonNetworkImage(
            url = userDetail?.pic ?: AppGlobalData.sLoginData?.pic,
            placeholder = R.drawable.ic_default_avator,
            error = R.drawable.ic_default_avator,
            modifier =  Modifier.size(120.cdp)
                .clip(
                    RoundedCornerShape(50)
                )
        )
    }
}
