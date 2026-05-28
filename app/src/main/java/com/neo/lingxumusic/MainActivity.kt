package com.neo.lingxumusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.neo.lingxumusic.core.navigation.LingXuNavGraph
import com.neo.lingxumusic.utils.RequestNotificationPermission
import com.neo.lingxumusic.ui.page.mine.PlayListSheet
import com.neo.lingxumusic.ui.page.mine.PlayMusicPage
import com.neo.lingxumusic.ui.page.mine.component.BottomMusicPlay
import com.neo.lingxumusic.ui.theme.AppTheme
import com.neo.lingxumusic.ui.theme.themeTypeState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

      /*  transparentStatusBar()
        setAndroidNativeLightStatusBar()*/

        setContent {
            AppTheme(themeTypeState.value) {
                //申请权限，用于前台service
                RequestNotificationPermission()
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    LingXuNavGraph(navController) {
                        finish()
                    }
                    // 底部播放器组件
                    BottomMusicPlay()
                    // 音乐播放Sheet
                    PlayMusicPage()
                    // 播放列表Sheet
                    PlayListSheet()
                }
                }
            }
        }
}
