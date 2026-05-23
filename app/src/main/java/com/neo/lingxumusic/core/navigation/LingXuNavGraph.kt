package com.neo.lingxumusic.core.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neo.lingxumusic.ui.page.home.HomePage
import com.neo.lingxumusic.ui.page.login.LoginPage
import com.neo.lingxumusic.ui.page.splash.SplashPage
import com.neo.lingxumusic.utils.TwoBackFinish

object NavController {
    @SuppressLint("StaticFieldLeak")
    lateinit var instance: NavHostController
}


@Composable
fun LingXuNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH,
    onFinish: () -> Unit = { }
) {
    NavController.instance = navController

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.SPLASH) {
            SplashPage()
        }
        composable(Routes.LOGIN) {
            LoginPage()
        }
        composable(Routes.HOME) {
            HomePage()
            BackHandler {
                TwoBackFinish().execute(onFinish)
            }
        }
    }
}