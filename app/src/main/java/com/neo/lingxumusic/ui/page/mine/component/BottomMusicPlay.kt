package com.neo.lingxumusic.ui.page.mine.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.common.CircleProgress
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.page.mine.showBottomMusicPlay
import com.neo.lingxumusic.ui.page.mine.showPlayMusicPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.replaceSize



val BottomMusicPlayPadding = 104.cdp

@Composable
fun BoxScope.BottomMusicPlay() {
    if (MusicPlayController.songList.isNotEmpty()) { // 有歌曲才显示
        //动态计算底部内边距（首页需要避开底部导航栏）
        val paddingBottom = animateDpAsState(
            targetValue = if (NavController.instance.currentBackStackEntryAsState().value?.destination?.route == Routes.HOME) {
                80.dp
            } else {
                0.dp
            }
        )
        // 动画显示/隐藏播放条
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()                     // 宽度占满
                .align(Alignment.BottomCenter)      // 定位在 Box 底部中央
                .padding(bottom = paddingBottom.value),  // 动态底部间距
            visibleState = remember { MutableTransitionState(false) }
                .apply { targetState = showBottomMusicPlay && // 播放页打开时隐藏
                        //在引导页时隐藏
                        NavController.instance.currentBackStackEntryAsState().value?.destination?.route != Routes.SPLASH &&
                        //在登录页时隐藏
                        NavController.instance.currentBackStackEntryAsState().value?.destination?.route != Routes.LOGIN
                       },
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },  // 从底部滑入
                animationSpec = tween(200)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight }, // 向底部滑出
                animationSpec = tween(200)
            )
        ) {
            BottomMusicPlayBar() // 实际播放条 UI
        }
    }
}

// 播放条 UI
@Composable
private fun BottomMusicPlayBar() {
    val currentSong = MusicPlayController.songList[MusicPlayController.curIndex]

    //控制唱片旋转角度
    val diskRotateAngle by remember {
        mutableStateOf(Animatable(0f))
    }

    //上次唱片旋转的位置
    var lastDiskRotateAngleForSnap by remember { mutableStateOf(0f) }

    //播放中	唱片持续旋转（8秒一圈），从停止点继续
    //暂停时	记录当前角度，停止旋转
    LaunchedEffect(MusicPlayController.isPlaying()) {
        // 播放：从上次停止的角度继续旋转
        if (MusicPlayController.isPlaying()) {
            diskRotateAngle.snapTo(lastDiskRotateAngleForSnap)
            diskRotateAngle.animateTo(
                targetValue = 360f + lastDiskRotateAngleForSnap,//转360°
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart // 每次旋转到 360° 后重新开始
                )
            )
        } else {
            // 暂停：记录当前角度，停止动画
            lastDiskRotateAngleForSnap = diskRotateAngle.value
            diskRotateAngle.stop()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.cdp)
            .clickable {
                showPlayMusicPage = true // 点击打开播放页
                showBottomMusicPlay = false
            }
    ) {

        // 背景色
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .clip(CircleShape)
                .background(AppColorsProvider.current.bottomMusicPlayBarBackground)
        )

        //内容行
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 42.cdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 唱片区域
            Box(
                modifier = Modifier
                    .size(92.cdp),
                contentAlignment = Alignment.Center
            ) {
                // 唱片背景
                CommonLocalImage(
                    R.drawable.ic_disc,
                    modifier = Modifier.fillMaxSize()
                )
                // 旋转的封面图
                CommonNetworkImage(
                    url = currentSong.cover?.replaceSize(),
                    placeholder = R.drawable.ic_defalut_disk_cover,
                    error = R.drawable.ic_defalut_disk_cover,
                    modifier = Modifier
                        .size(70.cdp)
                        .clip(CircleShape)
                        .rotate(diskRotateAngle.value) //旋转角度
                )
            }

            // 2. 歌曲名和作者名
            val name = currentSong.name.orEmpty()
            val (singer, songName) = StringUtil.parseSongName(name)
            Box(
                modifier = Modifier
                    .weight(1f)//占满剩余空间，讲其他内容挤在两侧
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = songName.ifEmpty {  "已下架" },
                        fontSize = 18.sp,
                        color = AppColorsProvider.current.firstText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = singer,
                        fontSize = 12.sp,
                        color = AppColorsProvider.current.secondText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 3. 播放控制区域
            Box(
                Modifier
                    .size(75.cdp)
                    .clip(CircleShape)
                    .clickable {
                        if (MusicPlayController.isPlaying()) {
                            MusicPlayController.pause()
                        } else {
                            MusicPlayController.resume()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // 播放/暂停图标
                Icon(
                    painterResource(if (!MusicPlayController.isPlaying()) R.drawable.ic_play_without_circle else R.drawable.ic_pause_without_circle),
                    null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(30.cdp)
                )
                // 进度圆环
                CircleProgress(modifier = Modifier.size(58.cdp), MusicPlayController.progress)
            }
        }
    }
}


