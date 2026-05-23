package com.neo.lingxumusic.core.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

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
        startDestination = startDestination
    ) {
        /*composable(Routes.SPLASH) {
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
        }*/
    }
}