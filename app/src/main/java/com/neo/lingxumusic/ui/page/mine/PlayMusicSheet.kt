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
import androidx.lifecycle.LifecycleOwner
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.core.navigation.RoutesConstant
import com.neo.lingxumusic.core.player.PlayMode
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonLocalImage
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.LifeCycleObserverComponent
import com.neo.lingxumusic.ui.common.SeekBar
import com.neo.lingxumusic.utils.StringUtil
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.viewmodel.mine.PlayMusicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.compareTo
import kotlin.math.abs

private const val DISK_ROTATE_ANIM_CYCLE = 10000
var showBottomMusicPlay by mutableStateOf(false) //是否显示底部播放组件

var showPlayMusicSheetWithoutAnim  = false //是否评论
var showPlayMusicSheet by mutableStateOf(false)      // 是否显示播放页
var sheetNeedleUp by mutableStateOf(true)           // 唱针是否抬起
val sheetDiskRotate by mutableStateOf(Animatable(0f)) // 唱片旋转角度
var lastSheetDiskRotateAngleForSnap = 0f            // 上次暂停时的角度

@Composable
fun PlayMusicPage() {
    //根据标志决定动画时长
    //从播放页跳转到评论页：瞬间消失
    //从评论页返回播放页/其他时候：有动画
    val animationDuration = if (showPlayMusicSheetWithoutAnim) 0 else 600

    //动画结束后重置标志（防止影响下次）
    LaunchedEffect(showPlayMusicSheet) {
        if (showPlayMusicSheet) {
            delay(animationDuration.toLong())
            showPlayMusicSheetWithoutAnim = false //关闭评论(并不是这个变量控制页面)
        }
    }

    // 动画显示/隐藏播放页
    AnimatedVisibility(
        visible = showPlayMusicSheet,  // 根据这个变量控制显示
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },  // 从底部滑入
            animationSpec = tween(animationDuration)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // 向底部滑出
            animationSpec = tween(animationDuration)
        )
    ){
        PlayMusicSheet()
    }
}

@Composable
fun PlayMusicSheet() {
    val scope = rememberCoroutineScope()
    //当播放页打开时，按返回键关闭播放页
    //防止按返回键直接退出 App 或返回上一个路由，而不是关闭播放页
    BackHandler(enabled = showPlayMusicSheet) {
        scope.launch {
            lastSheetDiskRotateAngleForSnap = 0f
            sheetDiskRotate.snapTo(0f)
            sheetDiskRotate.stop()
            showPlayMusicSheet = false
            showBottomMusicPlay = true
        }
    }
    PlayMusicContent(scope)
}

@Composable
fun PlayMusicContent(scope: CoroutineScope) {
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
                    val name = curSong.name.orEmpty()
                    val (singer, songName) = StringUtil.parseSongName(name)
                    // 歌名 - 歌手名
                    Text(
                        text = "$songName - $singer",
                        fontSize = 24.csp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                },
                //todo 图标不够大，不明显
                leftIconResId = R.drawable.ic_arrow_down,
                appBarHeight = 120.cdp,
                customRightLayout = { //右侧有占位组件，这样让文字水平居中
                    Spacer(modifier = Modifier.size(88.cdp))
                },
                leftClick = {
                    scope.launch {
                        lastSheetDiskRotateAngleForSnap = 0f
                        sheetDiskRotate.snapTo(0f)
                        sheetDiskRotate.stop()
                        showPlayMusicSheet = false
                        showBottomMusicPlay = true
                    }
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
                    MiddleActionLayout()
                    ProgressLayout()
                    BottomActionLayout()
                }
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
private var onStopBefore = false //记录页面是否曾经停止过，用于恢复时判断是否需要重新启动动画

@Composable
private fun DiskPager(pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    //记录值，当歌曲索引curIndex变化时会引起pagerState变化，但是当pagerState变化时，会使用play逻辑
    //索引curIndex变化时内部已经处理了音乐播放逻辑，此时引起的pagerState动画会导致其内再次play造成冲突
    //因此在引curIndex变化时会引起pagerState变化时，suppressPagerSync为true，让pagerState监听不起作用，防止冲突
    var suppressPagerSync by remember { mutableStateOf(false) }

    //通过生命周期控制，当页面不存在时后台不要转动，节省资源
    LifeCycleObserverComponent(lifeCycleListener = object : ComposeLifeCycleListener {
        //页面恢复时重新启动
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if(onStopBefore) {
                onStopBefore = false
                coroutineScope.launch {
                    delay(300) // 等待页面完全可见
                    controlSheetNeedleAndDiskAnim()
                }
            }
        }

        //页面不可见时停止唱片旋转动画，防止浪费资源
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            onStopBefore = true
            coroutineScope.launch {
                lastSheetDiskRotateAngleForSnap = 0f // 重置角度
                sheetDiskRotate.snapTo(0f) // 立即设置到 0°
                sheetDiskRotate.stop()  // 停止旋转
            }
        }
    }) {
        //播放状态监听
        LaunchedEffect(MusicPlayController.isPlaying()) {
            controlSheetNeedleAndDiskAnim()
        }

        //外部切歌时同步 UI
        LaunchedEffect(MusicPlayController.curIndex) {
            if (MusicPlayController.curIndex != -1 &&
                MusicPlayController.curIndex != pagerState.currentPage) {

                // 1. 重置旋转角度
                lastSheetDiskRotateAngleForSnap = 0f
                sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap)

                // 2. 滚动 Pager 到目标页
                suppressPagerSync = true
                if (abs(MusicPlayController.curIndex - pagerState.currentPage) > 1) {
                    pagerState.scrollToPage(MusicPlayController.curIndex)  // 无动画跳转
                } else {
                    pagerState.animateScrollToPage(                         // 动画滚动
                        MusicPlayController.curIndex,
                        animationSpec = tween(400)
                    )
                }
                suppressPagerSync = false
            }
        }

        //用户滑动时同步数据
        LaunchedEffect(pagerState.settledPage) {
            if (!suppressPagerSync && MusicPlayController.curIndex != pagerState.settledPage) {
                lastSheetDiskRotateAngleForSnap = 0f
                sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap)
                MusicPlayController.play(pagerState.settledPage)
            }
        }

        //页面
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
}

//播放状态监听
private suspend fun controlSheetNeedleAndDiskAnim() {
    if (MusicPlayController.isPlaying()) {
        sheetNeedleUp = false  // 唱针落下
        sheetDiskRotate.stop()
        sheetDiskRotate.snapTo(lastSheetDiskRotateAngleForSnap) // 从上次角度开始
        sheetDiskRotate.animateTo(
            targetValue = 360f + lastSheetDiskRotateAngleForSnap,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = DISK_ROTATE_ANIM_CYCLE, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        sheetNeedleUp = true // 唱针抬起
    }
}

@Composable
private fun DiskItem(song: Song) {
    val scope = rememberCoroutineScope()
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
                                scope.launch {
                                    delay(400)
                                    if (MusicPlayController.isPlaying()) {
                                        sheetNeedleUp = false  // 落下唱针
                                    }
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
                    if (MusicPlayController.isPlaying(song))
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
            url = song.cover?.replaceSize(),
            modifier = Modifier
                .width(180.dp)
                .height(180.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = CircleShape
                ),
            placeholder = R.drawable.ic_default_disk_cover,
            error = R.drawable.ic_default_disk_cover,
        )
    }
}

//音乐播放器中部的操作按钮栏
@Composable
private fun MiddleActionLayout() {
    val viewModel: PlayMusicViewModel = hiltViewModel()
    //页面变化时获取新的评论
    LaunchedEffect(MusicPlayController.curIndex) {
        viewModel.songCommentResult = null //先把原评论置为空，防止新评论没加载出来之前受原数据影响
        viewModel.getSongComment(MusicPlayController.songList[MusicPlayController.curIndex])
    }

    Row(
        modifier = Modifier
            .padding(start = 44.cdp, end = 44.cdp, bottom = 32.cdp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) { // 水平均匀分布
        MiddleActionIcon(R.drawable.ic_like_no)      // 点赞（未点赞状态）
        MiddleActionIcon(R.drawable.ic_download)     // 下载
        MiddleActionIcon(R.drawable.ic_action_sing)  // K歌/唱歌
        Box(modifier = Modifier.width(100.cdp)) { //评论
            MiddleActionIcon(
                R.drawable.ic_comment_count,
                modifier = Modifier.align(if (viewModel.songCommentResult == null) Alignment.Center else Alignment.CenterStart)
            ) {
                //设置参数
                NavController.instance.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(
                        RoutesConstant.SONG,
                        MusicPlayController.songList[MusicPlayController.curIndex]
                    )
                NavController.instance.navigate(Routes.SONG_COMMENT)
                showPlayMusicSheetWithoutAnim = true //打开评论(并不是这个变量控制页面)
                showPlayMusicSheet = false //关闭播放页
            }
            //显示评论数量
            viewModel.songCommentResult?.let {
                val commentText = StringUtil.friendlyNumber(it.count)
                Text(
                    text = commentText,
                    color = Color.White,
                    fontSize = 18.csp,
                    modifier = Modifier
                        .padding(
                            top = 6.cdp,
                            start = if (commentText.length >= 4) 50.cdp else 60.cdp
                        )
                        .align(Alignment.TopStart)
                )
            }
        }
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
    Row(
        modifier = Modifier
            .padding(start = 44.cdp, end = 44.cdp, bottom = 32.cdp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //左侧已播放时间
        Text(text = MusicPlayController.curPositionStr, fontSize = 26.csp, color = Color.White)
        //进度条
        SeekBar(
            progress = MusicPlayController.progress,
            seeking = {
                MusicPlayController.seeking(it)
            },
            seekTo = {
                MusicPlayController.seekTo(it)
            },
            modifier = Modifier
                .padding(horizontal = 20.cdp)
                .weight(1f)
        )
        //右侧总时间
        Text(text = MusicPlayController.totalDuringStr, fontSize = 26.csp, color = Color.White)
    }

}

//底部按钮
@Composable
private fun BottomActionLayout() {
    val coroutineScopeScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .padding(start = 20.cdp, end = 20.cdp, bottom = 60.cdp)
            .fillMaxWidth()
            .height(120.cdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val playModeResId = when(MusicPlayController.playMode) {
            PlayMode.RANDOM -> R.drawable.ic_play_mode_random
            PlayMode.SINGLE -> R.drawable.ic_play_mode_single
            PlayMode.LOOP -> R.drawable.ic_play_mode_loop
        }
        // 播放模式（顺序/随机/单曲循环）
        ActionButton(playModeResId) {
            when (MusicPlayController.playMode) {
                PlayMode.RANDOM -> MusicPlayController.changePlayMode(PlayMode.SINGLE)
                PlayMode.SINGLE -> MusicPlayController.changePlayMode(PlayMode.LOOP)
                PlayMode.LOOP -> MusicPlayController.changePlayMode(PlayMode.RANDOM)
            }
        }
        // 播放上一曲
        ActionButton(R.drawable.ic_action_pre) {
            val newIndex = MusicPlayController.getPreIndex()
            coroutineScopeScope.launch {
                sheetDiskRotate.stop()               // 停止旋转
                lastSheetDiskRotateAngleForSnap = 0f // 重置角度
                MusicPlayController.play(newIndex)
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
                MusicPlayController.resume()
                //这里不需要控制唱片与唱针的状态，DiskPager 有监听
            }
        }
        // 播放下一曲
        ActionButton(R.drawable.ic_action_next) {
            val newIndex = MusicPlayController.getNextIndex()
            sheetNeedleUp = true
            coroutineScopeScope.launch {
                lastSheetDiskRotateAngleForSnap = 0f
                MusicPlayController.play(newIndex)
            }
        }
        ActionButton(R.drawable.ic_play_list){
            showPlayListSheet = true
        }
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
        tint = if (enable) Color.White else Color(0xFFBBBBBB),
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

