package com.neo.lingxumusic.ui.page.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.page.mine.BgImageShapes
import com.neo.lingxumusic.ui.page.mine.UserInfoComponent
import com.neo.lingxumusic.ui.page.mine.mineCommonCard
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun ProfilePage() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
    ) {

        //先添加，位于底层
        Image(
            painter = painterResource(id = R.drawable.ic_bg), // 背景图片资源
            contentDescription = null,
            contentScale = ContentScale.FillBounds, // 拉伸填充整个区域
            modifier = Modifier
                .fillMaxWidth()
                .height(584.cdp)
                .clip(BgImageShapes()) // 底部弧形裁剪
        )


        //后添加，在上层
        Column {
            // 用户信息
            UserInfoComponent(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 360.cdp)
            )

            Box(modifier = Modifier
                .mineCommonCard()
                .height(400.cdp),
                 contentAlignment = Alignment.Center) {
                    Text(text = "个人详情页", fontSize = 50.csp, fontWeight = FontWeight.Bold)
            }
        }

    }

}