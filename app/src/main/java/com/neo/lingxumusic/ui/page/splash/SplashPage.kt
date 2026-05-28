package com.neo.lingxumusic.ui.page.splash

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.theme.AppColorsProvider

@Composable
fun SplashPage() {
    // 加载 Lottie 动画资源
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.splash)
    )

    // 无限循环旋转动画
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // 按钮颜色无限过渡动画
    val infiniteTransition = rememberInfiniteTransition()
    val buttonColor by infiniteTransition.animateColor(
        initialValue = AppColorsProvider.current.primary,
        targetValue = AppColorsProvider.current.secondary,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.primary)
    ) {
        // 背景 Lottie 动画
        composition?.let {
            LottieAnimation(
                composition = it,
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 前景内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Welcome to",
                fontSize = 24.sp,
                color = AppColorsProvider.current.thirdText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "聆序音乐",
                fontSize = 32.sp,
                color = AppColorsProvider.current.thirdText
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    NavController.instance.popBackStack()
                    NavController.instance.navigate(
                        //这个if有意义，ide判断有问题
                        if (AppGlobalData.sLoginData == null) Routes.LOGIN else Routes.HOME)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                Text("开始", fontSize = 18.sp)
            }
        }
    }

}