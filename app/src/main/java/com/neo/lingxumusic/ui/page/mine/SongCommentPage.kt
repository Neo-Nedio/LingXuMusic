package com.neo.lingxumusic.ui.page.mine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.neo.lingxumusic.core.MusicPlayController
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.ui.common.CommonTopAppBar
import com.neo.lingxumusic.ui.theme.AppColorsProvider

@Composable
fun SongCommentPage(song: Song?) {

    BackHandler(true) {
        NavController.instance.popBackStack()
        MusicPlayController.playMusicSheetOffset = 0
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(AppColorsProvider.current.background)
    ) {
        CommonTopAppBar(title = "评论",
            titleAlign = TextAlign.Start,
            leftClick = {
                NavController.instance.popBackStack()
                MusicPlayController.playMusicSheetOffset = 0
            })
        Text(text = song?.name.orEmpty())
    }
}
