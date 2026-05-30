package com.neo.lingxumusic.ui.page.mine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.neo.lingxumusic.ui.common.DragToggleState
import com.neo.lingxumusic.ui.common.FixHeadBackgroundDraggableBodyLayout
import com.neo.lingxumusic.ui.common.rememberDragToggleState
import com.neo.lingxumusic.ui.page.mine.component.MusicApplicationComponent
import com.neo.lingxumusic.ui.page.mine.component.SongPlayListHelper
import com.neo.lingxumusic.ui.page.mine.component.UserInfoComponent
import com.neo.lingxumusic.ui.page.mine.component.UserPlaylistItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.VibratorHelper
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.toPx
import com.neo.lingxumusic.utils.transformDp
import com.neo.lingxumusic.viewmodel.mine.MineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.CollapsingToolbarState
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

//todo 歌单和歌单内的歌曲默认一次加载30，要监听滚动加入数据

// 主页面
@Composable
fun MinePage() {
    val viewModel: MineViewModel = hiltViewModel()
    // 背景图片的透明度（拖拽时逐渐变为 1）
    var bodyAlphaValue by remember { mutableStateOf(0f) }
    // 顶部栏的透明度（滚动时渐变，滚动的距离越大越不透明）
    val topBarAlphaValue = remember { mutableStateOf(0f) }
    // LazyColumn 滚动状态
    val lazyListState = rememberLazyListState()
    //获取当前可见的第一项索引
    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex

    // 滚动自动切换 Tab
    //不是 Tab 点击触发的滚动（避免冲突） 数据已加载完成（索引已计算） 有可见项
    if (!animateScrolling && viewModel.songHelperIndex != 0 && lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
        when {
            //切换到「歌单助手」Tab（index = 2）
            //last().index == songHelperIndex：最后一项是歌单助手
            //last().offset == viewportSize.height - last().size：该项的底部与屏幕底部对齐（完全可见）
            lazyListState.layoutInfo.visibleItemsInfo.last().index == viewModel.songHelperIndex &&
                    lazyListState.layoutInfo.visibleItemsInfo.last().offset == lazyListState.layoutInfo.viewportSize.height - lazyListState.layoutInfo.visibleItemsInfo.last().size -> {
                viewModel.selectedTabIndex = 2
            }
            //切换到「收藏歌单」Tab（index = 1）
            (firstVisibleItemIndex == viewModel.collectPlayListHeaderIndex - 1 //第一项是创建歌单 footer
                    //滚动偏移量足够大
                    && lazyListState.firstVisibleItemScrollOffset >= lazyListState.layoutInfo.visibleItemsInfo[1].size - 100.cdp.toPx)
                    //第一项已经超过创建歌单 footer
                    || firstVisibleItemIndex > viewModel.collectPlayListHeaderIndex - 1 -> {
                viewModel.selectedTabIndex = 1
            }
            //切换到「创建歌单」Tab（index = 0）
            //永远为 true，但只会走到这里是因为前面的条件都不满足
            firstVisibleItemIndex >= viewModel.selfCreatePlayListHeaderIndex -> {
                viewModel.selectedTabIndex = 0
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
                    headBackgroundComponent = { state, _, maxDrag -> // 头部背景组件
                        if (state.offset >= 0) {
                            var alpha = state.offset / maxDrag
                            if (alpha > 1f) {
                                alpha = 1f
                            }
                            bodyAlphaValue = alpha
                        }
                        HeaderBackground(bodyAlphaValue)
                    }) {
                    Body(
                        topBarAlphaValue,           // 参数1：顶部栏透明度状态
                        lazyListState,              // 参数2：列表滚动状态
                        dragToggleState,            // 参数3：拖拽状态
                        1 - bodyAlphaValue          // 参数4：主体透明度（取反）
                    ) {
                        VibratorHelper.vibrate()    // 参数5：lambda 回调内容
                        dragToggleState.dragStatus = DragStatus.OverOpenTriggerWhenFling
                    }
                }
            }
        }

        // 2. 顶部导航栏（覆盖在上面，始终显示）
        TopBar(topBarAlphaValue.value)
    }
}

private var animateScrolling = false //是否正在执行 Tab 触发的自动滚动

@Composable
private fun Body(
    topBarAlphaValue: MutableState<Float>,
    lazyListState: LazyListState,
    dragToggleState: DragToggleState,
    bodyAlphaValue: Float,
    openUserPageCallback: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarsTopPx = WindowInsets.statusBars.getTop(density)
    // 粘性区域顶部位置 = 状态栏高度 + 100dp（用于判断 Tab 是否到达顶部）
    val stickyPositionTop = statusBarsTopPx + 100.cdp.toPx
    val toolbarMaxHeight = statusBarsTopPx.transformDp + 88.cdp + 300.cdp +  // 状态栏高度+标题栏高度+用户信息高度
            368.cdp +  // 音乐应用高度
            194.cdp // 喜欢的歌单高度
    val toolbarMaxHeightPx = toolbarMaxHeight.toPx

    val toolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
    //顶部栏透明度计算
    // 滚动距离 / 粘性区域顶部位置 = 透明度（0 ~ 1）
    var topBarAlpha = (1 - toolbarScaffoldState.toolbarState.progress) / (stickyPositionTop / toolbarMaxHeightPx)
    if (topBarAlpha > 1) topBarAlpha = 1f  // 最大为 1（完全不透明）
    topBarAlphaValue.value = topBarAlpha

    CollapsingToolbarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dragToggleState.offset > 0) Color.Transparent else AppColorsProvider.current.background),
        state = toolbarScaffoldState,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        toolbar = {
            ScrollHeader(bodyAlphaValue, toolbarMaxHeight, openUserPageCallback)
        }
    ) {
        PlayList(bodyAlphaValue, lazyListState, toolbarScaffoldState.toolbarState)
    }
}

@Composable
private fun CollapsingToolbarScope.ScrollHeader(
    bodyAlphaValue: Float,
    toolbarMaxHeight: Dp,
    openUserPageCallback: () -> Unit,
) {
    val viewModel: MineViewModel = hiltViewModel()

    Column(
        Modifier
            .fillMaxWidth()
            .height(toolbarMaxHeight)
            .parallax(1f)
            .verticalScroll(rememberScrollState())
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
            UserPlaylistItem(viewModel.favoritePlayList,0.cdp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WindowInsets.statusBars.getTop(LocalDensity.current).transformDp + 88.cdp)
    )
}

@Composable
private fun PlayList(
    bodyAlphaValue: Float,
    lazyListState: LazyListState,
    toolbarState: CollapsingToolbarState,
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: MineViewModel = hiltViewModel()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = bodyAlphaValue },
        state = lazyListState
    ) {
        // 原生 Tab 栏（stickyHeader 吸顶）
        stickyHeader {
            StickyTabLayout(lazyListState, coroutineScope, bodyAlphaValue, toolbarState)
        }

        // 创建歌单
        item {
            PlaylistHeader(title = "创建歌单(${viewModel.selfCreatePlayList?.size ?: 0}个)")
        }

        //显示前 N-1 个歌单
        items((viewModel.selfCreatePlayList?.size ?: 0).coerceAtLeast(1) - 1) {
            UserPlaylistItem(viewModel.selfCreatePlayList!![it])
        }

        // 创建歌单 footer
        if (!viewModel.selfCreatePlayList.isNullOrEmpty()) {
            item {
                PlaylistFooter(viewModel.selfCreatePlayList!!.last())
            }
        }

        // 收藏歌单 header
        item {
            PlaylistHeader(title = "收藏歌单(${viewModel.collectPlayList?.size ?: 0}个)")
        }

        //显示前 N-1 个歌单
        items((viewModel.collectPlayList?.size ?: 0).coerceAtLeast(1) - 1) {
            UserPlaylistItem(viewModel.collectPlayList!![it])
        }

        // 收藏歌单 footer
        if (!viewModel.collectPlayList.isNullOrEmpty()) {
            item {
                PlaylistFooter(viewModel.collectPlayList!!.last())
            }
        }

        // 歌单助手
        item {
            Box(
                modifier = Modifier
                    .padding(bottom = 30.cdp)
                    .mineCommonCard(),
                contentAlignment = Alignment.Center
            ) {
                SongPlayListHelper()
            }
        }
    }
}

@Composable
private fun PlaylistHeader(title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 32.cdp, end = 32.cdp, top = 20.cdp)
            .background(
                AppColorsProvider.current.card,
                RoundedCornerShape(topStart = 24.cdp, topEnd = 24.cdp)
            )
            .padding(top = 24.cdp)
    ) {
        // 标题行
        Text(
            text = title,
            color = AppColorsProvider.current.secondText,
            fontSize = 28.csp,
            modifier = Modifier.padding(bottom = 12.dp, top = 20.cdp, start = 32.cdp)
        )
    }
}

@Composable
private fun PlaylistFooter(playlist: Playlist) {
    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(bottomStart = 24.cdp, bottomEnd = 24.cdp))
    ) {
        UserPlaylistItem(playlist)
        Box(
            Modifier
                .padding(start = 32.cdp, end = 32.cdp)
                .fillMaxWidth()
                .height(24.cdp)
                .background(
                    AppColorsProvider.current.pure,
                    RoundedCornerShape(bottomStart = 24.cdp, bottomEnd = 24.cdp)
                )
        )
    }
}

@Composable
private fun StickyTabLayout(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
    bodyAlphaValue: Float,
    state: CollapsingToolbarState,
) {
    val viewModel: MineViewModel = hiltViewModel()

    Surface(color = Color.Transparent) {
        // 折叠进度 > 0 时使用背景色，否则使用卡片色
        val backgroundColor = if (state.progress > 0.01)
            AppColorsProvider.current.background else AppColorsProvider.current.pure

        CommonTabLayout(
            tabTexts = tabs,
            backgroundColor = backgroundColor,
            style = CommonTabLayoutStyle(
                isScrollable = false,           // 不分页滑动，三个 Tab 平分宽度
                indicatorPaddingBottom = 18.cdp, // 指示器底部内边距
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.cdp)
                    .background(backgroundColor)
                    .padding(top = 12.cdp)
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
            viewModel.selectedTabIndex = it // 更新选中的 Tab

            animateScrolling = true // 标记为 Tab 触发的滚动（禁用滚动自动切换）
            coroutineScope.launch {
                when (it) {
                    // 滚动到创建歌单区域
                    0 -> lazyListState.animateScrollToItem(viewModel.selfCreatePlayListHeaderIndex)
                    // 滚动到收藏歌单区域
                    1 -> lazyListState.animateScrollToItem(
                        viewModel.collectPlayListHeaderIndex,
                        -100.cdp.toPx.toInt() // 偏移量 -100dp，让内容不完全贴顶
                    )
                    else -> lazyListState.animateScrollToItem(viewModel.songHelperIndex) // 滚动到歌单助手
                }
                // 滚动完成，恢复自动切换
                animateScrolling = false
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
                targetState = alphaValue == 1f
            },
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
