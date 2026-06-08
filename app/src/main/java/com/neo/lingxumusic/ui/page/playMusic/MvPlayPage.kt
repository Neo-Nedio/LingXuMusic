package com.neo.lingxumusic.ui.page.playMusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.VideoPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.displayPlayUrl
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.page.brush.component.BrushVideoPlay
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.viewmodel.playMusic.MvPlayViewModel

@Composable
fun MvPlayPage(albumAudioId: Long, songName: String?, singerName: String?) {
    val viewModel: MvPlayViewModel = hiltViewModel()
    val context = LocalContext.current

    // 页面进入时初始化视频播放器，退出时释放资源并恢复歌曲播放
    DisposableEffect(Unit) {
        VideoPlayController.initIfNeeded(context)
        onDispose {
            VideoPlayController.release()
            MusicPlayController.showPlayMusicSheet = true
            MusicPlayController.resume()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ViewStateComponent(
            viewStateLiveData = viewModel.mvResult,
            loadDataBlock = { viewModel.loadMv(albumAudioId, songName, singerName) },
            customEmptyComponent = {
                // 自定义空页面：暂无 MV
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    CommonIcon(
                        resId = R.drawable.ic_empty,
                        modifier = Modifier.size(200.cdp),
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.cdp))
                    Text(
                        text = "暂无 MV",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 32.csp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            },
            contentView = {
                // 数据加载成功后自动播放
                LaunchedEffect(viewModel.mvVideo) {
                    viewModel.mvVideo?.let {
                        //绑定VideoPlayController
                        VideoPlayController.switchVideo(0, it.displayPlayUrl)
                    }
                }

                viewModel.mvVideo?.let { video ->
                    BrushVideoPlay(
                        index = 0,
                        lazyListState = rememberLazyListState(),
                        video = video,
                        itemCount = 1,
                        onSwitchVideo = {},
                        modifier = Modifier.fillMaxSize(),
                        keepAspectRatio = true  // MV 保持原始宽高比，横屏视频居中不拉伸
                    )
                }
            }
        )

        // 顶部导航栏（透明背景，白色文字，左上角返回键）
        CommonTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            title = "MV",
            leftIconResId = R.drawable.ic_arrow_down,
            leftClick = { NavController.instance.popBackStack() },
            backgroundColor = Color.Transparent,
            contentColor = Color.White,
        )
    }
}
