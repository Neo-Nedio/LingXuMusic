package com.neo.lingxumusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.neo.lingxumusic.core.navigation.LingXuNavGraph
import com.neo.lingxumusic.ui.page.home.component.HomeDrawer
import com.neo.lingxumusic.ui.page.playMusic.PlayListSheet
import com.neo.lingxumusic.ui.page.playMusic.PlayMusicPage
import com.neo.lingxumusic.ui.page.playMusic.BottomMusicPlay
import com.neo.lingxumusic.ui.theme.AppColorsProvider
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
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = false,
                    drawerContent = {
                        ModalDrawerSheet(
                            //占用90%屏幕
                            modifier = Modifier.fillMaxWidth(0.9f),
                            drawerContainerColor = AppColorsProvider.current.background,
                        ) {
                            HomeDrawer(drawerState)
                        }
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColorsProvider.current.background)
                            .navigationBarsPadding()
                    ) {
                        LingXuNavGraph(drawerState, navController) {
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
}
