package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.SeekBar
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.mine.PlayListViewModel
import kotlinx.coroutines.launch
import kotlin.math.max


var showBottomMusicPlay by mutableStateOf(false) //是否显示底部播放组件
var showPlayMusicPage by mutableStateOf(false)      // 是否显示播放页
var sheetNeedleUp by mutableStateOf(true)           // 唱针是否抬起
val sheetDiskRotate by mutableStateOf(Animatable(0f)) // 唱片旋转角度
var lastSheetDiskRotateAngleForSnap = 0f            // 上次暂停时的角度

@Composable
fun PlayMusicPage() {
    // 动画显示/隐藏播放页
    AnimatedVisibility(
        visible = showPlayMusicPage,  // 根据这个变量控制显示
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },  // 从底部滑入
            animationSpec = tween(600)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // 向底部滑出
            animationSpec = tween(600)
        )
    ){
        PlayMusicSheet()
    }
}

@Composable
fun PlayMusicSheet() {
    //当播放页打开时，按返回键关闭播放页
    //防止按返回键直接退出 App 或返回上一个路由，而不是关闭播放页
    BackHandler(enabled = showPlayMusicPage) {
        showPlayMusicPage = false
        showBottomMusicPlay = true
    }
    PlayMusicContent()
}

@Composable
fun PlayMusicContent() {
    val pagerState = rememberPagerState(
        initialPage = MusicPlayController.curIndex,  // 从当前播放歌曲开始
        pageCount = { MusicPlayController.songList.size }  // 总页数 = 歌单数量
    )

    //根容器（渐变背景）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.5f),   // 顶部半透明黑
                        Color.DarkGray.copy(alpha = 0.5f), // 中间深灰
                        Color.Black.copy(alpha = 0.5f)     // 底部半透明黑
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
                //todo 自定义一个组件，当文本过长时显示走马灯效果，从右往左轮播，文本不超出时用text
                customTitleLayout = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        val name = curSong.name.orEmpty()
                        val (singer, songName) = StringUtil.parseSongName(name)
                        // 歌名
                        Text(
                            text = songName,
                            fontSize = 36.csp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        //歌手名
                        Text(
                            text = singer,
                            fontSize = 24.csp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                },
                //todo 图片太小，箭头显示不明显
                leftIconResId = R.drawable.ic_arrow_down,
                appBarHeight = 120.cdp,
                customRightLayout = { //右侧有占位组件，这样让文字水平居中
                    Spacer(modifier = Modifier.size(88.cdp))
                },
                leftClick = {
                    showPlayMusicPage = false
                    showBottomMusicPlay = true
                },
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
            )

            //唱片区域
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    DiskRoundBackground()  // 底层：半透明圆形背景
                    DiskPager(pagerState)  // 中层：唱片轮播
                    DiskNeedle()           // 顶层：唱针
                }

                //底部区域
                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    BottomActionLayout()
                    ProgressLayout()
                    BottomActionLayout(pagerState)
                }
            }
        }
    }
}

@Composable
private fun BlurBackground(song: Song) {
    // 高斯模糊背景
    CommonNetworkImage(
        url = song.cover?.replaceSize(),
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 0.5f }
    )
}

// 半透明圆形背景
@Composable
private fun DiskRoundBackground() {
    Box(
        modifier = Modifier
            .padding(top = 100.dp)
            .width(274.dp)
            .height(274.dp)
            .clip(CircleShape)
            .background(Color(0x55EEEEEE))
    )
}

//唱针
@Composable
private fun DiskNeedle() {
    // 唱针旋转角度动画
    val needleRotateAnim by animateFloatAsState(
        targetValue = if (sheetNeedleUp) -25f else 0f,  // 抬起 = -25°，落下 = 0°
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)  // 200ms 线性动画
    )
    Image(
        painter = painterResource(id = R.drawable.ic_play_neddle),
        contentDescription = "needle",
        modifier = Modifier
            .padding(start = 70.dp)
            .width(110.dp)
            .height(167.dp)
            .graphicsLayer(
                rotationZ = needleRotateAnim,                     // 旋转角度
                transformOrigin = TransformOrigin(0.164f, 0.109f) // 旋转中心点
            )
    )
}

//唱片轮播
@Composable
private fun DiskPager(pagerState: PagerState) {
    // 初始播放状态
    LaunchedEffect(Unit) {
        if (MusicPlayController.isPlaying()) { ///正在播放
            sheetNeedleUp = false                      // 唱针落下
            sheetDiskRotate.stop()                     // 停止当前动画
            lastSheetDiskRotateAngleForSnap = 0f       // 重置角度
            sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap)  // 设置初始角度
            sheetDiskRotate.animateTo(                 // 开始旋转动画
                targetValue = 360f + lastSheetDiskRotateAngleForSnap,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (MusicPlayController.curIndex != pagerState.settledPage) {
            MusicPlayController.play() // 开始播放新歌曲
            sheetNeedleUp = false // 唱针落下
            sheetDiskRotate.stop()
            lastSheetDiskRotateAngleForSnap = 0f
            sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap)
            MusicPlayController.curIndex = pagerState.settledPage // 更新当前索引
            sheetDiskRotate.animateTo(  // 开始旋转
                targetValue = 360f + lastSheetDiskRotateAngleForSnap,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    //滑动切换歌曲
    //分页器只包含DiskItem，所以只有滑动唱片附近的区域才能左右滑动
    HorizontalPager(
        modifier = Modifier
            .padding(top = 100.dp)
            .fillMaxWidth()
            .height(274.dp),
        state = pagerState,
    ) { position ->
        DiskItem(MusicPlayController.songList[position])
    }
}

@Composable
private fun DiskItem(song: Song) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            //手势检测详解
            .pointerInput(Unit) {
                awaitEachGesture { // 等待一个完整的手势序列（按下→移动→松开）
                    while (true) { //循环等待事件
                        // 等待下一个触摸事件
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        // 只处理单指触摸
                        if (event.changes.size == 1) {
                            val pointer = event.changes[0]
                            if (pointer.pressed) { //按压
                                sheetNeedleUp = true  // 抬起唱针
                            } else { //松开处理
                                if (MusicPlayController.isPlaying()) {
                                    sheetNeedleUp = false  // 落下唱针
                                }
                                break
                            }
                        }
                    }
                }
            }
            .graphicsLayer {
                rotationZ =
                        // 当前播放的歌曲 → 旋转
                    if (song.hash == MusicPlayController.songList[MusicPlayController.curIndex].hash)
                        sheetDiskRotate.value
                    else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        //背景圆环
        CommonLocalImage(
             R.drawable.ic_disc_background,
            modifier = Modifier
                .width(270.dp)
                .height(270.dp)
        )

        //旋转图片
        CommonNetworkImage(
            url = song.cover?.replaceSize(), modifier = Modifier
                .width(180.dp)
                .height(180.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = CircleShape
                )
        )
    }
}

//音乐播放器底部的操作按钮栏
@Composable
private fun BottomActionLayout() {
    Row(modifier = Modifier.padding(start = 44.cdp, end = 44.cdp, bottom = 32.cdp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround) { // 水平均匀分布
        MiddleActionIcon(R.drawable.ic_like_no)      // 点赞（未点赞状态）
        MiddleActionIcon(R.drawable.ic_download)     // 下载
        MiddleActionIcon(R.drawable.ic_action_sing)  // K歌/唱歌
        MiddleActionIcon(R.drawable.ic_comment_count) // 评论
        MiddleActionIcon(R.drawable.ic_song_more)    // 更多

    }
}

@Composable
private fun MiddleActionIcon(resId: Int, modifier: Modifier = Modifier, clickable: () -> Unit = {}) {
    CommonIcon(
        resId,
        tint = Color.White,
        modifier = modifier
            .size(78.cdp) // 图标容器大小 78dp（包含内边距）
            .clip(CircleShape)
            .clickable {
                clickable.invoke()
            }
            .padding(16.cdp)  // 内边距 16dp
    )
}

@Composable
private fun ProgressLayout() {
    val viewModel: PlayListViewModel = hiltViewModel()

    Row(
        modifier = Modifier
            .padding(start = 44.cdp, end = 44.cdp, bottom = 32.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //左侧已播放时间
        Text(text = "00:00", fontSize = 26.csp, color = Color.White)
        //进度条
        SeekBar(
            progress = 22,
            seeking = {
                //viewModel.seeking(it)
            },
            seekTo = {
                //viewModel.seekTo(it)
            },
            modifier = Modifier
                .padding(horizontal = 20.cdp)
                .weight(1f)
        )
        //右侧总时间
        Text(text = "05:00", fontSize = 26.csp, color = Color.White)
    }

}

//底部按钮
@Composable
private fun BottomActionLayout(pagerState: PagerState) {
    val coroutineScopeScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .padding(start = 20.cdp, end = 20.cdp, bottom = 60.cdp)
            .fillMaxWidth()
            .height(120.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 播放模式（顺序/随机/单曲循环）
        ActionButton(R.drawable.ic_play_serial)
        // 播放上一曲
        ActionButton(R.drawable.ic_action_pre, enable = MusicPlayController.curIndex != 0) {
            sheetNeedleUp = true // 唱针抬起
            val newIndex = max(0, MusicPlayController.curIndex - 1) //新位置
            coroutineScopeScope.launch {
                sheetDiskRotate.stop()               // 停止旋转
                lastSheetDiskRotateAngleForSnap = 0f // 重置角度
                pagerState.animateScrollToPage(newIndex)  // 滚动到上一页
            }
        }
        // 播放or暂停
        ActionButton(if (MusicPlayController.isPlaying()) R.drawable.ic_action_pause else R.drawable.ic_action_play, size = 116) {
            if (MusicPlayController.isPlaying()) {
                MusicPlayController.pause()
                coroutineScopeScope.launch {
                    sheetNeedleUp = true                      // 唱针抬起
                    lastSheetDiskRotateAngleForSnap = sheetDiskRotate.value  // 记录角度
                    sheetDiskRotate.stop()                    // 停止旋转
                }
            } else {
                // 播放逻辑
                MusicPlayController.play()
                coroutineScopeScope.launch {
                    sheetNeedleUp = false                     // 唱针落下
                    sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap)  // 恢复角度
                    sheetDiskRotate.animateTo(                // 开始旋转
                        targetValue = 360f + lastSheetDiskRotateAngleForSnap,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
            }
        }
        // 播放下一曲
        ActionButton(R.drawable.ic_action_next, enable = MusicPlayController.curIndex != MusicPlayController.songList.size - 1) {
            val newIndex = (MusicPlayController.songList.size - 1).coerceAtMost(MusicPlayController.curIndex + 1)
            sheetNeedleUp = true
            coroutineScopeScope.launch {
                sheetDiskRotate.stop()
                lastSheetDiskRotateAngleForSnap = 0f
                pagerState.animateScrollToPage(newIndex)
            }
        }
        ActionButton(R.drawable.ic_play_list)
    }
}


@Composable
private fun ActionButton(
    resId: Int,                      // 图标资源 ID
    size: Int = 84,                  // 按钮大小（默认 40dp）
    enable: Boolean = true,          // 是否可用（默认可用）
    onClick: () -> Unit = {}         // 点击回调（默认为空）
) {
    CommonIcon(
        resId,
        tint = if (enable) Color.White else Color.Gray,  // 可用=白色，禁用=灰色
        modifier = Modifier
            .size(size.cdp)
            .clip(CircleShape)
            .clickable(enabled = enable) {
                if (enable) {
                    onClick()
                }
            }
            .padding(16.cdp)
    )
}

