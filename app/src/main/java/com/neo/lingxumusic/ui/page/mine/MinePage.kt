package com.neo.lingxumusic.ui.page.mine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.ui.common.CommonHeadBackgroundShape
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.DragStatus
import com.neo.lingxumusic.ui.common.FixHeadBackgroundDraggableBodyLayout
import com.neo.lingxumusic.ui.common.rememberDragToggleState
import com.neo.lingxumusic.ui.page.mine.component.MusicApplicationComponent
import com.neo.lingxumusic.ui.page.mine.component.UserInfoComponent
import com.neo.lingxumusic.ui.page.mine.component.UserPlaylistItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.VibratorHelper
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.viewmodel.mine.MineViewModel
import kotlinx.coroutines.launch
import kotlin.times

//todo 歌单和歌单内的歌曲默认一次加载30，要监听滚动加入数据

// 主页面
@Composable
fun MinePage() {
    val viewModel: MineViewModel = hiltViewModel()
    // 背景图片的透明度（拖拽时逐渐变为 1）
    var bodyAlphaValue by remember { mutableStateOf(0f) }
    // 顶部栏的透明度（滚动时渐变，滚动的距离越大越不透明）
    val topBarAlphaValue = remember { mutableStateOf(0f) }
    // 滚动状态（监听 Column 的滚动距离）
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    // 粘性区域顶部位置 = 状态栏高度 + 100dp（用于判断 Tab 是否到达顶部）
    val stickyPositionTop = WindowInsets.statusBars.getTop(density) + 100.cdp.toPx

    //顶部栏透明度计算
    // 滚动距离 / 粘性区域顶部位置 = 透明度（0 ~ 1）
    var topBarAlpha = scrollState.value / stickyPositionTop
    if (topBarAlpha > 1) topBarAlpha = 1f  // 最大为 1（完全不透明）
    topBarAlphaValue.value = topBarAlpha

    // 滚动自动切换 Tab
    // 如果不是 Tab 点击触发的滚动（animateScrolling = false）
    if (!animateScrolling) {
        // 从后往前遍历 itemPositionMap（存储各模块的 Y 坐标）
        for (i in itemPositionMap.size - 1 downTo 0) {
            // 如果当前滚动位置 + 粘性区域顶部 > 模块的 Y 坐标
            if (scrollState.value + stickyPositionTop > itemPositionMap[i]!!) {
                // 自动切换到对应的 Tab
                viewModel.selectedTabIndex = i
                break  // 找到第一个就停止
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 数据加载区域（会切换 Loading / 内容 / 错误 页面）
        ViewStateComponent(
            viewStateLiveData = viewModel.userPlaylistResult,
            loadDataBlock = { viewModel.getUserPlayList() }) {

            Box {
                val dragToggleState = rememberDragToggleState(DragStatus.Idle)

                // 拖拽中时，禁止滚动联动（避免冲突）
                if (dragToggleState.isDragging) {
                    animateScrolling = false
                }

                FixHeadBackgroundDraggableBodyLayout(
                    state = dragToggleState,
                    triggerRadio = 0.24f, //触发阈值比例（0-1）
                    maxDragRadio = 0.48f, //最大拖拽距离比例（0-1）
                    modifier = Modifier.background(AppColorsProvider.current.background),
                    onOverOpenTriggerWhenDragging = {
                        dragToggleState.dragStatus = DragStatus.OverOpenTriggerWhenDragging
                        VibratorHelper.vibrate()
                    },
                    onOverOpenTriggerWhenFling = {
                        dragToggleState.dragStatus = DragStatus.OverOpenTriggerWhenFling
                    },
                    onOpened = { // 完全打开回调
                        dragToggleState.dragStatus = DragStatus.Opened
                        NavController.instance.navigate(Routes.PROFILE)  // 跳转个人主页
                        dragToggleState.dragStatus = DragStatus.Idle
                    },
                    headBackgroundComponent = { state, _ , maxDrag -> // 头部背景组件
                        if(state.offset >= 0) {
                            var alpha = state.offset / maxDrag
                            if (alpha > 1f) {
                                alpha = 1f
                            }
                            bodyAlphaValue = alpha
                        }
                        HeaderBackground(bodyAlphaValue)
                    }) {
                    Body(
                        1 - bodyAlphaValue,
                        scrollState
                    ) {
                        VibratorHelper.vibrate()
                        dragToggleState.dragStatus = DragStatus.OverOpenTriggerWhenFling
                    }
                }
            }
        }

        // 2. 顶部导航栏（覆盖在上面，始终显示）
        TopBar(topBarAlphaValue.value)
    }
}


private const val KEY_TAB_LAYOUT = -1 //Tab 栏自身的标识
private const val KEY_CREATE_PLAY_LIST = 0 //"创建歌单"模块的标识
private const val KEY_COLLECT_PLAY_LIST = 1 //"收藏歌单"模块的标识
private const val KEY_PLAY_LIST_HELP = 2 //"歌单助手"模块的标识

private var animateScrolling = false //是否正在执行 Tab 触发的自动滚动
private val itemPositionMap = HashMap<Int, Float>() //存储每个模块的 Y 坐标

@Composable
private fun Body(
    bodyAlphaValue: Float,
    scrollState: ScrollState,
    openUserPageCallback: () -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 粘性区域顶部位置 = 状态栏高度 + 100dp
    // 当原生 Tab 滚动到这个位置以上时，显示粘性 Tab
    val stickyPositionTop = WindowInsets.statusBars.getTop(density) + 100.cdp.toPx

    // 粘性区域底部位置 = 状态栏高度 + 188dp
    // 用于点击 Tab 时，计算滚动目标位置，让内容正好停在 Tab 栏下方，不被遮挡
    val stickyPositionBottom = WindowInsets.statusBars.getTop(density) + 188.cdp.toPx

    // 是否显示粘性 Tab（原生 Tab 滚出屏幕时为 true）
    var showStickyTabLayout by remember { mutableStateOf(false) }

    val viewModel: MineViewModel = hiltViewModel()

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // 绑定滚动状态
        ) {

            // 用户信息
            UserInfoComponent(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 88.cdp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },//记录交互状态
                        indication = null,//取消涟漪效果
                        onClick = openUserPageCallback
                    )
            )

            // 音乐应用
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = bodyAlphaValue }
                    .mineCommonCard()
                    .height(300.cdp),
                contentAlignment = Alignment.Center
            ) {
                MusicApplicationComponent()
            }

            // 喜欢的歌单
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = bodyAlphaValue }
                    .mineCommonCard(),
                contentAlignment = Alignment.Center
            ) {
                UserPlaylistItem(viewModel.favoritePlayList)
            }

            //  原生 Tab 栏（随内容滚动，会滚出屏幕）
            CommonTabLayout(
                tabTexts = tabs,
                backgroundColor = AppColorsProvider.current.card,
                style = CommonTabLayoutStyle(isScrollable = false,   // 不分页滑动，平分宽度
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.cdp)
                        .mineCommonCard()
                        // 监听 Tab 栏的位置变化
                        .onGloballyPositioned {
                            //记录 Tab 栏自身的 Y 坐标（用于滚动联动）
                            if (itemPositionMap[KEY_TAB_LAYOUT] == null && itemPositionMap[KEY_TAB_LAYOUT] == 0f) {
                                //boundsInParent().top 是 Tab 相对于父容器顶部的距离，记录一次就够了
                                itemPositionMap[KEY_TAB_LAYOUT] = it.boundsInParent().top
                            }
                            //  判断原生 Tab 是否滚出屏幕
                            // positionInRoot().y 是 Tab 相对于屏幕顶部的距离
                            // 当这个距离 <= stickyPositionTop 时，说明 Tab 已经滚到顶部附近
                            showStickyTabLayout = it.positionInRoot().y <= stickyPositionTop
                        }
                        .graphicsLayer { alpha = bodyAlphaValue },
                    //绘制tab之间的分割线
                    tabItemDrawBehindBlock = { position ->
                        // 不是最后一个 Tab 才绘制分割线
                        if (position != tabs.size - 1) {
                            drawLine(
                                Color.LightGray,                                        // 颜色：浅灰色
                                Offset(size.width, size.height * 0.3f),  // 起点：tab右上角（向下30%位置）
                                Offset(size.width, size.height * 0.7f),    // 终点：tab右下角（向下70%位置）
                                strokeWidth = 2.cdp.toPx()                              // 线宽：2dp
                            )
                        }
                    }
                ),
                selectedIndex = viewModel.selectedTabIndex
            ) {
                //点击回调
                viewModel.selectedTabIndex= it // 更新选中的 Tab
                // 滚动到目标位置
                itemPositionMap[it]?.let { position ->
                    animateScrolling = true
                    coroutineScope.launch {
                        // 减去 stickyPositionBottom 让内容停在 Tab 栏下方
                        scrollState.animateScrollTo((position - stickyPositionBottom).toInt(), tween(500))
                        animateScrolling = false
                    }
                }
            }

            // 创建歌单
            UserPlaylistComponent(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = bodyAlphaValue
                    }
                    //boundsInParent().top 是 Tab 相对于父容器顶部的距离，记录一次就够了
                    .onGloballyPositioned {
                        if (itemPositionMap[KEY_CREATE_PLAY_LIST] == null || itemPositionMap[KEY_CREATE_PLAY_LIST] == 0f) {
                            itemPositionMap[KEY_CREATE_PLAY_LIST] = it.boundsInParent().top
                        }
                    },
                list = viewModel.selfCreatePlayList,
                title = "创建歌单"
            )

            // 收藏歌单
            UserPlaylistComponent(
                modifier = Modifier
                    .graphicsLayer { alpha = bodyAlphaValue }
                    .onGloballyPositioned {
                        //boundsInParent().top 是 Tab 相对于父容器顶部的距离，记录一次就够了
                        if (itemPositionMap[KEY_COLLECT_PLAY_LIST] == null || itemPositionMap[KEY_COLLECT_PLAY_LIST] == 0f) {
                            itemPositionMap[KEY_COLLECT_PLAY_LIST] = it.boundsInParent().top
                        }
                    },
                list = viewModel.collectPlayList,
                title = "收藏歌单"
            )

            // 歌单助手
            Box(
                modifier = Modifier
                    .padding(bottom = 30.cdp)
                    .mineCommonCard()
                    .height(1000.cdp)
                    .onGloballyPositioned {
                        //boundsInParent().top 是 Tab 相对于父容器顶部的距离，记录一次就够了
                        if (itemPositionMap[KEY_PLAY_LIST_HELP] == null || itemPositionMap[KEY_PLAY_LIST_HELP] == 0f) {
                            itemPositionMap[KEY_PLAY_LIST_HELP] = it.boundsInParent().top
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("歌单助手",color = AppColorsProvider.current.firstText)
            }
        }

        //粘性 Tab 栏 ，在column外面，位于上层
        // 当原生 Tab 滚出屏幕后，这个 Tab 会固定在顶部显示
        if (showStickyTabLayout) {
            CommonTabLayout(
                tabTexts = tabs,
                backgroundColor = AppColorsProvider.current.pure,
                style = CommonTabLayoutStyle(
                    isScrollable = false,
                    modifier = Modifier
                        .statusBarsPadding()           // 避开状态栏
                        .padding(top = 88.cdp)         // 避开顶部导航栏
                        .fillMaxWidth()
                        .height(100.cdp)
                        .background(AppColorsProvider.current.pure)
                        .padding(top = 12.cdp),
                    //绘制tab之间的分割线
                    tabItemDrawBehindBlock = { position ->
                        if (position != tabs.size - 1) {
                            drawLine(
                                Color.LightGray,
                                Offset(size.width, size.height * 0.3f),
                                Offset(size.width, size.height * 0.7f),
                                strokeWidth = 2.cdp.toPx()
                            )
                        }
                    }
                ),
                selectedIndex = viewModel.selectedTabIndex
            ) {
                // 点击逻辑与原生 Tab 完全相同
                viewModel.selectedTabIndex = it
                itemPositionMap[it]?.let { position ->
                    animateScrolling = true
                    coroutineScope.launch {
                        scrollState.animateScrollTo((position - stickyPositionBottom).toInt(), tween(500))
                        animateScrolling = false
                    }
                }
            }
        }
    }
}

// 歌单列表组件
@Composable
private fun UserPlaylistComponent(
    modifier: Modifier = Modifier,
    list: List<Playlist>?,
    title: String
) {
    list?.let {
        Box(
            modifier = modifier.mineCommonCard()
        ) {
            Column {
                // 标题行
                Text(
                    text = "${title}(${list.size}个)",
                    color = AppColorsProvider.current.secondText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp, top = 20.cdp, start = 32.cdp)
                )
                // 歌单列表
                it.forEach {
                    UserPlaylistItem(it)
                }
            }
        }
    }
}

//顶部区域
@Composable
private fun TopBar(alphaValue: Float) {
    //主导航栏  始终显示，背景透明度随滚动变化
    CommonTopAppBar(
        modifier = Modifier
            .background(AppColorsProvider.current.pure.copy(alpha = alphaValue))
            .statusBarsPadding(),
        backgroundColor = Color.Transparent,
        leftIconResId = R.drawable.ic_drawer_toggle,
        leftClick = { },
        rightIconResId = R.drawable.ic_search
    )

    //动画区域（头像 + 昵称）  只在滚动到顶部时显示（alphaValue == 1f）
    AnimatedVisibility(
        // 避开状态栏
        modifier = Modifier.statusBarsPadding(),
        // 控制显示/隐藏的状态
        visibleState = remember { MutableTransitionState(false) }
            .apply {
                // 当 alphaValue == 1f（完全滚动到顶部）时，目标状态变为 true
                targetState = alphaValue == 1f },
        // 入场动画：淡入 + 从上往下滑入
        enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }),
        /*// 出场动画：淡出 + 从下往上滑出
        exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight })*/
        exit = ExitTransition.None
    ) {
        // 头像和昵称行（水平居中）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.cdp), // 高度与导航栏一致
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CommonNetworkImage(
                url = AppGlobalData.sLoginData?.pic,
                placeholder = R.drawable.ic_default_avator,
                error = R.drawable.ic_default_avator,
                modifier = Modifier
                    .size(50.cdp)
                    .clip(
                        RoundedCornerShape(50)
                    )
            )
            Text(
                text = AppGlobalData.sLoginData?.nickname.orEmpty(),
                fontSize = 32.csp,
                fontWeight = FontWeight.Medium,
                color = AppColorsProvider.current.firstText,
                modifier = Modifier.padding(start = 20.cdp)
            )
        }
    }
}

//个人主页的头部背景图片组件，支持透明度渐变效果，可拖拽
@Composable
private fun HeaderBackground(alphaValue: Float) {
    Image(
        painter = painterResource(id = R.drawable.ic_bg), // 背景图片资源
        contentDescription = null,
        contentScale = ContentScale.FillBounds, // 拉伸填充整个区域
        modifier = Modifier
            .fillMaxWidth()          // 宽度占满父容器
            .height(280.dp)          // 固定高度 280dp
            .clip(CommonHeadBackgroundShape())   // 底部弧形裁剪
            .graphicsLayer {
                alpha = alphaValue  // 透明度
            }
    )
}


// 卡片样式扩展函数
fun Modifier.mineCommonCard() = composed {
    this.fillMaxWidth()         // 宽度占满
        .padding(start = 32.cdp, end = 32.cdp, top = 20.cdp) // 1. 先 padding（上下左右都有间距）
        .background(AppColorsProvider.current.card, RoundedCornerShape(24.cdp)) // 2. 背景（只覆盖 padding 后的区域）
        .padding(top = 24.cdp, bottom = 24.cdp)              // 3. 再 padding（增加上下内边距）
}


private val tabs = listOf("创建歌单", "收藏歌单", "歌单助手")
