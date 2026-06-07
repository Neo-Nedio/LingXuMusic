package com.neo.lingxumusic.core.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neo.lingxumusic.ui.page.home.HomePage
import com.neo.lingxumusic.ui.page.login.LoginPage
import com.neo.lingxumusic.ui.page.mine.UserFollowPage
import com.neo.lingxumusic.ui.page.playList.PlaylistPage
import com.neo.lingxumusic.ui.page.profile.ProfilePage
import com.neo.lingxumusic.ui.page.splash.SplashPage
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.page.commemt.SongCommentPage
import com.neo.lingxumusic.ui.page.discovery.component.RankAudioPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider

object NavController {
    @SuppressLint("StaticFieldLeak")
    lateinit var instance: NavHostController
}


@Composable
fun LingXuNavGraph(
    drawerState: DrawerState,
    navController: NavHostController,
    startDestination: String = Routes.SPLASH,
    onFinish: () -> Unit = { }
) {
    NavController.instance = navController

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background),
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Routes.SPLASH) {
            SplashPage()
        }
        composable(Routes.LOGIN) {
            LoginPage()
        }
        composable(Routes.HOME) {
            HomePage(drawerState){ onFinish() }
        }
        composable(Routes.PROFILE,
            enterTransition = { EnterTransition.None }) { // 进入动画：无
            ProfilePage()
        }
        composable(Routes.PLAY_LIST) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<PlaylistBrief>(RoutesConstant.KEY_PLAY_LIST_BRIEF)
                ?.let { PlaylistPage(it) }
        }
        composable(Routes.RANK_AUDIO) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<RankInfo>(RoutesConstant.KEY_RANK_INFO)
                ?.let { RankAudioPage(it) }
        }
        composable(Routes.SONG_COMMENT) {
            val songBean = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Song>(RoutesConstant.SONG)
            SongCommentPage(songBean)
        }
        composable(Routes.USER_FOLLOW) {
            UserFollowPage()
        }

    }
}