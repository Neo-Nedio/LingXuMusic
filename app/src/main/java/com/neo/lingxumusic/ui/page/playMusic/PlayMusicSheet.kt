package com.neo.lingxumusic.ui.page.playMusic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.page.playMusic.component.DiskPagers
import com.neo.lingxumusic.ui.page.playMusic.component.Lyric
import com.neo.lingxumusic.ui.page.playMusic.component.PlayMusicActionLayout
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.playMusic.PlayMusicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val DISK_ROTATE_ANIM_CYCLE = 10000


@Composable
fun PlayMusicPage() {
    val viewModel: PlayMusicViewModel = hiltViewModel()

    // 播放 Sheet 显示时恢复唱片旋转，隐藏时暂停并记录角度
    LaunchedEffect(MusicPlayController.showPlayMusicSheet) {
        if (MusicPlayController.showPlayMusicSheet && MusicPlayController.isPlaying()) {
            viewModel.sheetDiskRotate.snapTo(viewModel.lastSheetDiskRotateAngleForSnap)
            viewModel.sheetDiskRotate.animateTo(
                targetValue = 360f + viewModel.lastSheetDiskRotateAngleForSnap,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = DISK_ROTATE_ANIM_CYCLE, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            viewModel.lastSheetDiskRotateAngleForSnap = viewModel.sheetDiskRotate.value
            viewModel.sheetDiskRotate.stop()
        }
    }

    AnimatedVisibility(
        visible = MusicPlayController.showPlayMusicSheet,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },  // 从底部滑入
            animationSpec = tween(600)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // 向底部滑出
            animationSpec = tween(600)
        )
    ) {
        PlayMusicSheet()
    }
}

@Composable
fun PlayMusicSheet() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    //当播放页打开时，按返回键关闭播放页
    //防止按返回键直接退出 App 或返回上一个路由，而不是关闭播放页
    BackHandler(enabled = MusicPlayController.showPlayMusicSheet) {
        scope.launch {
            viewModel.lastSheetDiskRotateAngleForSnap = 0f
            viewModel.sheetDiskRotate.snapTo(0f)
            viewModel.sheetDiskRotate.stop()
            MusicPlayController.showPlayMusicSheet = false
            MusicPlayController.showBottomMusicPlay = true
        }
    }
    PlayMusicContent(scope)
}

@Composable
fun PlayMusicContent(scope: CoroutineScope) {
    val viewModel: PlayMusicViewModel = hiltViewModel()

    //根容器（渐变背景）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.DarkGray.copy(alpha = 0.8f),
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .clickable(enabled = false) {}, // 禁用点击，让子组件处理
        contentAlignment = Alignment.Center
    ) {
        val curSong = MusicPlayController.songList[MusicPlayController.curIndex]
        BlurBackground(curSong) // 使用歌曲封面作为模糊背景

        Column(modifier = Modifier.fillMaxSize()) {
            //顶部导航栏
            CommonTopAppBar(
                customTitleLayout = {
                    val name = curSong.name.orEmpty()
                    val (singer, songName) = StringUtil.parseSongName(name)
                    MarqueeText(
                        text = "$songName - $singer",
                        fontSize = 32
                            .csp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leftIconResId = R.drawable.ic_arrow_down,
                appBarHeight = 120.cdp,
                leftClick = {
                    scope.launch {
                        viewModel.lastSheetDiskRotateAngleForSnap = 0f
                        viewModel.sheetDiskRotate.snapTo(0f)
                        viewModel.sheetDiskRotate.stop()
                        MusicPlayController.showPlayMusicSheet = false
                        MusicPlayController.showBottomMusicPlay = true
                    }
                },
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
            )


            //唱片区域
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (viewModel.showLyric) {
                        DiskPagers()
                    } else {
                        Lyric()
                    }
                }

                //底部区域
                PlayMusicActionLayout()
            }
        }
    }
}

@Composable
private fun BlurBackground(song: Song) {
    //todo 使用BlurTransformation实现真正的高斯模糊效果
    // 高斯模糊背景
    CommonNetworkImage(
        url = song.cover?.replaceSize(),
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.5f }
    )
}

