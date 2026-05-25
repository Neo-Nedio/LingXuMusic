package com.neo.lingxumusic.ui.page.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.api.UserApi
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.Playlist
import com.neo.lingxumusic.model.PlaylistData
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.common.DragStatus
import com.neo.lingxumusic.ui.common.DragToggleState
import com.neo.lingxumusic.ui.common.FixHeadBackgroundDraggableBodyLayout
import com.neo.lingxumusic.ui.common.rememberDragToggleState
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.toPx
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


// 主页面
@Composable
fun MinePage() {
    val viewModel: MineViewModel = hiltViewModel()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
    ) {
        // 1. 数据加载区域（会切换 Loading / 内容 / 错误 页面）
        ViewStateComponent(
            viewStateLiveData = viewModel.userPlaylistResult,
            loadDataBlock = { viewModel.getUserPlayList() }) {

            Box {
                var dragStatus by remember { // 拖拽状态
                    mutableStateOf<DragStatus>(DragStatus.Idle)
                }
                val dragToggleState = rememberDragToggleState(dragStatus) // 拖拽状态管理器

                FixHeadBackgroundDraggableBodyLayout(
                    state = dragToggleState,
                    triggerRadio = 0.38f, //触发阈值比例（0-1）
                    maxDragRadio = 0.643f, //最大拖拽距离比例（0-1）
                    modifier = Modifier.background(Color(0xFFEEEEEE)),
                    onOverOpenTrigger = { // 超过触发点回调
                        dragStatus = DragStatus.OverOpenTrigger
                    },
                    onOpened = { // 完全打开回调
                        dragStatus = DragStatus.Opened
                    },
                    headBackgroundComponent = { state, trigger, maxDrag -> // 头部背景组件
                        var alpha = state.offset / maxDrag
                        if (alpha > 1f) {
                            alpha = 1f
                        }
                        HeaderBackground(alpha)
                    }) {
                    Body(dragToggleState)  // 主体内容
                }
            }
        }

        // 2. 顶部导航栏（覆盖在上面，始终显示）
        CommonTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            leftIconResId = R.drawable.ic_drawer_toggle,
            leftClick = { }, //todo
            rightIconResId = R.drawable.ic_search
        )
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
            .clip(BgImageShapes())   // 底部弧形裁剪
            .graphicsLayer {
                 alpha = alphaValue  // 透明度
            }
    )
}

var animateScrolling = false

// 主体内容（LazyColumn）
/*
滚动前（列表在顶部）：
┌─────────────────┐
│   用户信息        │ ← 普通 item
│   音乐应用        │ ← 普通 item
│   喜欢的音乐      │ ← 普通 item
│   Tab 栏          │ ← stickyHeader
│   创建歌单列表     │ ← 普通 item
│   收藏歌单列表     │ ← 普通 item
└─────────────────┘

用户向上滚动一点：
┌─────────────────┐
│   Tab 栏          │ ← stickyHeader 粘在顶部！
├─────────────────┤
│   创建歌单列表     │
│   收藏歌单列表     │
│   歌单助手        │
└─────────────────┘
（用户信息、音乐应用、喜欢的音乐都滚走了）

再滚动到底部：
┌─────────────────┐
│   Tab 栏          │ ← 始终粘在顶部！
├─────────────────┤
│   歌单助手        │
│                 │
│                 │
└─────────────────┘*/
@Composable
private fun Body(dragToggleState: DragToggleState) {
    val scrollState = rememberLazyListState()           // 滚动状态
    val coroutineScope = rememberCoroutineScope()       // 协程作用域
    val offsetY = remember { -88.cdp.toPx.toInt() }     // 滚动偏移量（负值）
    val selectedTabIndex = remember { mutableStateOf(0) } // 当前选中的 Tab
    val viewModel: MineViewModel = hiltViewModel()       // ViewModel

    //dragToggleState 是头部背景的拖拽状态
    //当用户正在拖拽头部时（isDraggableInProgress = true），把 animateScrolling 设为 false
    if (dragToggleState.isDraggableInProgress) {
        animateScrolling = false
    }

    LazyColumn(
        modifier = Modifier
            .statusBarsPadding()      // 避开状态栏
            .padding(top = 88.cdp)    // 顶部留 88dp，给 TopAppBar 腾空间
            .fillMaxSize(),           // 填满整个屏幕
        state = scrollState,          // 绑定滚动状态
    ) {
        // 用户信息
        item {
            UserInfoComponent()
        }

        // 音乐应用
        item {
            Box(
                modifier = Modifier
                    .mineCommonCard()      // 卡片样式
                    .height(300.cdp),      // 高度 300dp
                contentAlignment = Alignment.Center
            ) {
                MusicApplicationComponent() // 音乐快捷入口
            }
        }

        //喜欢的音乐
        item {
            Box(
                modifier = Modifier
                    .mineCommonCard(),
                contentAlignment = Alignment.Center
            ) {
                UserPlaylistItem(viewModel.favoritePlayList)
            }
        }

        //粘性 Tab 栏
        stickyHeader {
            CommonTabLayout(
                tabTexts = tabs, // ["创建歌单", "收藏歌单", "歌单助手"]
                style = CommonTabLayoutStyle(isScrollable = false),
                selectedIndex = selectedTabIndex.value // 当前选中的 Tab
            ) { index ->
                selectedTabIndex.value = index                    // 1. 更新选中状态
                animateScrolling = true                           // 2. 标记正在动画滚动
                coroutineScope.launch {                           // 3. 启动协程
                    //index + 4 ：把 Tab 索引 映射到 LazyColumn 中对应内容的索引
                    //再用offsetY造成偏移
                    scrollState.animateScrollToItem(index + 4, offsetY)  // 4. 滚动到对应位置
                    animateScrolling = false                      // 5. 动画结束
                }
            }
        }

        //创建歌单列表
        item {
            UserPlaylistComponent(
                list = viewModel.selfCreatePlayList,
                title = "创建歌单",
                itemPosition = 5,
                selectedTabIndex = selectedTabIndex
            )
        }

        //收藏歌单列表
        item {
            UserPlaylistComponent(
                list = viewModel.collectPlayList,
                title = "收藏歌单",
                itemPosition = 6,
                selectedTabIndex = selectedTabIndex
            )
        }

        //底部区域
        item {
            Box(
                modifier = Modifier
                    .padding(bottom = 30.cdp)
                    .mineCommonCard()
                    .height(500.cdp)
                    .onGloballyPositioned {
                        if(!animateScrolling) {
                            // 获取最后一个可见的 item
                            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo[scrollState.layoutInfo.visibleItemsInfo.size - 1]
                            // 判断是否滚动到了底部
                            if (lastVisibleItem.offset + lastVisibleItem.size >= scrollState.layoutInfo.viewportEndOffset) {
                                selectedTabIndex.value = 2  // 切换到"歌单助手"
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("歌单助手")
            }
        }
    }
}

// 歌单列表组件
@Composable
fun UserPlaylistComponent(modifier: Modifier = Modifier,
                          list: List<Playlist>?,           // 歌单列表
                          title: String,                    // 标题（如"创建歌单"）
                          itemPosition: Int,                // 在 LazyColumn 中的位置索引
                          selectedTabIndex: MutableState<Int>,  // 当前选中的 Tab 索引
) {
    // 粘性头部高度 = TopAppBar(88) + 间距(12)
    val stickyHeight = remember {
        88.cdp.toPx.toInt() + 12.cdp.toPx.toInt()
    }

    list?.let {
        Box(
            modifier = modifier
                .mineCommonCard() // 卡片样式
                .onGloballyPositioned {
                    // 滚动监听：检测此组件是否滚动到粘性头部下方
                    if(!animateScrolling) {
                        val top = it.boundsInParent().top      // 组件顶部位置
                        val bottom = it.boundsInParent().bottom  // 组件底部位置
                        // 当组件顶部超过粘性头部，且底部还在粘性头部下方时
                        if (top <= stickyHeight && bottom > stickyHeight) {
                            // 更新选中的 Tab
                            //item位置与tab索引不是一一对应，要减去五
                            selectedTabIndex.value = itemPosition - 5
                        }
                    }
                }
        ) {
            Column {
                // 标题行
                Text(
                    text = "${title}(${list.size}个)",
                    color = AppColorsProvider.current.secondText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp, top = 12.cdp)
                )
                // 歌单列表
                it.forEach {
                    UserPlaylistItem(it)
                }
            }
        }
    }
}


// 卡片样式扩展函数
@Composable
fun Modifier.mineCommonCard() = this
    .padding(horizontal = 32.cdp, vertical = 12.cdp)  // 1. 外层间距
    .fillMaxWidth()                                    // 2. 宽度占满
    .shadow(4.cdp, RoundedCornerShape(24.cdp))        // 3. 阴影效果
    .background(AppColorsProvider.current.card)       // 4. 背景颜色
    .padding(start = 32.cdp, end = 32.cdp, top = 24.cdp, bottom = 24.cdp)  // 5. 内层间距


private val tabs = listOf("创建歌单", "收藏歌单", "歌单助手")

// 视图模型
@HiltViewModel
class MineViewModel @Inject constructor(private val api: UserApi) : BaseViewStateViewModel() {

    var favoritePlayList: Playlist? by mutableStateOf(null)      // 喜欢的音乐歌单
    var selfCreatePlayList: List<Playlist>? by mutableStateOf(null)  // 自己创建的歌单
    var collectPlayList: List<Playlist>? by mutableStateOf(null)      // 收藏的歌单

    // 请求状态
    val userPlaylistResult = ViewStateMutableLiveData()

    // 获取歌单
    fun getUserPlayList() {
        launch(userPlaylistResult, handleResult = {
            val selfCreateList = mutableListOf<Playlist>()
            val collectList = mutableListOf<Playlist>()
            val userId = AppGlobalData.sLoginData?.userid
            val playlistData = it.dataAs<PlaylistData>()

            playlistData?.info.orEmpty().forEach { playlist ->
                if (playlist.list_create_userid == userId) {  // 是自己创建的
                    if (playlist.name?.contains("喜欢") ?: false) { // 是"喜欢的音乐"歌单
                        favoritePlayList = playlist  // → 单独存储
                    } else {
                        selfCreateList.add(playlist) // → 创建的歌单
                    }
                } else {
                    collectList.add(playlist) // → 收藏的歌单
                }
            }
            selfCreatePlayList = selfCreateList
            collectPlayList = collectList
        }) {
            api.getUserPlayList()
        }
    }
}