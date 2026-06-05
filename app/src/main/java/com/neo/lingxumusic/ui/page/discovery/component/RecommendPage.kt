package com.neo.lingxumusic.ui.page.discovery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.core.navigation.RoutesConstant
import com.neo.lingxumusic.core.viewState.ViewStateComponent
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.displayTitle
import com.neo.lingxumusic.model.toBrief
import com.neo.lingxumusic.model.toPlaylist
import com.neo.lingxumusic.model.toSongList
import com.neo.lingxumusic.ui.common.CommonIcon
import com.neo.lingxumusic.ui.common.CommonNetworkImage
import com.neo.lingxumusic.ui.common.CommonTabLayout
import com.neo.lingxumusic.ui.common.CommonTabLayoutStyle
import com.neo.lingxumusic.ui.common.MarqueeText
import com.neo.lingxumusic.ui.page.playMusic.component.SongItem
import com.neo.lingxumusic.ui.page.playList.component.RecommendPlayListItem
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.ui.theme.isInDarkTheme
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import com.neo.lingxumusic.utils.replaceSize
import com.neo.lingxumusic.utils.showToast
import com.neo.lingxumusic.viewmodel.discovery.RecommendViewModel

@Composable
fun RecommendPage() {
    val viewModel: RecommendViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.cdp, vertical = 24.cdp),
    ) {
        EveryDayAndGuessLikeRow(viewModel)
        RecommendSongs(modifier = Modifier.padding(top = 24.cdp))
        RecommendPlayLists(modifier = Modifier.padding(top = 24.cdp))
    }
}

//每日推荐与猜你喜欢
@Composable
private fun EveryDayAndGuessLikeRow(viewModel: RecommendViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ViewStateComponent(
            modifier = Modifier.weight(1f),
            viewStateLiveData = viewModel.everyDayResult,
            loadDataBlock = { viewModel.loadEveryDayRecommend() },
            specialRetryBlock = { viewModel.loadEveryDayRecommend() },
            viewStateContentAlignment = Alignment.TopCenter,
            viewStateComponentModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            RecommendCoverCard(
                modifier = Modifier.align(Alignment.TopCenter),
                label = "每日推荐",
                songName = viewModel.everyDaySongList.firstOrNull()?.displayTitle().orEmpty(),
                coverUrl = viewModel.everyDayCover,
                onClick = {
                    playRecommendSongs(viewModel.everyDaySongList.toSongList())
                },
            )
        }
        Box(modifier = Modifier.width(16.cdp))
        ViewStateComponent(
            modifier = Modifier.weight(1f),
            viewStateLiveData = viewModel.guessLikeResult,
            loadDataBlock = { viewModel.loadGuessYourLike() },
            specialRetryBlock = { viewModel.loadGuessYourLike() },
            viewStateContentAlignment = Alignment.TopCenter,
            viewStateComponentModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            RecommendCoverCard(
                modifier = Modifier.align(Alignment.TopCenter),
                label = "猜你喜欢",
                songName = viewModel.guessLikeSongList.firstOrNull()?.displayTitle().orEmpty(),
                coverUrl = viewModel.guessLikeCover,
                onClick = {
                    playRecommendSongs(viewModel.guessLikeSongList.toSongList())
                },
            )
        }
    }
}

//推荐的card
@Composable
private fun RecommendCoverCard(
    label: String,
    songName: String,
    coverUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isInDarkTheme()
    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF4A4A4A)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFE0E0E0)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }
    val textColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.cdp))
            .background(brush = backgroundBrush)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.65f)
                .padding(start = 16.cdp, top = 16.cdp, end = 8.cdp),
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 26.csp,
            )
            MarqueeText(
                text = songName,
                modifier = Modifier.padding(top = 8.cdp),
                color = textColor,
                fontSize = 22.csp,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.58f)
                .aspectRatio(1f)
                .padding(end = 12.cdp, bottom = 12.cdp),
        ) {
            CommonNetworkImage(
                url = coverUrl?.replaceSize(),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.cdp)),
            )
            CommonIcon(
                resId = R.drawable.ic_action_play,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.cdp)
                    .size(40.cdp),
                tint = Color.White,
            )
        }
    }
}


//获取歌曲推荐
@Composable
private fun RecommendSongs(
    modifier: Modifier = Modifier,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    var recommendSongsCardId by remember { mutableStateOf("1") }
    val selectedIndex = viewModel.recommendCardTabs
        .indexOfFirst { it.cardId == recommendSongsCardId }
        .coerceAtLeast(0)

    Column(modifier = modifier.fillMaxWidth()) {
        //标题
        Text(
            text = "歌曲推荐",
            color = AppColorsProvider.current.firstText,
            fontSize = 32.csp,
            fontWeight = FontWeight.Bold,
        )
        //推荐tabs
        CommonTabLayout(
            tabTexts = viewModel.recommendCardTabs.map { it.title },
            backgroundColor = Color.Transparent,
            style = CommonTabLayoutStyle(
                isScrollable = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.cdp),
                selectedTextSize = 28.csp,
                unselectedTextSize = 28.csp,
                tabHorizontalPadding = 16.cdp,
                customIndicator = { },
            ),
            selectedIndex = selectedIndex,
        ) {
            val cardId = viewModel.recommendCardTabs[it].cardId
            if (cardId == recommendSongsCardId) {
                return@CommonTabLayout
            }
            recommendSongsCardId = cardId
            viewModel.recommendSongList = emptyList()
            viewModel.loadRecommendSongs(cardId)
        }
        //下部推荐栏
        ViewStateComponent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.cdp),
            viewStateLiveData = viewModel.recommendSongsResult,
            loadDataBlock = { viewModel.loadRecommendSongs(recommendSongsCardId) },
            specialRetryBlock = { viewModel.loadRecommendSongs(recommendSongsCardId) },
            viewStateComponentModifier = Modifier.fillMaxWidth(),
            viewStateContentAlignment = Alignment.TopCenter,
        ) {
            val songs = viewModel.recommendSongList.toSongList()
            LazyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                //songs.chunked(3) 是将一个列表按每 3 个元素为一组进行分组
                itemsIndexed(songs.chunked(3)) { columnIndex, columnSongs ->
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth(0.9f)  // 每个 item 占 LazyRow 可视区域宽度的 90%
                            .padding(end = 16.cdp),    // 右侧间距（可选）
                    ) {
                        columnSongs.forEachIndexed { rowIndex, song ->
                            SongItem(
                                index = columnIndex * 3 + rowIndex,
                                song = song,
                                onClick = { MusicPlayController.addSong(song) },
                            )
                        }
                    }
                }
            }
        }
    }
}


//获取歌单推荐
@Composable
private fun RecommendPlayLists(
    modifier: Modifier = Modifier,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "歌单推荐",
            color = AppColorsProvider.current.firstText,
            fontSize = 32.csp,
            fontWeight = FontWeight.Bold,
        )
        ViewStateComponent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.cdp),
            viewStateLiveData = viewModel.recommendPlayListResult,
            loadDataBlock = { viewModel.loadRecommendPlaylists() },
            specialRetryBlock = { viewModel.loadRecommendPlaylists() },
            viewStateComponentModifier = Modifier.fillMaxWidth(),
            viewStateContentAlignment = Alignment.TopCenter,
        ) {
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(viewModel.recommendPlayList.chunked(5)) { columnIndex, columnPlaylists ->
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth(0.9f)
                            .padding(end = 26.cdp),
                    ) {
                        columnPlaylists.forEach { playlist ->
                            RecommendPlayListItem(
                                playlist = playlist,
                                onClick = {
                                    NavController.instance.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(
                                            RoutesConstant.KEY_PLAY_LIST_BRIEF,
                                            playlist.toPlaylist().toBrief(),
                                        )
                                    NavController.instance.navigate(Routes.PLAY_LIST)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun playRecommendSongs(songs: List<Song>) {
    val firstHash = songs.firstOrNull()?.hash
    if (firstHash.isNullOrEmpty()) {
        showToast("该歌曲暂不支持播放")
    } else {
        MusicPlayController.songList.clear()
        MusicPlayController.setDataSource(songs, firstHash)
        MusicPlayController.showBottomMusicPlay = false
        MusicPlayController.showPlayMusicSheet = true
    }
}