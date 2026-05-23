package com.neo.lingxumusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.neo.lingxumusic.core.navigation.LingXuNavGraph
import com.neo.lingxumusic.ui.theme.AppTheme
import com.neo.lingxumusic.ui.theme.themeTypeState
import com.neo.lingxumusic.utils.setAndroidNativeLightStatusBar
import com.neo.lingxumusic.utils.transparentStatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        setAndroidNativeLightStatusBar()
        setContent {
            AppTheme(themeTypeState.value) {
                val navController = rememberNavController()
                LingXuNavGraph(navController) {
                    finish()
                }
            }
        }
    }
}
