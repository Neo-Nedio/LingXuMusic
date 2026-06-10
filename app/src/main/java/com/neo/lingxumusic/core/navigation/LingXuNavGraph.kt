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
import com.neo.lingxumusic.ui.page.singerDetail.albumDetail.AlbumDetailPage
import com.neo.lingxumusic.ui.page.playList.PlaylistPage
import com.neo.lingxumusic.ui.page.playMusic.MvPlayPage
import com.neo.lingxumusic.ui.page.profile.ProfilePage
import com.neo.lingxumusic.ui.page.splash.SplashPage
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.RankInfo
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.ArtistAlbum
import com.neo.lingxumusic.ui.page.commemt.SongCommentPage
import com.neo.lingxumusic.ui.page.discovery.component.RankAudioPage
import com.neo.lingxumusic.ui.page.singerDetail.SingerDetailPage
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
        composable(
            Routes.MV_PLAY,
            arguments = listOf(
                navArgument("albumAudioId") { type = NavType.LongType },
                navArgument("songName") { type = NavType.StringType; nullable = true },
                navArgument("singerName") { type = NavType.StringType; nullable = true },
            )
        ) { backStackEntry ->
            val albumAudioId = backStackEntry.arguments?.getLong("albumAudioId") ?: 0
            val songName = backStackEntry.arguments?.getString("songName")
            val singerName = backStackEntry.arguments?.getString("singerName")
            MvPlayPage(albumAudioId, songName, singerName)
        }
        composable(
            Routes.SINGER_DETAIL,
            arguments = listOf(
                navArgument("singerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val singerId = backStackEntry.arguments?.getLong("singerId") ?: 0
            SingerDetailPage(singerId)
        }

        composable(Routes.ALBUM_DETAIL) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ArtistAlbum>(RoutesConstant.KEY_ALBUM)
                ?.let { AlbumDetailPage(it) }
        }

    }
}