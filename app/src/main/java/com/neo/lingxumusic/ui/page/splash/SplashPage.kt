package com.neo.lingxumusic.ui.page.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import kotlinx.coroutines.delay

@Composable
fun SplashPage() {
    LaunchedEffect(Unit) {
        delay(1000)
        NavController.instance.popBackStack()
        NavController.instance.navigate(
            //这个if有意义，ide判断有问题
            if (AppGlobalData.sLoginResult == null) Routes.LOGIN else Routes.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.primary),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier
                .padding(top = 485.cdp)
                .size(190.cdp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 480.cdp)
                .size(200.cdp)
                .clip(RoundedCornerShape(50)),
            tint = AppColorsProvider.current.primaryVariant
        )
    }

}