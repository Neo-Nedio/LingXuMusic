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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.page.mine.showPlayMusicPage
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.replaceSize
import kotlin.math.min



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
            visibleState = remember { MutableTransitionState(true) }
                .apply { targetState = !showPlayMusicPage },  // 播放页打开时隐藏
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
            .height(50.dp)
            .clickable {
                showPlayMusicPage = true // 点击打开播放页
            }
    ) {
        // 顶部分割线（0.2dp 高，浅灰色）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.2.dp)
                .background(Color(0xAACCCCCC))
        )

        // 背景色
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .clip(CircleShape)
                .background(AppColorsProvider.current.background)
        )

        //内容行
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 唱片区域
            Box(
                modifier = Modifier
                    .size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                // 唱片背景
                Image(
                    painter = painterResource(id = R.drawable.ic_disc_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                // 旋转的封面图
                CommonNetworkImage(
                    url = currentSong.cover?.replaceSize(),
                    placeholder = R.drawable.ic_default_place_holder,
                    modifier = Modifier
                        .size(34.dp)
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
                        fontSize = 14.sp,
                        color = AppColorsProvider.current.firstText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = singer,
                        fontSize = 11.sp,
                        color = AppColorsProvider.current.secondText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 3. 播放控制区域
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (MusicPlayController.isPlaying()) {
                            MusicPlayController.pause()
                        } else {
                            MusicPlayController.play()
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
                        .size(14.dp)
                )
                // 进度圆环（硬编码 33%）
                CircleProgress(modifier = Modifier.size(28.dp), 33)
            }
        }
    }
}


//圆环进度条
@Composable
fun CircleProgress(modifier: Modifier = Modifier, progress: Int) {
    // 计算圆弧扫过的角度（0% → 0度，100% → 360度）
    val sweepAngle = progress / 100f * 360

    Canvas(modifier = modifier) {
        // 取宽高中的最小值，保证圆是正圆
        val canvasSize = min(size.width, size.height)

        // 1. 背景圆环（浅灰色，完整圆）
        drawCircle(
            color = Color.LightGray,
            radius = canvasSize / 2,
            style = Stroke(width = 4f)  // 空心圆，线宽 4f
        )

        // 2. 进度圆弧（深灰色）
        drawArc(
            color = Color.DarkGray,
            style = Stroke(width = 4f),
            startAngle = -90f,           // 从顶部开始（-90° = 12点钟方向）
            sweepAngle = sweepAngle,     // 扫过的角度
            useCenter = false            // 不连接圆心，保持圆弧
        )
    }
}
